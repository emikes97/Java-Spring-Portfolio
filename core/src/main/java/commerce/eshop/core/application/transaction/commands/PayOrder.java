package commerce.eshop.core.application.transaction.commands;

import commerce.eshop.core.application.events.payments.PaymentExecutionRequestEvent;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.transaction.factory.TransactionFactory;
import commerce.eshop.core.application.transaction.validation.AuditedTransactionValidation;
import commerce.eshop.core.application.transaction.writer.TransactionWriter;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.model.entity.Order;
import commerce.eshop.core.model.entity.Transaction;
import commerce.eshop.core.web.dto.requests.Transactions.DTOTransactionRequest;
import commerce.eshop.core.web.dto.requests.Transactions.PaymentVariants.UseNewCard;
import commerce.eshop.core.web.dto.requests.Transactions.PaymentVariants.UseSavedMethod;
import commerce.eshop.core.web.dto.response.Transactions.DTOTransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class PayOrder {

    // == Fields ==
    private final AuditedTransactionValidation validation;
    private final DomainLookupService domainLookupService;
    private final TransactionFactory transactionFactory;
    private final TransactionWriter writer;
    private final ApplicationEventPublisher publisher;

    // == Constructors ==
    @Autowired
    public PayOrder(AuditedTransactionValidation validation, DomainLookupService domainLookupService,
                    TransactionFactory transactionFactory, TransactionWriter writer, ApplicationEventPublisher publisher) {
        this.validation = validation;
        this.domainLookupService = domainLookupService;
        this.transactionFactory = transactionFactory;
        this.writer = writer;
        this.publisher = publisher;
    }

    // == Public Methods ==
    @Transactional
    public DTOTransactionResponse handle(UUID customerId, UUID orderId, String idemKey, DTOTransactionRequest dto){

        validation.checkIdemKeyValidity(idemKey, customerId); // -> fail early in case of damaged/null idemkey
        validation.checkDtoValidity(dto, customerId); // -> fail early in case of broken dto

        Order order = domainLookupService.getOrderOrThrow(customerId, orderId, EndpointsNameMethods.TRANSACTION_PAY);
        validation.checkOrderState(order.getOrderStatus(), customerId);
        validation.checkTotalOutstanding(order.getTotalOutstanding(), customerId);

        Transaction transaction = transactionFactory.handle(dto, order, customerId.toString(), idemKey);


        // -> Idemkey run , if duplicate resolve early
        try {
            transaction = writer.save(transaction);
        } catch (DataIntegrityViolationException dup){
            transaction = writer.getByIdemKey(idemKey);
            return resolveWinner(transaction, orderId);
        }

        String paymentMethod = "";

        if (dto.instruction() instanceof UseNewCard card){
            paymentMethod = "USE_NEW_CARD";
        } else if (dto.instruction() instanceof UseSavedMethod card) {
            paymentMethod = "USE_SAVED_METHOD";
        } else {
            validation.unSupportedPaymentMethod(customerId);
        }

        String message = "Payment by " + paymentMethod;
        PaymentExecutionRequestEvent event = new PaymentExecutionRequestEvent(transaction.getTransactionId(), transaction.getOrder().getOrderId(), customerId, paymentMethod,
                transaction.getPaymentMethod(), transaction.getTotalOutstanding(), transaction.getIdempotencyKey());
        publisher.publishEvent(event);
        validation.auditSuccess(customerId,message);
        return transactionFactory.toDto(transaction);
    }

    private DTOTransactionResponse resolveWinner(Transaction winner, UUID expectedOrderId){
        validation.checkIfSameIdemKey(winner, expectedOrderId);
        validation.checkTransactionStatus(winner);
        return transactionFactory.toDto(winner);
    }
}
