package commerce.eshop.core.service.Impl;

import commerce.eshop.core.model.entity.Cart;
import commerce.eshop.core.model.entity.CartItem;
import commerce.eshop.core.model.entity.Product;
import commerce.eshop.core.util.CentralAudit;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.util.enums.AuditMessage;
import commerce.eshop.core.util.enums.AuditingStatus;
import commerce.eshop.core.repository.CartItemRepo;
import commerce.eshop.core.repository.CartRepo;
import commerce.eshop.core.repository.ProductRepo;
import commerce.eshop.core.service.CartService;
import commerce.eshop.core.util.SortSanitizer;
import commerce.eshop.core.web.dto.response.Cart.DTOCartItemResponse;
import commerce.eshop.core.web.mapper.CartServiceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
public class CartServiceImpl implements CartService {

    // == Constants ==
    private static final int MAX_QTY = 99;

    // == Fields ==
    private final CartItemRepo cartItemRepo;
    private final CartRepo cartRepo;
    private final ProductRepo productRepo;
    private final CentralAudit centralAudit;
    private final CartServiceMapper cartServiceMapper;
    private final SortSanitizer sortSanitizer;

    // == Whitelisting & Constraints
    public static final Map<String, String> CART_ITEMS_SORT_WHITELIST = Map.ofEntries(
            Map.entry("added_at",   "addedAt"),
            Map.entry("quantity",   "quantity"),
            Map.entry("unit_price", "priceAt"),
            Map.entry("product_name","productName")
    );

    // == Constructors ==
    @Autowired
    public CartServiceImpl(CartItemRepo cartItemRepo, CartRepo cartRepo, ProductRepo productRepo, CentralAudit centralAudit,
                           CartServiceMapper cartServiceMapper, SortSanitizer sortSanitizer){
        this.cartItemRepo = cartItemRepo;
        this.cartRepo = cartRepo;
        this.productRepo = productRepo;
        this.centralAudit = centralAudit;
        this.cartServiceMapper = cartServiceMapper;
        this.sortSanitizer = sortSanitizer;
    }

    // == Public Methods ==
    @Transactional(readOnly = true)
    @Override
    public Page<DTOCartItemResponse> viewAllCartItems(UUID customerId, Pageable pageable) {

        Pageable p = sortSanitizer.sanitize(pageable, CART_ITEMS_SORT_WHITELIST, 25);
        final Cart cart = getCartOrThrow(customerId, EndpointsNameMethods.CART_VIEW_ALL);
        Page<CartItem> items = cartItemRepo.findByCart_CartId(cart.getCartId(), p);
        centralAudit.info(customerId, EndpointsNameMethods.CART_VIEW_ALL, AuditingStatus.SUCCESSFUL, AuditMessage.CART_VIEW_ALL_SUCCESS.getMessage());
        return items.map(cartServiceMapper::toDto);
    }

    @Transactional(readOnly = true)
    @Override
    public DTOCartItemResponse findItem(UUID customerId, long productId) {

        final Cart cart = getCartOrThrow(customerId, EndpointsNameMethods.CART_FIND_ITEM);
        final CartItem cartItem = getCartItemOrThrow(cart.getCartId(), productId, customerId, EndpointsNameMethods.CART_FIND_ITEM);
        centralAudit.info(customerId, EndpointsNameMethods.CART_FIND_ITEM, AuditingStatus.SUCCESSFUL, AuditMessage.CART_FIND_ITEM_SUCCESS.getMessage());
        return cartServiceMapper.toDto(cartItem);
    }

    @Transactional
    @Override
    public DTOCartItemResponse addCartItem(UUID customerId, long productId, int quantity) {

        if (quantity <= 0 || quantity > MAX_QTY){
            throw centralAudit.audit(new IllegalArgumentException("Quantity must be positive, and shouldn't exceed 99"), customerId,
                    EndpointsNameMethods.CART_ADD_ITEM, AuditingStatus.ERROR);
        }

        final Product product = getProductOrThrow(productId, customerId, EndpointsNameMethods.CART_ADD_ITEM);
        final Cart cart = getCartOrThrow(customerId, EndpointsNameMethods.CART_ADD_ITEM);

        int updated = cartItemRepo.bumpQuantity(cart.getCartId(), productId, quantity, MAX_QTY);

        CartItem item;

        if (updated == 0) {
            // no row existed → insert
            item = new CartItem(cart, product, product.getProductName(), quantity, product.getPrice());
            try {
                cartItemRepo.saveAndFlush(item);
            } catch (DataIntegrityViolationException dup) {
                // another thread inserted first → bump again
                cartItemRepo.bumpQuantity(cart.getCartId(), productId, quantity, MAX_QTY);
                item = cartItemRepo.getCartItemByCartIdAndProductId(cart.getCartId(), productId).orElseThrow();
            }
        } else {
            item = cartItemRepo.getCartItemByCartIdAndProductId(cart.getCartId(), productId).orElseThrow();
        }

        centralAudit.info(customerId, EndpointsNameMethods.CART_ADD_ITEM, AuditingStatus.SUCCESSFUL,
                AuditMessage.CART_ADD_ITEM_SUCCESS.getMessage());

        return cartServiceMapper.toDto(item);
    }

    @Transactional
    @Override
    public void removeCartItem(UUID customerId, long productId, Integer quantity) {

        final Cart cart = getCartOrThrow(customerId, EndpointsNameMethods.CART_REMOVE);

        final CartItem cartItem = getCartItemOrThrow(cart.getCartId(), productId, customerId, EndpointsNameMethods.CART_REMOVE);

        if (quantity == null || cartItem.getQuantity() <= quantity) {
            cartItemRepo.deleteItemByCartIdAndProductId(cart.getCartId(), productId);
            centralAudit.info(customerId, EndpointsNameMethods.CART_REMOVE, AuditingStatus.SUCCESSFUL, AuditMessage.CART_REMOVE_SUCCESS.getMessage());
            return;
        }

        if (quantity <= 0) {
            throw centralAudit.audit(new IllegalArgumentException("Quantity must be positive."), customerId,
                    EndpointsNameMethods.CART_REMOVE, AuditingStatus.ERROR);
        }

        cartItem.setQuantity(cartItem.getQuantity() - quantity);

        try {
            cartItemRepo.saveAndFlush(cartItem);
            centralAudit.info(customerId, EndpointsNameMethods.CART_REMOVE, AuditingStatus.SUCCESSFUL, AuditMessage.CART_REMOVE_SUCCESS.getMessage());
        }catch (DataIntegrityViolationException dup){
            throw centralAudit.audit(dup, customerId, EndpointsNameMethods.CART_REMOVE, AuditingStatus.ERROR);
        }
    }

    @Transactional
    @Override
    public void clearCart(UUID customerId) {

        final Cart cart = getCartOrThrow(customerId, EndpointsNameMethods.CART_CLEAR);

        try {
            cartItemRepo.clearCart(cart.getCartId());
            centralAudit.info(customerId, EndpointsNameMethods.CART_CLEAR, AuditingStatus.SUCCESSFUL, AuditMessage.CART_CLEAR_SUCCESS.getMessage());
        } catch (DataIntegrityViolationException dup){
            throw centralAudit.audit(dup, customerId, EndpointsNameMethods.CART_CLEAR, AuditingStatus.ERROR, dup.toString());
        }

    }

    // == Private Methods ==

    private Cart getCartOrThrow(UUID customerId, String method){
        try {
            final Cart cart = cartRepo.findCartByCustomerId(customerId).orElseThrow(() -> new NoSuchElementException("Cart doesn't exist"));
            return cart;
        } catch (NoSuchElementException e){
            throw centralAudit.audit(e,customerId, method, AuditingStatus.ERROR, e.toString());
        }
    }

    private Product getProductOrThrow(long productId, UUID customerId, String method){
        try {
            final Product product = productRepo.findById(productId).orElseThrow(() -> new NoSuchElementException("Product doesn't exist."));
            return product;
        } catch (NoSuchElementException e){
            throw centralAudit.audit(e, customerId, method, AuditingStatus.ERROR, e.toString());
        }
    }

    private CartItem getCartItemOrThrow(UUID cartId, long productId, UUID customerId, String method ){
        try{
           final CartItem cartItem = cartItemRepo.getCartItemByCartIdAndProductId(cartId, productId).orElseThrow(
                    () -> new NoSuchElementException("Cart Item doesn't exist"));
           return cartItem;
        } catch (NoSuchElementException e){
            throw centralAudit.audit(e, customerId, method, AuditingStatus.ERROR, e.toString());
        }
    }
}
