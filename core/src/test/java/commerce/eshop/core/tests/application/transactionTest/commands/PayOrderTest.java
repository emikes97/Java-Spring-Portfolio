package commerce.eshop.core.tests.application.transactionTest.commands;

import commerce.eshop.core.application.events.payments.PaymentExecutionRequestEvent;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.transaction.commands.PayOrder;
import commerce.eshop.core.application.transaction.factory.TransactionFactory;
import commerce.eshop.core.application.transaction.validation.AuditedTransactionValidation;
import commerce.eshop.core.application.transaction.writer.TransactionWriter;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.enums.OrderStatus;
import commerce.eshop.core.model.entity.Order;
import commerce.eshop.core.model.entity.Transaction;
import commerce.eshop.core.web.dto.requests.Transactions.DTOTransactionRequest;
import commerce.eshop.core.web.dto.requests.Transactions.PaymentVariants.UseNewCard;
import commerce.eshop.core.web.dto.response.Transactions.DTOTransactionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PayOrderTest {

    // == Fields ==
    private AuditedTransactionValidation auditedTransactionValidation;
    private DomainLookupService domainLookupService;
    private TransactionFactory transactionFactory;
    private TransactionWriter transactionWriter;
    private ApplicationEventPublisher applicationEventPublisher;
    private PayOrder payOrder;

    // == Tests ==

    @BeforeEach
    void setUp() {
        auditedTransactionValidation = mock(AuditedTransactionValidation.class);
        domainLookupService = mock(DomainLookupService.class);
        transactionFactory = mock(TransactionFactory.class);
        transactionWriter = mock(TransactionWriter.class);
        applicationEventPublisher = mock(ApplicationEventPublisher.class);

        payOrder = new PayOrder(auditedTransactionValidation, domainLookupService, transactionFactory, transactionWriter, applicationEventPublisher);
    }

    @Test
    void handle_useNewCard_success() {
        UUID customerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        String idemKey = "idem-123";
        UUID transactionid = UUID.randomUUID();
        DTOTransactionRequest dto = mock(DTOTransactionRequest.class);
        Order order = mock(Order.class);
        Transaction transaction = mock(Transaction.class);
        DTOTransactionResponse dtoResponse = mock(DTOTransactionResponse.class);
        UseNewCard instruction = new UseNewCard(
                "4111111111111111",
                "VISA",
                12,
                30,
                "Mike",
                "123"
        );

        when(domainLookupService.getOrderOrThrow(customerId, orderId, EndpointsNameMethods.TRANSACTION_PAY))
                .thenReturn(order);
        when(order.getOrderStatus()).thenReturn(OrderStatus.PENDING_PAYMENT);
        when(order.getTotalOutstanding()).thenReturn(BigDecimal.TEN);
        when(dto.instruction()).thenReturn(instruction);
        when(transactionFactory.handle(dto, order, customerId.toString(), idemKey))
                .thenReturn(transaction);
        when(transactionWriter.save(transaction)).thenReturn(transaction);
        when(transactionFactory.toDto(transaction)).thenReturn(dtoResponse);

        when(transaction.getTransactionId()).thenReturn(transactionid);
        when(transaction.getOrder()).thenReturn(order);
        when(order.getOrderId()).thenReturn(orderId);
        when(transaction.getPaymentMethod()).thenReturn(Map.of("type", "card"));
        when(transaction.getTotalOutstanding()).thenReturn(BigDecimal.TEN);
        when(transaction.getIdempotencyKey()).thenReturn(idemKey);

        DTOTransactionResponse result = payOrder.handle(customerId, orderId, idemKey, dto);

        assertSame(dtoResponse, result);

        verify(auditedTransactionValidation).checkIdemKeyValidity(idemKey, customerId);
        verify(auditedTransactionValidation).checkDtoValidity(dto, customerId);
        verify(auditedTransactionValidation).checkOrderState(OrderStatus.PENDING_PAYMENT, customerId);
        verify(auditedTransactionValidation).checkTotalOutstanding(BigDecimal.TEN, customerId);
        verify(transactionFactory).handle(dto, order, customerId.toString(), idemKey);
        verify(transactionWriter).save(transaction);

        ArgumentCaptor<PaymentExecutionRequestEvent> eventCaptor =
                ArgumentCaptor.forClass(PaymentExecutionRequestEvent.class);

        verify(applicationEventPublisher, times(1)).publishEvent(eventCaptor.capture());
        PaymentExecutionRequestEvent event = eventCaptor.getValue();

        assertEquals(transactionid, event.transactionId());
        assertEquals(orderId, event.orderId());
        assertEquals(customerId, event.customerId());
        assertEquals("USE_NEW_CARD", event.methodType());
        assertEquals(idemKey, event.idemKey());

        verify(auditedTransactionValidation, times(1))
                .auditSuccess(customerId, "Payment by USE_NEW_CARD");
    }

    @Test
    void handle_duplicateIdemKey_skipEvent() {
        UUID customerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        String idemKey = "idem-dup";
        DTOTransactionRequest dto = mock(DTOTransactionRequest.class);
        Order order = mock(Order.class);
        Transaction justCreatedTransaction = mock(Transaction.class);
        Transaction winnerTransaction = mock(Transaction.class);
        DTOTransactionResponse winnerDto = mock(DTOTransactionResponse.class);
        UseNewCard instruction = new UseNewCard(
                "4111111111111111",
                "VISA",
                12,
                30,
                "Mike",
                "123"
        );

        when(domainLookupService.getOrderOrThrow(customerId, orderId, EndpointsNameMethods.TRANSACTION_PAY))
                .thenReturn(order);
        when(order.getOrderStatus()).thenReturn(OrderStatus.PENDING_PAYMENT);
        when(order.getTotalOutstanding()).thenReturn(BigDecimal.ONE);
        when(dto.instruction()).thenReturn(instruction);
        when(transactionFactory.handle(dto, order, customerId.toString(), idemKey))
                .thenReturn(justCreatedTransaction);
        when(transactionWriter.save(justCreatedTransaction))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));
        when(transactionWriter.getByIdemKey(idemKey)).thenReturn(winnerTransaction);
        when(transactionFactory.toDto(winnerTransaction)).thenReturn(winnerDto);

        DTOTransactionResponse result = payOrder.handle(customerId, orderId, idemKey, dto);

        assertSame(winnerDto, result);

        verify(auditedTransactionValidation).checkIfSameIdemKey(winnerTransaction, orderId);
        verify(auditedTransactionValidation).checkTransactionStatus(winnerTransaction);
        verify(applicationEventPublisher, never()).publishEvent(any());
        verify(auditedTransactionValidation, never())
                .auditSuccess(any(), anyString());
    }
}