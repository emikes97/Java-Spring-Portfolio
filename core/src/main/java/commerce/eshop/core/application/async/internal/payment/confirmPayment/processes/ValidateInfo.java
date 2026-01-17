package commerce.eshop.core.application.async.internal.payment.confirmPayment.processes;

import commerce.eshop.core.application.transaction.validation.AuditedTransactionValidation;
import commerce.eshop.core.model.entity.Order;
import commerce.eshop.core.web.dto.requests.Transactions.DTOTransactionRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ValidateInfo {

    // == Fields ==
    private final AuditedTransactionValidation validation;

    // == Constructors ==
    public ValidateInfo(AuditedTransactionValidation validation) {
        this.validation = validation;
    }

    // == Public Methods ==
    public void checkTransactionValidity(UUID idemKey, UUID customerId, DTOTransactionRequest dto){
        validation.checkIdemKeyValidity(idemKey.toString(), customerId); // -> fail early in case of damaged/null idemkey
        validation.checkDtoValidity(dto, customerId); // -> fail early in case of broken dto
    }

    public void checkOrderStatus(Order order, UUID customerId){
        validation.checkOrderState(order.getOrderStatus(), customerId);
        validation.checkTotalOutstanding(order.getTotalOutstanding(), customerId);
    }

    public void auditSuccess(UUID customerId, String message){
        validation.auditSuccess(customerId, message);
    }

    public void unSupportedPaymentMethod(UUID customerId){
        validation.unSupportedPaymentMethod(customerId);
    }

}
