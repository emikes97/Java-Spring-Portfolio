package commerce.eshop.core.application.order.writer;


import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import commerce.eshop.core.model.entity.Order;
import commerce.eshop.core.repository.OrderItemRepo;
import commerce.eshop.core.repository.OrderRepo;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrderWriter {

    // == Fields ==
    private final OrderRepo oRepo;
    private final OrderItemRepo oiRepo;
    private final CentralAudit centralAudit;

    // == Constructors ==

    public OrderWriter(OrderRepo oRepo, OrderItemRepo oiRepo, CentralAudit centralAudit) {
        this.oRepo = oRepo;
        this.oiRepo = oiRepo;
        this.centralAudit = centralAudit;
    }

    // == Public Methods ==

    /** Save for Order Placement only **/
    public Order save(Order order, UUID cartId, UUID customerId){
        try {
            order = oRepo.saveAndFlush(order);
            // Snapshot cart items to order_items
            oiRepo.snapShotFromCart(order.getOrderId(), cartId);
            oiRepo.clearCart(cartId);
            return order;
        } catch (DataIntegrityViolationException dub){
            throw centralAudit.audit(dub, customerId, EndpointsNameMethods.ORDER_PLACE, AuditingStatus.ERROR, dub.toString());
        }
    }

    /** Generic case for saves in db **/
    public Order save(Order order, UUID customerId, String endpoint){
        try {
            order = oRepo.saveAndFlush(order);
            return order;
        } catch (DataIntegrityViolationException dup){
            throw centralAudit.audit(dup, customerId, endpoint, AuditingStatus.ERROR, dup.toString());
        }
    }

    public int countOrderItems(UUID orderId){
        return oRepo.countProductRowsToBeUpdated(orderId);
    }

    public int restoreProductStock(UUID orderId){
        return oRepo.restoreProductStockFromOrder(orderId);
    }
}
