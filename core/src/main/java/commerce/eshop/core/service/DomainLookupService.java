package commerce.eshop.core.service;

import commerce.eshop.core.model.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface DomainLookupService {


    // == Find or throw methods ==

    // --- Customer & Identity ---
    Customer getCustomerOrThrow(UUID customerId, String method);
    Customer getCustomerByPhoneOrEmailOrThrow(String key, String method);

    // --- Cart & Items (ownership enforced) ---
    Cart getCartOrThrow(UUID customerId, String method);
    CartItem getCartItemOrThrow(UUID cartId, long productId, UUID customerId, String method );

    // --- Orders (ownership enforced) ---
    Order getOrderOrThrow(UUID customerId, UUID orderId, String method);

    // --- Addresses (ownership enforced) ---
    CustomerAddress getCustomerAddrOrThrow(UUID customerId, long id, String method);
    CustomerAddress getCustomerAddrOrThrow(UUID customerId, String method); // Get default address

    // --- Payment Methods (ownership enforced) ---
    CustomerPaymentMethod getPaymentMethodOrThrow(UUID customerId, UUID paymentMethodId, String method);

    // --- Wishlist (ownership enforced) ---
    Wishlist getWishlistOrThrow(UUID customerId, String method);
    WishlistItem getWishOrThrow(UUID customerId, Wishlist wishlist, long wishId, String method);

    // --- Catalog ---
    Product getProductOrThrow(UUID customerId, long productId, String method);
    Product getProductOrThrow(long productId, String method); // Product service overload
    Category getCategoryOrThrow(long categoryId, String method);

    // --- Pageable ---
    Page<Order>  getPagedOrders(UUID customerId, Pageable page);
    Page<CartItem> getPagedCartItems(UUID cartId, Pageable page);
    Page<CustomerAddress> getPagedCustomerAddresses(UUID customerId, Pageable page);
    Page<CustomerPaymentMethod> getPagedPaymentMethods(UUID customerId, Pageable page);
    Page<WishlistItem> getPagedWishItems(UUID wishlist, Pageable page);
}