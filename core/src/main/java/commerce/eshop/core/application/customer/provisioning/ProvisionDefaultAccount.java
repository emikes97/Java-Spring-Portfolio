package commerce.eshop.core.application.customer.provisioning;

import commerce.eshop.core.events.customer.CustomerRegisteredEvent;
import commerce.eshop.core.model.entity.Cart;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.model.entity.Wishlist;
import commerce.eshop.core.repository.CartRepo;
import commerce.eshop.core.repository.CustomerRepo;
import commerce.eshop.core.repository.WishlistRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.NoSuchElementException;

@Slf4j
@Component
public class ProvisionDefaultAccount {

    // == Fields ==
    private final WishlistRepo wishlistRepo;
    private final CartRepo cartRepo;
    private final CustomerRepo customerRepo;

    // == Constructors ==

    public ProvisionDefaultAccount(WishlistRepo wishlistRepo, CartRepo cartRepo, CustomerRepo customerRepo) {
        this.wishlistRepo = wishlistRepo;
        this.cartRepo = cartRepo;
        this.customerRepo = customerRepo;
    }

    // == Public Methods ==
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(CustomerRegisteredEvent event){

        try {
            Customer customer = customerRepo.findById(event.customerId()).orElseThrow(
                    () -> new NoSuchElementException("No customer exists with customerId =" + event.customerId())
            );

            if (cartRepo.findByCustomerCustomerId(customer.getCustomerId()).isEmpty()){
                cartRepo.save(new Cart(customer));
            }

            if (wishlistRepo.findWishlistByCustomerId(customer.getCustomerId()).isEmpty()){
                wishlistRepo.save(new Wishlist(customer));
            }
        } catch (NoSuchElementException ex){
            log.warn("Customer with id" + event.customerId() + " doesn't exist");
        }
    }
}
