package commerce.eshop.core.application.cart.validation;

import commerce.eshop.core.util.CentralAudit;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.util.enums.AuditingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuditedCartValidation {

    // == Fields ==
    private final CentralAudit centralAudit;

    // == Constructors ==
    @Autowired
    public AuditedCartValidation(CentralAudit centralAudit) {
        this.centralAudit = centralAudit;
    }

    // == Public Methods ==

    public void checkValidQuantity(int quantity, UUID customerId, int MAX_QTY){
        if (quantity <= 0 || quantity > MAX_QTY){
            throw centralAudit.audit(new IllegalArgumentException("Quantity must be positive, and shouldn't exceed 99"), customerId,
                    EndpointsNameMethods.CART_ADD_ITEM, AuditingStatus.ERROR);
        }
    }

    public void checkValidQuantity(int quantity, UUID customerId){
        if (quantity <= 0) {
            throw centralAudit.audit(new IllegalArgumentException("Quantity must be positive."), customerId,
                    EndpointsNameMethods.CART_REMOVE, AuditingStatus.ERROR);
        }
    }


}
