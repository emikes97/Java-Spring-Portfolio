package commerce.eshop.core.application.order.validation;

import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import commerce.eshop.core.application.util.enums.OrderStatus;
import commerce.eshop.core.web.dto.requests.Order.DTOOrderCustomerAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Component
public class AuditedOrderValidation {

    // == Fields ==
    private final CentralAudit centralAudit;

    // == Constructors ==
    @Autowired
    public AuditedOrderValidation(CentralAudit centralAudit) {
        this.centralAudit = centralAudit;
    }

    // == Public Methods ==
    public void checkCustomerId(UUID customerId){
        if(customerId == null){
            IllegalArgumentException illegal = new IllegalArgumentException("The identification key can't be empty");
            throw centralAudit.audit(illegal, null, EndpointsNameMethods.ORDER_PLACE, AuditingStatus.WARNING, illegal.toString());
        }
    }

    public boolean checkAddressDto(DTOOrderCustomerAddress addressDto){
        return addressDto == null;
    }

    public void checkOutstanding(BigDecimal total_outstanding, UUID customerId){
        if(Objects.isNull(total_outstanding) || total_outstanding.compareTo(BigDecimal.ZERO) <= 0 ){
            IllegalStateException illegal = new IllegalStateException("The cart is empty");
            throw centralAudit.audit(illegal, customerId, EndpointsNameMethods.ORDER_PLACE, AuditingStatus.WARNING, illegal.toString());
        }
    }

    public void checkCustomerAndOrder(UUID customerId, UUID orderId){
        if (customerId == null || orderId == null) {
            IllegalArgumentException bad = new IllegalArgumentException("Missing customerId/orderId.");
            throw centralAudit.audit(bad, customerId, EndpointsNameMethods.ORDER_CANCEL, AuditingStatus.WARNING, "MISSING_IDS");
        }
    }

    public void checkOrderStatus(OrderStatus status, UUID customerId){
        if(status != OrderStatus.PENDING_PAYMENT){
            IllegalStateException illegal = new IllegalStateException("INVALID_STATE" + status);
            throw centralAudit.audit(illegal, customerId, EndpointsNameMethods.ORDER_CANCEL, AuditingStatus.WARNING, illegal.toString());
        }
    }

    public void checkExpectedUpdated(int expected, UUID customerId){
        if (expected == 0) {
            throw centralAudit.audit(new ResponseStatusException(HttpStatus.CONFLICT, "ORDER_ITEMS_EMPTY_OR_MISSING"), customerId,
                    EndpointsNameMethods.ORDER_CANCEL, AuditingStatus.WARNING, "ORDER_ITEMS_EMPTY_OR_MISSING");
        }
    }

    public void checkRestockUpdate(int updated, int expected, UUID customerId){
        if (updated != expected){
            /// Client error, The product was out-of-stock when order was placed.
            ResponseStatusException err = new ResponseStatusException(HttpStatus.CONFLICT, "ORDER_ITEMS_EMPTY_OR_MISSING");
            throw centralAudit.audit(err, customerId, EndpointsNameMethods.ORDER_CANCEL, AuditingStatus.WARNING, "ORDER_ITEMS_EMPTY_OR_MISSING");
        }
    }
}
