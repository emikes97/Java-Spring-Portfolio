package commerce.eshop.core.application.infrastructure.domain;

import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.model.entity.Order;
import commerce.eshop.core.model.entity.OrderItem;
import commerce.eshop.core.repository.OrderItemRepo;
import commerce.eshop.core.repository.OrderRepo;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Stream;

@Component
public class OrderDomain {

    // == Fields ==
    private final OrderRepo oRepo;
    private final OrderItemRepo oiRepo;
    private final CentralAudit centralAudit;

    // == Constructors ==
    @Autowired
    public OrderDomain(OrderRepo oRepo, OrderItemRepo oiRepo, CentralAudit centralAudit) {
        this.oRepo = oRepo;
        this.oiRepo = oiRepo;
        this.centralAudit = centralAudit;
    }

    // == Public Methods ==
    public Order retrieveOrder(UUID customerId, UUID orderId, String method){
        try {
            return oRepo.findByCustomer_CustomerIdAndOrderId(customerId, orderId).orElseThrow( () -> new NoSuchElementException("There is no order with the ID=" + orderId));
        } catch (NoSuchElementException e){
            throw centralAudit.audit(e, customerId, method, AuditingStatus.WARNING, e.toString());
        }
    }

    public Stream<OrderItem> retrieveOrderItems(UUID orderId){
        return oiRepo.getOrderItems(orderId).stream();
    }

    // == Paged queries ==
    public Page<Order> retrievePagedOrders(UUID customerId, Pageable page){
        return oRepo.findByCustomer_CustomerId(customerId, page);
    }
}
