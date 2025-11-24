package commerce.eshop.core.application.customerTest.provisioning;

import commerce.eshop.core.application.customer.provisioning.ProvisionDefaultAccount;
import commerce.eshop.core.application.events.customer.CustomerRegisteredEvent;
import commerce.eshop.core.model.entity.Cart;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.model.entity.Wishlist;
import commerce.eshop.core.repository.CartRepo;
import commerce.eshop.core.repository.CustomerRepo;
import commerce.eshop.core.repository.WishlistRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

class ProvisionDefaultAccountTest {

    private WishlistRepo wishlistRepo;
    private CartRepo cartRepo;
    private CustomerRepo customerRepo;
    private ProvisionDefaultAccount provisionDefaultAccount;

    @BeforeEach
    void setUp() {
        wishlistRepo = mock(WishlistRepo.class);
        cartRepo = mock(CartRepo.class);
        customerRepo = mock(CustomerRepo.class);

        provisionDefaultAccount = new ProvisionDefaultAccount(wishlistRepo, cartRepo, customerRepo);
    }

    @Test
    void on() {
        UUID customerId = UUID.randomUUID();
        Customer customer = mock(Customer.class);
        when(customer.getCustomerId()).thenReturn(customerId);

        CustomerRegisteredEvent event = mock(CustomerRegisteredEvent.class);
        when(event.customerId()).thenReturn(customerId);

        when(customerRepo.findById(customerId)).thenReturn(Optional.of(customer));
        when(cartRepo.findByCustomerCustomerId(customerId)).thenReturn(Optional.empty());
        when(wishlistRepo.findWishlistByCustomerId(customerId)).thenReturn(Optional.empty());

        provisionDefaultAccount.on(event);

        verify(customerRepo).findById(customerId);
        verify(cartRepo).findByCustomerCustomerId(customerId);
        verify(wishlistRepo).findWishlistByCustomerId(customerId);

        verify(cartRepo).save(any(Cart.class));
        verify(wishlistRepo).save(any(Wishlist.class));

        verifyNoMoreInteractions(customerRepo, cartRepo, wishlistRepo);
    }

    @Test
    void on_customerExists_cartAndWishlistAlreadyPresent_doesNotCreateNew() {
        UUID customerId = UUID.randomUUID();
        Customer customer = mock(Customer.class);
        when(customer.getCustomerId()).thenReturn(customerId);

        CustomerRegisteredEvent event = mock(CustomerRegisteredEvent.class);
        when(event.customerId()).thenReturn(customerId);
        when(customerRepo.findById(customerId)).thenReturn(Optional.of(customer));

        when(cartRepo.findByCustomerCustomerId(customerId)).thenReturn(Optional.of(mock(Cart.class)));
        when(wishlistRepo.findWishlistByCustomerId(customerId)).thenReturn(Optional.of(mock(Wishlist.class)));

        provisionDefaultAccount.on(event);

        verify(customerRepo).findById(customerId);
        verify(cartRepo).findByCustomerCustomerId(customerId);
        verify(wishlistRepo).findWishlistByCustomerId(customerId);

        // No new saves
        verify(cartRepo, never()).save(any(Cart.class));
        verify(wishlistRepo, never()).save(any(Wishlist.class));

        verifyNoMoreInteractions(customerRepo, cartRepo, wishlistRepo);
    }

    @Test
    void on_customerDoesNotExist_logsWarningAndDoesNothingElse() {
        UUID customerId = UUID.randomUUID();

        CustomerRegisteredEvent event = mock(CustomerRegisteredEvent.class);
        when(event.customerId()).thenReturn(customerId);

        when(customerRepo.findById(customerId)).thenReturn(Optional.empty());

        provisionDefaultAccount.on(event);

        verify(customerRepo).findById(customerId);

        verifyNoInteractions(cartRepo, wishlistRepo);
        verifyNoMoreInteractions(customerRepo);
    }
}