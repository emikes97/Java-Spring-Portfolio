package commerce.eshop.core.service;

import commerce.eshop.core.model.entity.*;

import java.util.UUID;

public interface DomainLookupService {

    // == Find or throw methods ==
    public CartItem getCartItemOrThrow(UUID cartId, long productId, UUID customerId, String method );
    public Product getProductOrThrow(long productId, UUID customerId, String method);
    public Cart getCartOrThrow(UUID customerId, String method);
    public Category getCategoryOrThrow(long categoryId, String method);
    public CustomerAddress getCustomerAddrOrThrow(UUID customerId, long id, String method);
    public Customer getCustomerOrThrow(UUID customerId, String method);
    public CustomerPaymentMethod getPaymentMethodOrThrow(UUID customerId, UUID paymentMethodId, String method);
    public Customer getCustomerByPhoneOrEmailOrThrow(String key, String method);
    public Order getOrderOrThrow(UUID customerId, UUID orderId, String method);
    public CustomerAddress getCustomerAddrOrThrow(UUID customerId, String method); // Get default address
    public Product getProductOrThrow(long productId, String method); // Product service overload
    public Wishlist getWishlistOrThrow(UUID customerId, String method);
    public WishlistItem getWishOrThrow(UUID customerId, Wishlist wishlist, long wishId, String method);
    public Product getProductOrThrow(UUID customerId, long productId, String method); // Overload for wishlist
}
