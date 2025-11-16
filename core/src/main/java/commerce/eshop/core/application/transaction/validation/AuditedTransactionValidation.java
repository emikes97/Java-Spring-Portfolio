package commerce.eshop.core.application.transaction.validation;

import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import commerce.eshop.core.application.util.enums.OrderStatus;
import commerce.eshop.core.application.util.enums.TransactionStatus;
import commerce.eshop.core.model.entity.Transaction;
import commerce.eshop.core.web.dto.requests.Transactions.DTOTransactionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Component
public class AuditedTransactionValidation {

    // == Field ==
    private final CentralAudit centralAudit;

    // == Constructors ==
    @Autowired
    public AuditedTransactionValidation(CentralAudit centralAudit) {
        this.centralAudit = centralAudit;
    }


    // == Public Methods ==

    public void checkIdemKeyValidity(String idemKey, UUID customerId){
        if (idemKey == null || idemKey.isBlank()) {
            ResponseStatusException bad = new ResponseStatusException(HttpStatus.BAD_REQUEST, "MISSING_IDEM_KEY");
            throw centralAudit.audit(bad, customerId, EndpointsNameMethods.TRANSACTION_PAY, AuditingStatus.WARNING, "MISSING_IDEM_KEY");
        }
    }

    public void checkDtoValidity(DTOTransactionRequest dto, UUID customerId){
        if (dto == null || dto.instruction() == null) {
            ResponseStatusException bad = new ResponseStatusException(HttpStatus.BAD_REQUEST, "MISSING_INSTRUCTION");
            throw centralAudit.audit(bad, customerId, EndpointsNameMethods.TRANSACTION_PAY, AuditingStatus.WARNING, "MISSING_INSTRUCTION");
        }
    }

    public void checkOrderState(OrderStatus status, UUID customerId){
        if (status != OrderStatus.PENDING_PAYMENT) {
            IllegalStateException illegal =
                    new IllegalStateException("INVALID_STATE:" + status);
            throw centralAudit.audit(illegal, customerId, EndpointsNameMethods.TRANSACTION_PAY, AuditingStatus.WARNING, illegal.toString());
        }
    }

    public void checkTotalOutstanding(BigDecimal totalOutstanding, UUID customerId){
        if (totalOutstanding == null || totalOutstanding.signum() <= 0) {
            ResponseStatusException bad = new ResponseStatusException(HttpStatus.CONFLICT, "ZERO_OR_NEGATIVE_TOTAL");
            throw centralAudit.audit(bad, customerId, EndpointsNameMethods.TRANSACTION_PAY, AuditingStatus.WARNING, "ZERO_OR_NEGATIVE_TOTAL");
        }
    }

    public void checkIfSameIdemKey(Transaction winner, UUID expectedOrderId){
        if (!Objects.equals(winner.getOrder().getOrderId(), expectedOrderId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Idempotency key reused for a different order");
        }
    }

    public void checkTransactionStatus(Transaction winner){
        if (winner.getStatus() == TransactionStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.ACCEPTED, "Transaction already processing");
        }
    }

    public void unSupportedPaymentMethod(UUID customerId){
        throw centralAudit.audit(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported payment instruction"),
                customerId, EndpointsNameMethods.TRANSACTION_PAY, AuditingStatus.WARNING, "Unsupported payment instruction");
    }

    public void auditSuccess(UUID customerId, String paymentMethod){
        centralAudit.info(customerId, EndpointsNameMethods.TRANSACTION_PAY, AuditingStatus.SUCCESSFUL, paymentMethod);
    }
}
