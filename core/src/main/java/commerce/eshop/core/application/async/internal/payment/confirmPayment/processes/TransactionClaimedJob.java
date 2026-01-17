package commerce.eshop.core.application.async.internal.payment.confirmPayment.processes;

import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.transaction.factory.TransactionFactory;
import commerce.eshop.core.application.transaction.writer.TransactionWriter;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.model.entity.Order;
import commerce.eshop.core.model.entity.Transaction;
import commerce.eshop.core.web.dto.requests.Transactions.DTOTransactionRequest;
import commerce.eshop.core.web.dto.requests.Transactions.PaymentVariants.UseNewCard;
import commerce.eshop.core.web.dto.requests.Transactions.PaymentVariants.UseSavedMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class TransactionClaimedJob {

    // == Fields ==
    private final ValidateInfo validate;
    private final DomainLookupService domainLookupService;
    private final TransactionFactory factory;
    private final TransactionWriter writer;

    // == Constructors ==
    @Autowired
    public TransactionClaimedJob(ValidateInfo validate, DomainLookupService domainLookupService, TransactionFactory factory, TransactionWriter writer) {
        this.validate = validate;
        this.domainLookupService = domainLookupService;
        this.factory = factory;
        this.writer = writer;
    }

    public Order findOrder(UUID idemKey, UUID customerId, DTOTransactionRequest dto, UUID orderId){
        validate.checkTransactionValidity(idemKey, customerId, dto);
        Order order = domainLookupService.getOrderOrThrow(customerId, orderId, EndpointsNameMethods.TRANSACTION_PAY);
        validate.checkOrderStatus(order, customerId);
        return order;
    }

    public Transaction createTransaction(DTOTransactionRequest dto, Order order, String customerId, String idemKey){
        Transaction transaction =  factory.handle(dto, order, customerId, idemKey);

        // -> Idemkey run , if duplicate resolve early
        try {
            return writer.save(transaction);
        } catch (DataIntegrityViolationException dup){
            log.info(
                    "Duplicate payment confirmation ignored | thread={} | orderId={} | idemKey={}",
                    Thread.currentThread().getName(),
                    transaction.getOrder().getOrderId(),
                    transaction.getIdempotencyKey()
            );
            throw dup;
        }
    }

    public void auditSuccess(UUID customerId, String message){
        validate.auditSuccess(customerId, message);
    }

    public String getPaymentMethod(DTOTransactionRequest dto){
        try {
            if (dto.instruction() instanceof UseNewCard card){
                return "USE_NEW_CARD";
            } else if (dto.instruction() instanceof UseSavedMethod card) {
                return "USE_SAVED_METHOD";
            }
            throw new IllegalStateException("Unsupported payment method");
        } catch (IllegalStateException ex){
            throw ex;
        }
    }

}
