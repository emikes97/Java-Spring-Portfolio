package commerce.eshop.core.application.infrastructure;

import commerce.eshop.core.model.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface DomainLookupService {


    // == Customer domain ==

    // --- Customer & Identity ---
    Customer getCustomerOrThrow(UUID customerId, String method);
    Customer getCustomerByPhoneOrEmailOrThrow(String key, String method);

    // --- Addresses (ownership enforced) ---
    CustomerAddress getCustomerAddrOrThrow(UUID customerId, long id, String method);
    CustomerAddress getCustomerAddrOrThrow(UUID customerId, String method); // default address

    // --- Payment Methods (ownership enforced) ---
    CustomerPaymentMethod getPaymentMethodOrThrow(UUID customerId, UUID paymentMethodId, String method);

    // --- Customer pageable ---
    Page<CustomerAddress> getPagedCustomerAddresses(UUID customerId, Pageable page);
    Page<CustomerPaymentMethod> getPagedPaymentMethods(UUID customerId, Pageable page);


    // == Cart domain ==

    // --- Cart & Items (ownership enforced) ---
    Cart getCartOrThrow(UUID customerId, String method);
    CartItem getCartItemOrThrow(UUID cartId, long productId, UUID customerId, String method);

    // --- Cart pageable ---
    Page<CartItem> getPagedCartItems(UUID cartId, Pageable page);


    // == Orders domain ==

    // --- Orders (ownership enforced) ---
    Order getOrderOrThrow(UUID customerId, UUID orderId, String method);

    // --- Orders pageable ---
    Page<Order> getPagedOrders(UUID customerId, Pageable page);


    // == Wishlist domain ==

    // --- Wishlist (ownership enforced) ---
    Wishlist getWishlistOrThrow(UUID customerId, String method);
    WishlistItem getWishOrThrow(UUID customerId, Wishlist wishlist, long wishId, String method);

    // --- Wishlist pageable ---
    Page<WishlistItem> getPagedWishItems(UUID wishlist, Pageable page);


    // == Product / Catalog domain ==

    // --- Product existence ---
    boolean checkIfProductExistsByProductName(String name);

    // --- Product (ownership optional) ---
    Product getProductOrThrow(long productId, String method); // Product service overload
    Product getProductOrThrow(UUID customerId, long productId, String method);

    // --- Category ---
    Category getCategoryOrThrow(long categoryId, String method);
    Boolean checkIfCatExists(String catName);

    // --- Product pageable ---
    Page<Product> getPagedProducts(long categoryId, Pageable pageable);
}