package commerce.eshop.core.service.async.internal;


import commerce.eshop.core.events.PaymentSucceededOrFailed;
import commerce.eshop.core.model.entity.Order;
import commerce.eshop.core.util.CentralAudit;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.util.enums.AuditingStatus;
import commerce.eshop.core.util.enums.OrderStatus;
import commerce.eshop.core.repository.OrderRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.NoSuchElementException;


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
    public void updateOrder(PaymentSucceededOrFailed paymentSucceededOrFailed){

        final Order order;

        try {
            order = orderRepo.findByOrderIdForUpdate(paymentSucceededOrFailed.orderId()).orElseThrow(
                    () -> new IllegalStateException("Order wasn't found"));
        } catch (NoSuchElementException ex){
            throw centralAudit.audit(ex, null, EndpointsNameMethods.UPDATE_ORDER_ASYNC, AuditingStatus.ERROR);
        }


        if(isTerminal(order.getOrderStatus())){
            log.info("Order {} already terminal ({}) – skipping update.", order.getOrderId(), order.getOrderStatus());
            centralAudit.info(order.getCustomer().getCustomerId(), EndpointsNameMethods.UPDATE_ORDER_ASYNC, AuditingStatus.SUCCESSFUL,
                    "Order {" + order.getOrderId() +"} already terminal ({"+ order.getOrderStatus() +"}) – skipping update.");
            return;
        }

        if (order.getOrderStatus() == paymentSucceededOrFailed.status()) {
            log.info("Order {} already in status {} – no-op.", order.getOrderId(), order.getOrderStatus());
            centralAudit.info(order.getCustomer().getCustomerId(), EndpointsNameMethods.UPDATE_ORDER_ASYNC, AuditingStatus.SUCCESSFUL,
                    "Order {" + order.getOrderId() +"} already in status ({"+ order.getOrderStatus() +"}) – no-op.");
            return;
        }

        try {
            order.setOrderStatus(paymentSucceededOrFailed.status());
            order.setCompletedAt(paymentSucceededOrFailed.time());
            orderRepo.restoreProductStockFromOrder(order.getOrderId());
            orderRepo.saveAndFlush(order);
            centralAudit.info(order.getCustomer().getCustomerId(), EndpointsNameMethods.UPDATE_ORDER_ASYNC, AuditingStatus.SUCCESSFUL, order.getOrderStatus().toString());
        } catch (DataIntegrityViolationException err){
            log.error("[OrderUpdater] Integrity violation updating order {}: {}", order.getOrderId(), err.getMessage(), err);
            throw centralAudit.audit(err,order.getCustomer().getCustomerId(), EndpointsNameMethods.UPDATE_ORDER_ASYNC, AuditingStatus.ERROR,
                    "[OrderUpdater] Integrity violation updating order" + order.getOrderId() + " " + err);
        } catch (Exception ex) {
            log.error("[OrderUpdater] Unexpected error updating order {}. Please update manually", order.getOrderId(), ex);
            throw centralAudit.audit(new RuntimeException("Unexpected Error"), order.getCustomer().getCustomerId(), EndpointsNameMethods.UPDATE_ORDER_ASYNC, AuditingStatus.ERROR,
                    "[OrderUpdater] Unexpected error updating order + " + order.getOrderId() + ". Please update manually");
        }
    }

    private boolean isTerminal(OrderStatus status){
        return status == OrderStatus.CANCELLED || status == OrderStatus.PAID || status == OrderStatus.PAYMENT_FAILED || status == OrderStatus.EXPIRED;
    }
}
