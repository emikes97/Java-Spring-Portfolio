package commerse.eshop.core.service.async.internal;


import commerse.eshop.core.events.PaymentSucceededOrFailed;
import commerse.eshop.core.model.entity.Order;
import commerse.eshop.core.model.entity.consts.EndpointsNameMethods;
import commerse.eshop.core.model.entity.enums.AuditingStatus;
import commerse.eshop.core.model.entity.enums.OrderStatus;
import commerse.eshop.core.repository.OrderRepo;
import commerse.eshop.core.service.AuditingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
    private final AuditingService auditingService;

    @Autowired
    public OrderUpdater(OrderRepo orderRepo, AuditingService auditingService){
        this.orderRepo = orderRepo;
        this.auditingService = auditingService;
    }

    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateOrder(PaymentSucceededOrFailed paymentSucceededOrFailed){

        Order order = orderRepo.findByOrderIdForUpdate(paymentSucceededOrFailed.orderId()).orElseThrow(
                () -> new IllegalStateException("Order wasn't found"));

        if(isTerminal(order.getOrderStatus())){
            log.info("Order {} already terminal ({}) – skipping update.", order.getOrderId(), order.getOrderStatus());
            auditingService.log(order.getCustomer().getCustomerId(), EndpointsNameMethods.UPDATE_ORDER_ASYNC, AuditingStatus.SUCCESSFUL,
                    "Order {" + order.getOrderId() +"} already terminal ({"+ order.getOrderStatus() +"}) – skipping update.");
            return;
        }

        if (order.getOrderStatus() == paymentSucceededOrFailed.status()) {
            log.info("Order {} already in status {} – no-op.", order.getOrderId(), order.getOrderStatus());
            auditingService.log(order.getCustomer().getCustomerId(), EndpointsNameMethods.UPDATE_ORDER_ASYNC, AuditingStatus.SUCCESSFUL,
                    "Order {" + order.getOrderId() +"} already in status ({"+ order.getOrderStatus() +"}) – no-op.");
            return;
        }

        try {
            order.setOrderStatus(paymentSucceededOrFailed.status());
            order.setCompletedAt(paymentSucceededOrFailed.time());
            orderRepo.restoreProductStockFromOrder(order.getOrderId());
            orderRepo.saveAndFlush(order);
            auditingService.log(order.getCustomer().getCustomerId(), EndpointsNameMethods.UPDATE_ORDER_ASYNC, AuditingStatus.SUCCESSFUL, order.getOrderStatus().toString());
        } catch (DataIntegrityViolationException err){
            log.error("[OrderUpdater] Integrity violation updating order {}: {}", order.getOrderId(), err.getMessage(), err);
            auditingService.log(order.getCustomer().getCustomerId(), EndpointsNameMethods.UPDATE_ORDER_ASYNC, AuditingStatus.ERROR,
                    "[OrderUpdater] Integrity violation updating order" + order.getOrderId() + " " + err.toString());
            throw err;
        } catch (Exception exception) {
            log.error("[OrderUpdater] Unexpected error updating order {}. Please update manually", order.getOrderId(), exception);
            auditingService.log(order.getCustomer().getCustomerId(), EndpointsNameMethods.UPDATE_ORDER_ASYNC, AuditingStatus.ERROR,
                    "[OrderUpdater] Unexpected error updating order + " + order.getOrderId() + ". Please update manually, " + exception);
            throw exception;
        }
    }

    private boolean isTerminal(OrderStatus status){
        return status == OrderStatus.CANCELLED || status == OrderStatus.PAID || status == OrderStatus.PAYMENT_FAILED || status == OrderStatus.EXPIRED;
    }
}
