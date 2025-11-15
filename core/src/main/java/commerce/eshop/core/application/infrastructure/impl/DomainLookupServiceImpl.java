package commerce.eshop.core.application.infrastructure.impl;

import commerce.eshop.core.application.infrastructure.domain.*;
import commerce.eshop.core.model.entity.*;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@Transactional(readOnly = true)
public class DomainLookupServiceImpl implements DomainLookupService {

    // == Fields ==
    private final CustomerDomain customerDomain;
    private final CartDomain cartDomain;
    private final WishlistDomain wishlistDomain;
    private final ProductCategoryDomain productCategoryDomain;
    private final OrderDomain orderDomain;

    // == Constructors ==
    public DomainLookupServiceImpl(
            CustomerDomain customerDomain,
            CartDomain cartDomain,
            WishlistDomain wishlistDomain,
            ProductCategoryDomain productCategoryDomain,
            OrderDomain orderDomain
    ) {
        this.customerDomain = customerDomain;
        this.cartDomain = cartDomain;
        this.wishlistDomain = wishlistDomain;
        this.productCategoryDomain = productCategoryDomain;
        this.orderDomain = orderDomain;
    }

// == Public Methods ==

    /* ===========================================
     *                CUSTOMER DOMAIN
     * =========================================== */

    @Override
    public Customer getCustomerOrThrow(UUID customerId, String method) {
        return customerDomain.retrieveCustomer(customerId, method);
    }

    @Override
    public Customer getCustomerByPhoneOrEmailOrThrow(String key, String method){
        return customerDomain.retrieveCustomerByPhoneOrMail(key, method);
    }

    // --- Addresses (ownership enforced) ---
    @Override
    public CustomerAddress getCustomerAddrOrThrow(UUID customerId, long id, String method) {
        return customerDomain.retrieveCustomerAddress(customerId, id, method);
    }

    @Override
    public CustomerAddress getCustomerAddrOrThrow(UUID customerId, String method){
        return customerDomain.retrieveDefaultAddress(customerId, method);
    }

    // --- Payment Methods ---
    @Override
    public CustomerPaymentMethod getPaymentMethodOrThrow(UUID customerId, UUID paymentMethodId, String method){
        return customerDomain.retrieveCustomerPaymentMethod(customerId, paymentMethodId, method);
    }

    // --- Customer pageable ---
    @Override
    public Page<CustomerAddress> getPagedCustomerAddresses(UUID customerId, Pageable page){
        return customerDomain.retrievePagedCustomerAddress(customerId, page);
    }

    @Override
    public Page<CustomerPaymentMethod> getPagedPaymentMethods(UUID customerId, Pageable page){
        return customerDomain.retrievePagedCustomerPaymentMethod(customerId, page);
    }


    /* ===========================================
     *                CART DOMAIN
     * =========================================== */

    @Override
    public Cart getCartOrThrow(UUID customerId, String method) {
        return cartDomain.retrieveCart(customerId, method);
    }

    @Override
    public CartItem getCartItemOrThrow(UUID cartId, long productId, UUID customerId, String method) {
        return cartDomain.retrieveCartItem(cartId, productId, customerId, method);
    }

    // --- Cart pageable ---
    @Override
    public Page<CartItem> getPagedCartItems(UUID cartId, Pageable page){
        return cartDomain.retrievedPagedCartItems(cartId, page);
    }


    /* ===========================================
     *                ORDER DOMAIN
     * =========================================== */

    @Override
    public Order getOrderOrThrow(UUID customerId, UUID orderId, String method){
        return orderDomain.retrieveOrder(customerId, orderId, method);
    }

    // --- Order pageable ---
    @Override
    public Page<Order> getPagedOrders(UUID customerId, Pageable page) {
        return orderDomain.retrievePagedOrders(customerId, page);
    }


    /* ===========================================
     *                WISHLIST DOMAIN
     * =========================================== */

    @Override
    public Wishlist getWishlistOrThrow(UUID customerId, String method){
        return wishlistDomain.retrieveWishlist(customerId, method);
    }

    @Override
    public WishlistItem getWishOrThrow(UUID customerId, Wishlist wishlist, long wishId, String method){
        return wishlistDomain.retrieveWishlistItem(customerId, wishlist, wishId, method);
    }

    // --- Wishlist pageable ---
    @Override
    public Page<WishlistItem> getPagedWishItems(UUID wishlist, Pageable page){
        return wishlistDomain.retrievedPagedWishlistItems(wishlist, page);
    }


    /* ===========================================
     *            PRODUCT / CATEGORY DOMAIN
     * =========================================== */

    // --- Product existence ---
    @Override
    public boolean checkIfProductExistsByProductName(String normalisedName){
        return productCategoryDomain.checkIfProductExistsByProductName(normalisedName);
    }

    // --- Product lookup ---
    @Override
    public Product getProductOrThrow(long productId, String method){
        return productCategoryDomain.retrieveProduct(productId, method);
    }

    @Override
    public Product getProductOrThrow(UUID customerId, long productId, String method){
        return productCategoryDomain.retrieveProduct(customerId, productId, method);
    }

    // --- Category ---
    @Override
    public Category getCategoryOrThrow(long categoryId, String method) {
        return productCategoryDomain.retrieveCategory(categoryId, method);
    }

    @Override
    public Boolean checkIfCatExists(String catName){
        return productCategoryDomain.checkIfCatExists(catName);
    }

    // --- Product pageable ---
    @Override
    public Page<Product> getPagedProducts(long categoryId, Pageable pageable) {
        return productCategoryDomain.retrievePagedProductsByCategory(categoryId, pageable);
    }
}