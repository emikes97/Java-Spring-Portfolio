package commerse.eshop.core.service.Impl;

import commerse.eshop.core.model.entity.Cart;
import commerse.eshop.core.model.entity.CartItem;
import commerse.eshop.core.model.entity.Product;
import commerse.eshop.core.model.entity.consts.EndpointsNameMethods;
import commerse.eshop.core.model.entity.enums.AuditMessage;
import commerse.eshop.core.model.entity.enums.AuditingStatus;
import commerse.eshop.core.repository.CartItemRepo;
import commerse.eshop.core.repository.CartRepo;
import commerse.eshop.core.repository.ProductRepo;
import commerse.eshop.core.service.AuditingService;
import commerse.eshop.core.service.CartService;
import commerse.eshop.core.util.SortSanitizer;
import commerse.eshop.core.web.dto.response.Cart.DTOCartItemResponse;
import commerse.eshop.core.web.mapper.CartServiceMapper;
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
    private final AuditingService auditingService;
    private final CartServiceMapper cartServiceMapper;
    private final SortSanitizer sortSanitizer;

    // == Whitelisting & Constraints
    public static final Map<String, String> CART_ITEMS_SORT_WHITELIST = Map.ofEntries(
            Map.entry("added_at",   "addedAt"),
            Map.entry("quantity",   "quantity"),
            Map.entry("unit_price", "unitPrice"),
            Map.entry("total_price","totalPrice"),
            Map.entry("product_name","productName")
    );

    // == Constructors ==
    @Autowired
    public CartServiceImpl(CartItemRepo cartItemRepo, CartRepo cartRepo, ProductRepo productRepo, AuditingService auditingService,
                           CartServiceMapper cartServiceMapper, SortSanitizer sortSanitizer){
        this.cartItemRepo = cartItemRepo;
        this.cartRepo = cartRepo;
        this.productRepo = productRepo;
        this.auditingService = auditingService;
        this.cartServiceMapper = cartServiceMapper;
        this.sortSanitizer = sortSanitizer;
    }

    // == Public Methods ==
    @Transactional(readOnly = true)
    @Override
    public Page<DTOCartItemResponse> viewAllCartItems(UUID customerId, Pageable pageable) {
        try{
            Pageable p = sortSanitizer.sanitize(pageable, CART_ITEMS_SORT_WHITELIST, 25);
            Page<CartItem> items = cartItemRepo.findByCart_CartId(cartRepo.findCartIdByCustomerId(customerId).orElseThrow(() ->
                    new NoSuchElementException("Cart not found for customer " + customerId)),p);
            auditingService.log(customerId, EndpointsNameMethods.CART_VIEW_ALL, AuditingStatus.SUCCESSFUL, AuditMessage.CART_VIEW_ALL_SUCCESS.getMessage());
            return items.map(cartServiceMapper::toDto);
        } catch (NoSuchElementException e){
            auditingService.log(customerId, EndpointsNameMethods.CART_VIEW_ALL, AuditingStatus.ERROR, e.toString());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    @Override
    public DTOCartItemResponse findItem(UUID customerId, long productId) {
        try {
            UUID cart = cartRepo.findCartIdByCustomerId(customerId).orElseThrow(
                    () -> new NoSuchElementException("Cart doesn't exist"));
            auditingService.log(customerId, EndpointsNameMethods.CART_FIND_ITEM, AuditingStatus.SUCCESSFUL, AuditMessage.CART_FIND_ITEM_SUCCESS.name());
            return cartServiceMapper.toDto(cartItemRepo.getCartItemByCartIdAndProductId(cart, productId).orElseThrow(() -> new NoSuchElementException("Product doesn't exist")));
        } catch (NoSuchElementException e){
            auditingService.log(customerId, EndpointsNameMethods.CART_FIND_ITEM, AuditingStatus.ERROR, e.toString());
            throw e;
        }
    }

    @Transactional
    @Override
    public DTOCartItemResponse addCartItem(UUID customerId, long productId, int quantity) {

        if (quantity <= 0 || quantity > MAX_QTY){
            auditingService.log(customerId, EndpointsNameMethods.CART_ADD_ITEM, AuditingStatus.ERROR,"Quantity must be positive, and shouldn't exceed 99");
            throw new IllegalArgumentException("Quantity must be positive, and shouldn't exceed 99");
        }

        final Product product;

        try {
            product = productRepo.findById(productId).orElseThrow(() -> new NoSuchElementException("Product doesn't exist."));
        } catch (NoSuchElementException e){
            auditingService.log(customerId, EndpointsNameMethods.CART_ADD_ITEM, AuditingStatus.ERROR, e.toString());
            throw e;
        }

        final Cart cart;

        try {
            cart = cartRepo.findCartByCustomerId(customerId).orElseThrow(() -> new NoSuchElementException("Cart doesn't exist for the provided UUID"));
        } catch (NoSuchElementException e){
            auditingService.log(customerId, EndpointsNameMethods.CART_ADD_ITEM, AuditingStatus.ERROR, e.toString());
            throw e;
        }

        CartItem cartItem;

        try{
            cartItem = new CartItem(cart, product, product.getProductName(), Math.min(quantity, MAX_QTY), product.getPrice());
            cartItemRepo.saveAndFlush(cartItem); // forcing constraint check
            auditingService.log(customerId, EndpointsNameMethods.CART_ADD_ITEM, AuditingStatus.SUCCESSFUL, AuditMessage.CART_ADD_ITEM_SUCCESS.getMessage());
            return cartServiceMapper.toDto(cartItem);
        } catch (DataIntegrityViolationException dup){
            log.warn("Duplicate exception occurred",dup);
            try{
            cartItem = cartItemRepo.getCartItemForUpdate(cart.getCartId(), productId).orElseThrow(
                    () -> new NoSuchElementException("The requested product doesn't exist")
            );
            } catch(NoSuchElementException e){
                auditingService.log(customerId, EndpointsNameMethods.CART_ADD_ITEM, AuditingStatus.ERROR, e.toString());
                log.error("[ERROR] No cart_item found for cartId={}, productId={}", cart.getCartId(), productId, e);
                throw e;
            }
            cartItem.setQuantity(Math.min(cartItem.getQuantity() + quantity, 99));
            cartItemRepo.save(cartItem);
            auditingService.log(customerId, EndpointsNameMethods.CART_ADD_ITEM, AuditingStatus.WARNING, dup.toString() + "Added + Quantity = " +quantity);
            return cartServiceMapper.toDto(cartItem);
        }
    }

    @Transactional
    @Override
    public void removeCartItem(UUID customerId, long productId, Integer quantity) {

        final Cart cart;

        try {
            cart = cartRepo.findCartByCustomerId(customerId).orElseThrow(() -> new NoSuchElementException("Cart doesn't exist"));
        } catch (NoSuchElementException e){
            auditingService.log(customerId, EndpointsNameMethods.CART_REMOVE, AuditingStatus.ERROR, e.toString());
            throw e;
        }

        final CartItem cartItem;

        try{
            cartItem = cartItemRepo.getCartItemByCartIdAndProductId(cart.getCartId(), productId).orElseThrow(
                    () -> new NoSuchElementException("Cart Item doesn't exist"));
        } catch (NoSuchElementException e){
            auditingService.log(customerId, EndpointsNameMethods.CART_REMOVE, AuditingStatus.ERROR, e.toString());
            throw e;
        }

        if (quantity == null || cartItem.getQuantity() <= quantity) {
            auditingService.log(customerId, EndpointsNameMethods.CART_REMOVE, AuditingStatus.SUCCESSFUL, AuditMessage.CART_REMOVE_SUCCESS.getMessage());
            cartItemRepo.deleteItemByCartIdAndProductId(cart.getCartId(), productId);
            return;
        }

        if (quantity <= 0) {
            auditingService.log(customerId, EndpointsNameMethods.CART_REMOVE, AuditingStatus.ERROR, "Quantity must be positive.");
            throw new IllegalArgumentException("Quantity must be positive.");
        }

        cartItem.setQuantity(cartItem.getQuantity() - quantity);

        try {
            cartItemRepo.save(cartItem);
            auditingService.log(customerId, EndpointsNameMethods.CART_REMOVE, AuditingStatus.SUCCESSFUL, AuditMessage.CART_REMOVE_SUCCESS.getMessage());
        }catch (DataIntegrityViolationException dup){
            auditingService.log(customerId, EndpointsNameMethods.CART_REMOVE, AuditingStatus.ERROR, dup.toString());
            throw dup;
        }
    }

    @Transactional
    @Override
    public void clearCart(UUID customerId) {
        final Cart cart;
        try {
            cart = cartRepo.findCartByCustomerId(customerId).orElseThrow(() -> new NoSuchElementException("Cart doesn't exist"));
        } catch (NoSuchElementException e){
            auditingService.log(customerId, EndpointsNameMethods.CART_CLEAR, AuditingStatus.ERROR, e.toString());
            throw e;
        }

        try {
            cartItemRepo.clearCart(cart.getCartId());
            auditingService.log(customerId, EndpointsNameMethods.CART_CLEAR, AuditingStatus.SUCCESSFUL, AuditMessage.CART_CLEAR_SUCCESS.getMessage());
        } catch (DataIntegrityViolationException dup){
            auditingService.log(customerId, EndpointsNameMethods.CART_CLEAR, AuditingStatus.ERROR, dup.toString());
            throw dup;
        }

    }
}
