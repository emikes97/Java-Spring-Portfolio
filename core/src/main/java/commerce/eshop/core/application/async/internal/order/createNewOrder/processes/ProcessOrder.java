package commerce.eshop.core.application.async.internal.order.createNewOrder.processes;

import commerce.eshop.core.application.checkout.writer.CheckoutWriter;
import commerce.eshop.core.application.order.factory.OrderFactory;
import commerce.eshop.core.application.order.writer.OrderWriter;
import commerce.eshop.core.model.entity.Cart;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.model.entity.Order;
import commerce.eshop.core.model.outbox.CheckoutJob;
import commerce.eshop.core.model.states.CheckoutStates;
import commerce.eshop.core.web.dto.requests.Order.DTOOrderCustomerAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ProcessOrder {

    // == Fields ==
    private final CheckoutWriter checkoutWriter;
    private final OrderWriter orderWriter;
    private final OrderFactory orderFactory;

    // == Constructors ==
    @Autowired
    public ProcessOrder(CheckoutWriter checkoutWriter, OrderWriter orderWriter, OrderFactory orderFactory) {
        this.checkoutWriter = checkoutWriter;
        this.orderWriter = orderWriter;
        this.orderFactory = orderFactory;
    }

    // == Public Methods ==

    public Order process(CheckoutJob job, Customer customer, Cart cart, DTOOrderCustomerAddress address, BigDecimal total_outstanding){
        Order order = orderFactory.handle(job.getOrderId(), customer, address, total_outstanding);

        // Save order and flush for forced check
        Order savedOrder = orderWriter.save(order, cart.getCartId(), job.getCustomerId());
        job.setState(CheckoutStates.ORDER_CREATED);
        checkoutWriter.save(job);

        return savedOrder;
    }

}
