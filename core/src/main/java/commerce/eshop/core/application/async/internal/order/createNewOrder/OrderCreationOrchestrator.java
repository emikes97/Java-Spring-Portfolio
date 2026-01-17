package commerce.eshop.core.application.async.internal.order.createNewOrder;

import commerce.eshop.core.application.async.internal.order.createNewOrder.processes.CheckoutClaimedJob;
import commerce.eshop.core.application.async.internal.order.createNewOrder.processes.ProcessCart;
import commerce.eshop.core.application.async.internal.order.createNewOrder.processes.ProcessOrder;
import commerce.eshop.core.application.events.order.PlacedOrderEvent;
import commerce.eshop.core.model.entity.Cart;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.model.entity.Order;
import commerce.eshop.core.model.outbox.CheckoutJob;
import commerce.eshop.core.model.states.CheckoutStates;
import commerce.eshop.core.web.dto.requests.Order.DTOOrderCustomerAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class OrderCreationOrchestrator {

    // == Fields ==
    private final CheckoutClaimedJob claimedJob;
    private final ProcessCart processCart;
    private final ProcessOrder processOrder;
    private final ApplicationEventPublisher publisher;

    // == Constructors ==
    @Autowired
    public OrderCreationOrchestrator(CheckoutClaimedJob claimedJob, ProcessCart processCart, ProcessOrder processOrder,
                                     ApplicationEventPublisher publisher) {
        this.claimedJob = claimedJob;
        this.processCart = processCart;
        this.processOrder = processOrder;
        this.publisher = publisher;
    }

    // == Public Methods ==

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createOrder(long jobId){

        CheckoutJob job = claimedJob.fetchJob(jobId);

        if(job.getState() != CheckoutStates.PROCESSING){
            return;
        }

        Customer customer = claimedJob.fetchCustomer(job.getCustomerId());
        Cart cart = processCart.fetchCart(customer);
        DTOOrderCustomerAddress address = claimedJob.fetchAddress(job);

        // #1 Sum total_outstanding from Cart_Items;
        BigDecimal total_outstanding = processCart.getTotalOutstanding(cart, customer);
        // #2 Decrement quantity of products based on the cart
        processCart.reserveStock(cart, customer);
        // #3 Create Order entity & Save
        Order order = processOrder.process(job, customer, cart, address, total_outstanding);

        PlacedOrderEvent orderEvent = new PlacedOrderEvent(job.getCustomerId(), order.getOrderId(), order.getTotalOutstanding(), "Euro", job.getIdemkey());
        publisher.publishEvent(orderEvent);
    }
}
