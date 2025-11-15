package commerce.eshop.core.application.async.internal;


import commerce.eshop.core.application.events.PaymentSucceededOrFailed;
import commerce.eshop.core.model.entity.Order;
import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import commerce.eshop.core.application.util.enums.OrderStatus;
import commerce.eshop.core.repository.OrderRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Component
@Slf4j
public class OrderUpdater {

    private final OrderRepo orderRepo;
    private final CentralAudit centralAudit;

    @Autowired
    public OrderUpdater(OrderRepo orderRepo, CentralAudit centralAudit){
        this.orderRepo = orderRepo;
        this.centralAudit = centralAudit;
    }

    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateOrder(PaymentSucceededOrFailed evt) {

        final Order order = orderRepo.findByOrderIdForUpdate(evt.orderId())
                .orElseThrow(() -> centralAudit.audit(
                        new IllegalStateException("Order wasn't found"), null,
                        EndpointsNameMethods.UPDATE_ORDER_ASYNC, AuditingStatus.ERROR));

        // If already terminal, ignore
        if (isTerminal(order.getOrderStatus())) {
            log.info("Order {} already terminal ({}), skipping.", order.getOrderId(), order.getOrderStatus());
            return;
        }

        // No-op if already at requested status
        if (order.getOrderStatus() == evt.status()) {
            log.info("Order {} already in status {}, no-op.", order.getOrderId(), order.getOrderStatus());
            return;
        }

        // Apply transition
        switch (evt.status()) {
            case PAID -> {
                // ✅ Do NOT restore stock on success
                order.setOrderStatus(OrderStatus.PAID);
                order.setCompletedAt(evt.time());
            }
            case PAYMENT_FAILED, CANCELLED, EXPIRED -> {
                // ✅ Only here we put stock back
                orderRepo.restoreProductStockFromOrder(order.getOrderId());
                order.setOrderStatus(evt.status());
                order.setCompletedAt(evt.time());
            }
            default -> {
                log.warn("Order {}: unexpected status {} in payment event", order.getOrderId(), evt.status());
                return;
            }
        }

        orderRepo.saveAndFlush(order);
    }

    private boolean isTerminal(OrderStatus status){
        return status == OrderStatus.CANCELLED || status == OrderStatus.PAID || status == OrderStatus.PAYMENT_FAILED || status == OrderStatus.EXPIRED;
    }
}
