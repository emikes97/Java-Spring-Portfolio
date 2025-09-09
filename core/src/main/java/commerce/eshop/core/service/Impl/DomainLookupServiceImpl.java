package commerce.eshop.core.service.Impl;

import commerce.eshop.core.model.entity.*;
import commerce.eshop.core.repository.*;
import commerce.eshop.core.service.DomainLookupService;
import commerce.eshop.core.util.CentralAudit;
import commerce.eshop.core.util.enums.AuditingStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DomainLookupServiceImpl implements DomainLookupService {

    // == Fields ==
    private final CustomerRepo customerRepo;
    private final CartRepo cartRepo;
    private final CartItemRepo cartItemRepo;
    private final OrderRepo orderRepo;
    private final CustomerAddrRepo customerAddrRepo;
    private final CustomerPaymentMethodRepo paymentMethodRepo;
    private final ProductRepo productRepo;
    private final CategoryRepo categoryRepo;
    private final CentralAudit centralAudit;
    private final WishlistRepo wishlistRepo;
    private final WishlistItemRepo wishlistItemRepo;

    // == Constructors ==
    public DomainLookupServiceImpl(
            CustomerRepo customerRepo,
            CartRepo cartRepo,
            CartItemRepo cartItemRepo,
            OrderRepo orderRepo,
            CustomerAddrRepo customerAddrRepo,
            CustomerPaymentMethodRepo paymentMethodRepo,
            ProductRepo productRepo,
            CategoryRepo categoryRepo,
            CentralAudit centralAuditaudit,
            WishlistItemRepo wishlistItemRepo,
            WishlistRepo wishlistRepo
    ) {
        this.customerRepo = customerRepo;
        this.cartRepo = cartRepo;
        this.cartItemRepo = cartItemRepo;
        this.orderRepo = orderRepo;
        this.customerAddrRepo = customerAddrRepo;
        this.paymentMethodRepo = paymentMethodRepo;
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
        this.centralAudit = centralAuditaudit;
        this.wishlistItemRepo = wishlistItemRepo;
        this.wishlistRepo = wishlistRepo;
    }

    // == Public Methods ==

    // --- Customer & Identity ---
    @Override
    public Customer getCustomerOrThrow(UUID customerId, String method) {
        try {
            return customerRepo.findById(customerId).orElseThrow(
                    () -> new NoSuchElementException("Customer doesn't exist")
            );
        } catch (NoSuchElementException e){
            throw centralAudit.audit(e, customerId, method, AuditingStatus.WARNING, e.toString());
        }
    }

    @Override
    public Customer getCustomerByPhoneOrEmailOrThrow(String key, String method){
        try {
            return customerRepo.findByPhoneNumberOrEmail(key)
                    .orElseThrow(() -> new NoSuchElementException("Customer not found for: " + key));
        } catch (NoSuchElementException e) {
            throw centralAudit.audit(e,null, method,
                    AuditingStatus.WARNING, "CUSTOMER_NOT_FOUND:" + key);
        }
    }

    // --- Cart & Items (ownership enforced) ---
    @Override
    public Cart getCartOrThrow(UUID customerId, String method) {
        try {
            return cartRepo.findCartByCustomerId(customerId).orElseThrow(() -> new NoSuchElementException("Cart doesn't exist"));
        } catch (NoSuchElementException e){
            throw centralAudit.audit(e,customerId, method, AuditingStatus.ERROR, e.toString());
        }
    }

    @Override
    public CartItem getCartItemOrThrow(UUID cartId, long productId, UUID customerId, String method) {
        try{
            return cartItemRepo.getCartItemByCartIdAndProductId(cartId, productId).orElseThrow(
                    () -> new NoSuchElementException("Cart Item doesn't exist"));
        } catch (NoSuchElementException e){
            throw centralAudit.audit(e, customerId, method, AuditingStatus.ERROR, e.toString());
        }
    }

    // --- Orders (ownership enforced) ---
    @Override
    public Order getOrderOrThrow(UUID customerId, UUID orderId, String method){
        try {
            return orderRepo.findByCustomer_CustomerIdAndOrderId(customerId, orderId).orElseThrow( () -> new NoSuchElementException("There is no order with the ID=" + orderId));
        } catch (NoSuchElementException e){
            throw centralAudit.audit(e, customerId, method, AuditingStatus.WARNING, e.toString());
        }
    }

    // --- Addresses (ownership enforced) ---
    @Override
    public CustomerAddress getCustomerAddrOrThrow(UUID customerId, long id, String method) {
        try {
            return customerAddrRepo.findById(id).orElseThrow(() -> new NoSuchElementException("The address doesn't exist."));
        } catch (NoSuchElementException e){
            throw centralAudit.audit(e, customerId, method, AuditingStatus.ERROR, e.toString());
        }
    }

    @Override
    // Get default address
    public CustomerAddress getCustomerAddrOrThrow(UUID customerId, String method){
        try {
            return  customerAddrRepo.findByCustomerCustomerIdAndIsDefaultTrue(customerId).orElseThrow(
                    () -> new NoSuchElementException("The customer = " + customerId + " doesn't have a default address and " +
                            "no address has been provided for the order"));
        } catch (NoSuchElementException e){
            throw centralAudit.audit(e, customerId, method, AuditingStatus.WARNING, e.toString());
        }
    }

    // --- Payment Methods (ownership enforced) ---
    @Override
    public CustomerPaymentMethod getPaymentMethodOrThrow(UUID customerId, UUID paymentMethodId, String method){
        try {
            return paymentMethodRepo.findByCustomer_CustomerIdAndCustomerPaymentId(
                    customerId, paymentMethodId).orElseThrow(
                    () -> new NoSuchElementException("The payment method doesn't exist"));
        } catch (NoSuchElementException e){
            throw centralAudit.audit(e, customerId, method, AuditingStatus.ERROR, e.toString());
        }
    }

    // --- Wishlist (ownership enforced) ---
    @Override
    public Wishlist getWishlistOrThrow(UUID customerId, String method){
        try {
            return wishlistRepo.findWishlistByCustomerId(customerId).orElseThrow(
                    () -> new NoSuchElementException("NOT_FOUND_BY_ID")
            );
        } catch (NoSuchElementException e){
            throw centralAudit.audit(e, customerId, method, AuditingStatus.WARNING, e.toString());
        }
    }

    @Override
    public WishlistItem getWishOrThrow(UUID customerId, Wishlist wishlist, long wishId, String method){
        try {
            return wishlistItemRepo.findWish(wishlist.getWishlistId(), wishId).orElseThrow(
                    () -> new NoSuchElementException("WISHLISTED_ITEM_NOT_FOUND")
            );
        } catch (NoSuchElementException e){
            throw centralAudit.audit(e, customerId, method, AuditingStatus.WARNING, e.toString());
        }
    }

    // --- Catalog ---
    @Override
    // get product for product service
    public Product getProductOrThrow(long productId, String method){
        try {
            return productRepo.findById(productId).orElseThrow(() -> new NoSuchElementException("Product with the provided ID doesn't exist"));
        } catch (NoSuchElementException e){
            throw centralAudit.audit(e, null, method, AuditingStatus.WARNING, e.toString());
        }
    }

    @Override
    public Product getProductOrThrow(UUID customerId, long productId, String method){
        try {
            final Product product = productRepo.findById(productId).orElseThrow(
                    () -> new NoSuchElementException("The provided ID doesn't match with any available product.")
            );
            return product;
        } catch (NoSuchElementException e){
            throw centralAudit.audit(e,customerId, method, AuditingStatus.WARNING, e.toString());
        }
    }

    @Override
    public Category getCategoryOrThrow(long categoryId, String method) {
        try{
            return categoryRepo.findById(categoryId).orElseThrow(
                    () -> new NoSuchElementException("The requested category doesn't exist"));
        } catch (NoSuchElementException e){
            throw centralAudit.audit(e, null, method, AuditingStatus.ERROR);
        }
    }
}