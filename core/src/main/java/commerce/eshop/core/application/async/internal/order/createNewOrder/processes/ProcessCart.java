package commerce.eshop.core.application.async.internal.order.createNewOrder.processes;

import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.order.validation.AuditedOrderValidation;
import commerce.eshop.core.application.order.writer.OrderCartWriter;
import commerce.eshop.core.model.entity.Cart;
import commerce.eshop.core.model.entity.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotSerializeTransactionException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ProcessCart {

    // == Fields ==
    private final DomainLookupService domainLookupService;
    private final OrderCartWriter orderCartWriter;
    private final AuditedOrderValidation validation;

    // == Constructors ==
    @Autowired
    public ProcessCart(DomainLookupService domainLookupService, OrderCartWriter orderCartWriter, AuditedOrderValidation validation) {
        this.domainLookupService = domainLookupService;
        this.orderCartWriter = orderCartWriter;
        this.validation = validation;
    }

    // == Public Methods ==

    public Cart fetchCart(Customer customer){
        return domainLookupService.getCartOrThrow(customer.getCustomerId(), "fetchCart");
    }

    public BigDecimal getTotalOutstanding(Cart cart, Customer customer){
        // ->  serialize SAME-CART checkouts only, guard against other retries
        if (!orderCartWriter.lockCart(cart.getCartId())){
            throw new CannotSerializeTransactionException("cart checkout locked");
        }

        // #1 Sum total_outstanding from Cart_Items;
        BigDecimal total_outstanding = orderCartWriter.sumCartOutstanding(cart.getCartId());
        validation.checkOutstanding(total_outstanding, customer.getCustomerId()); // -> Check total outstanding > 0 / else fail

        return total_outstanding;
    }

    public void reserveStock(Cart cart, Customer customer){
        orderCartWriter.reserveStock(cart.getCartId(), customer.getCustomerId());
    }
}
