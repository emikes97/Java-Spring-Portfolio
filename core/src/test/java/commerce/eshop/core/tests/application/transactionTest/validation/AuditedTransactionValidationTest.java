package commerce.eshop.core.tests.application.transactionTest.validation;

import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.transaction.validation.AuditedTransactionValidation;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import commerce.eshop.core.application.util.enums.OrderStatus;
import commerce.eshop.core.application.util.enums.TransactionStatus;
import commerce.eshop.core.model.entity.Order;
import commerce.eshop.core.model.entity.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;

class AuditedTransactionValidationTest {

    // == Fields ==
    private CentralAudit centralAudit;
    private AuditedTransactionValidation auditedTransactionValidation;

    // == Tests ==

    @BeforeEach
    void setUp() {
        centralAudit = mock(CentralAudit.class);

        auditedTransactionValidation = new AuditedTransactionValidation(centralAudit);
        mockAuditReturnSame(centralAudit);
    }

    @Test
    void checkIdemKeyValidity_validKey_success() {
        UUID customerId = UUID.randomUUID();

        assertDoesNotThrow(
                () -> auditedTransactionValidation.checkIdemKeyValidity("IDEM-123", customerId)
        );

        verify(centralAudit, never()).audit(any(), any(), anyString(), any(), anyString());
    }

    @Test
    void checkIdemKeyValidity_missingKey_throwsBadRequestAndAudits() {
        UUID customerId = UUID.randomUUID();

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> auditedTransactionValidation.checkIdemKeyValidity("   ", customerId)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("MISSING_IDEM_KEY", ex.getReason());

        verify(centralAudit, times(1)).audit(
                any(ResponseStatusException.class),
                eq(customerId),
                eq(EndpointsNameMethods.TRANSACTION_PAY),
                eq(AuditingStatus.WARNING),
                eq("MISSING_IDEM_KEY")
        );
    }

    @Test
    void checkDtoValidity_nullDto_throw() {
        UUID customerId = UUID.randomUUID();

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> auditedTransactionValidation.checkDtoValidity(null, customerId)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("MISSING_INSTRUCTION", ex.getReason());

        verify(centralAudit, times(1)).audit(
                any(ResponseStatusException.class),
                eq(customerId),
                eq(EndpointsNameMethods.TRANSACTION_PAY),
                eq(AuditingStatus.WARNING),
                eq("MISSING_INSTRUCTION")
        );
    }

    @Test
    void checkOrderState_pendingPayment_success() {
        UUID customerId = UUID.randomUUID();

        assertDoesNotThrow(
                () -> auditedTransactionValidation.checkOrderState(OrderStatus.PENDING_PAYMENT, customerId)
        );
    }

    @Test
    void checkOrderState_invalidState_throw() {
        UUID customerId = UUID.randomUUID();

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> auditedTransactionValidation.checkOrderState(OrderStatus.CANCELLED, customerId)
        );

        assertTrue(ex.getMessage().startsWith("INVALID_STATE:"));

        verify(centralAudit, times(1)).audit(
                any(IllegalStateException.class),
                eq(customerId),
                eq(EndpointsNameMethods.TRANSACTION_PAY),
                eq(AuditingStatus.WARNING),
                argThat(s -> s.contains("INVALID_STATE:")
        ));
    }

    @Test
    void checkTotalOutstanding_positive_success() {
        UUID customerId = UUID.randomUUID();
        assertDoesNotThrow(
                () -> auditedTransactionValidation.checkTotalOutstanding(new BigDecimal("10.00"), customerId)
        );
    }

    @Test
    void checkTotalOutstanding_zeroOrNegative_throw() {
        UUID customerId = UUID.randomUUID();

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> auditedTransactionValidation.checkTotalOutstanding(BigDecimal.ZERO, customerId)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals("ZERO_OR_NEGATIVE_TOTAL", ex.getReason());

        verify(centralAudit, times(1)).audit(
                any(ResponseStatusException.class),
                eq(customerId),
                eq(EndpointsNameMethods.TRANSACTION_PAY),
                eq(AuditingStatus.WARNING),
                eq("ZERO_OR_NEGATIVE_TOTAL")
        );
    }

    @Test
    void checkIfSameIdemKey_sameOrder_success() {
        UUID expectedOrderId = UUID.randomUUID();

        Transaction winner = mock(Transaction.class);
        Order order = mock(Order.class);

        when(winner.getOrder()).thenReturn(order);
        when(order.getOrderId()).thenReturn(expectedOrderId);

        assertDoesNotThrow(
                () -> auditedTransactionValidation.checkIfSameIdemKey(winner, expectedOrderId)
        );
    }

    @Test
    void checkIfSameIdemKey_differentOrder_throw() {
        UUID expectedOrderId = UUID.randomUUID();
        UUID otherOrderId = UUID.randomUUID();

        Transaction winner = mock(Transaction.class);
        Order order = mock(Order.class);

        when(winner.getOrder()).thenReturn(order);
        when(order.getOrderId()).thenReturn(otherOrderId);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> auditedTransactionValidation.checkIfSameIdemKey(winner, expectedOrderId)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals("Idempotency key reused for a different order", ex.getReason());
    }

    @Test
    void checkTransactionStatus_pending_throwsAccepted() {
        Transaction winner = mock(Transaction.class);
        when(winner.getStatus()).thenReturn(TransactionStatus.PENDING);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> auditedTransactionValidation.checkTransactionStatus(winner)
        );

        assertEquals(HttpStatus.ACCEPTED, ex.getStatusCode());
        assertEquals("Transaction already processing", ex.getReason());
    }

    @Test
    void checkTransactionStatus_nonPending_success() {
        Transaction winner = mock(Transaction.class);
        when(winner.getStatus()).thenReturn(TransactionStatus.SUCCESSFUL);

        assertDoesNotThrow(
                () -> auditedTransactionValidation.checkTransactionStatus(winner)
        );
    }

    @Test
    void unSupportedPaymentMethod_throw() {
        UUID customerId = UUID.randomUUID();

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> auditedTransactionValidation.unSupportedPaymentMethod(customerId)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Unsupported payment instruction", ex.getReason());

        verify(centralAudit, times(1)).audit(
                any(ResponseStatusException.class),
                eq(customerId),
                eq(EndpointsNameMethods.TRANSACTION_PAY),
                eq(AuditingStatus.WARNING),
                eq("Unsupported payment instruction")
        );
    }

    @Test
    void auditSuccess_logsInfoWithSuccessfulStatus() {
        UUID customerId = UUID.randomUUID();
        String paymentMethod = "CARD_PROVIDER_X";

        auditedTransactionValidation.auditSuccess(customerId, paymentMethod);

        verify(centralAudit, times(1)).info(
                eq(customerId),
                eq(EndpointsNameMethods.TRANSACTION_PAY),
                eq(AuditingStatus.SUCCESSFUL),
                eq(paymentMethod)
        );
    }

    // == Private Methods ==

    static <E extends RuntimeException> void mockAuditReturnSame(CentralAudit auditMock) {
        // 5-arg overload
        when(auditMock.audit(
                any(RuntimeException.class),
                nullable(UUID.class),
                anyString(),
                any(AuditingStatus.class),
                anyString()
        )).thenAnswer(inv -> inv.getArgument(0));

        // 4-arg overload
        when(auditMock.audit(
                any(RuntimeException.class),
                nullable(UUID.class),
                anyString(),
                any(AuditingStatus.class)
        )).thenAnswer(inv -> inv.getArgument(0));
    }
}