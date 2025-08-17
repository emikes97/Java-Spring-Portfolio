package commerse.eshop.core.service.Impl;

import commerse.eshop.core.model.entity.Cart;
import commerse.eshop.core.model.entity.CartItem;
import commerse.eshop.core.model.entity.Product;
import commerse.eshop.core.repository.CartItemRepo;
import commerse.eshop.core.repository.CartRepo;
import commerse.eshop.core.repository.ProductRepo;
import commerse.eshop.core.service.CartService;
import commerse.eshop.core.web.dto.response.Cart.DTOCartItemResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class CartServiceImpl implements CartService {

    // == Constants ==
    private static final int DEFAULT_QUANTITY = 1;

    // == Repos ==
    private final CartItemRepo cartItemRepo;
    private final CartRepo cartRepo;
    private final ProductRepo productRepo;

    @Autowired
    public CartServiceImpl(CartItemRepo cartItemRepo, CartRepo cartRepo, ProductRepo productRepo){
        this.cartItemRepo = cartItemRepo;
        this.cartRepo = cartRepo;
        this.productRepo = productRepo;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<DTOCartItemResponse> viewAllCartItems(UUID customerId, Pageable pageable) {
        return cartItemRepo.findByCart_CartId(cartRepo.findCartIdByCustomerId(customerId).orElseThrow(
                        () -> new NoSuchElementException("Cart not found for customer " + customerId)) , pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    @Override
    public DTOCartItemResponse findItem(UUID customerId, long productId) {
        UUID cart = cartRepo.findCartIdByCustomerId(customerId).orElseThrow(
                () -> new NoSuchElementException("Cart doesn't exist"));
        return toDto(cartItemRepo.getCartItemByCartIdAndProductId(cart, productId).orElseThrow(() -> new NoSuchElementException("Product doesn't exist")));
    }

    @Transactional
    @Override
    public DTOCartItemResponse addCartItem(UUID customerId, long productId) {
        
        Product product = productRepo.findById(productId).orElseThrow(() -> new NoSuchElementException("Product doesn't exist."));
        Cart cart = cartRepo.findCartByCustomerId(customerId).orElseThrow(() -> new NoSuchElementException("Cart doesn't exist for the provided UUID"));
        CartItem cartItem;

        if (cartItemRepo.findIfItemExists(cart.getCartId(), productId)){
            cartItem = cartItemRepo.getCartItemByCartIdAndProductId(cart.getCartId(), productId).orElseThrow(
                    () -> new NoSuchElementException("The requested product doesn't exist")
            );
            cartItem.setQuantity(cartItem.getQuantity() + 1);
        }
        else {
            cartItem = new CartItem(cart, product, product.getProductName(), DEFAULT_QUANTITY, product.getPrice(), OffsetDateTime.now());
        }

        try{
            cartItemRepo.save(cartItem);
        } catch (DataIntegrityViolationException dup){
            cartItem = cartItemRepo.getCartItemByCartIdAndProductId(cart.getCartId(), productId).orElseThrow();
            cartItem.setQuantity(Math.min(cartItem.getQuantity() + 1, 99));
            cartItemRepo.save(cartItem);
        }

        return toDto(cartItem);
    }

    @Transactional
    @Override
    public DTOCartItemResponse addCartItem(UUID customerId, long productId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive.");

        Product product = productRepo.findById(productId).orElseThrow(() -> new NoSuchElementException("Product doesn't exist."));
        Cart cart = cartRepo.findCartByCustomerId(customerId).orElseThrow(() -> new NoSuchElementException("Cart doesn't exist for the provided UUID"));
        CartItem cartItem;

        if (cartItemRepo.findIfItemExists(cart.getCartId(), productId)){
            cartItem = cartItemRepo.getCartItemByCartIdAndProductId(cart.getCartId(), productId).orElseThrow(
                    () -> new NoSuchElementException("The requested product doesn't exist")
            );
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        }
        else {
            cartItem = new CartItem(cart, product, product.getProductName(), quantity, product.getPrice(), OffsetDateTime.now());
        }

        try{
            cartItemRepo.save(cartItem);
        } catch (DataIntegrityViolationException dup){
            cartItem = cartItemRepo.getCartItemByCartIdAndProductId(cart.getCartId(), productId).orElseThrow();
            cartItem.setQuantity(Math.min(cartItem.getQuantity() + quantity, 99));
            cartItemRepo.save(cartItem);
        }

        return toDto(cartItem);
    }

    @Transactional
    @Override
    public void removeCartItem(UUID customerId, long productId) {
        Cart cart = cartRepo.findCartByCustomerId(customerId).orElseThrow(() -> new NoSuchElementException("Cart doesn't exist"));
        cartItemRepo.deleteItemByCartIdAndProductId(cart.getCartId(), productId);
        log.info("Cart Item with productId={} deleted", productId);
    }

    @Transactional
    @Override
    public void removeCartItem(UUID customerId, long productId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive.");
        Cart cart = cartRepo.findCartByCustomerId(customerId).orElseThrow(() -> new NoSuchElementException("Cart doesn't exist"));
        CartItem cartItem = cartItemRepo.getCartItemByCartIdAndProductId(cart.getCartId(), productId).orElseThrow(
                () -> new NoSuchElementException("Cart Item doesn't exist"));

        if (cartItem.getQuantity() <= quantity) {
            removeCartItem(customerId, productId);
            return;
        }

        cartItem.setQuantity(cartItem.getQuantity() - quantity);
        cartItemRepo.save(cartItem);
    }

    @Transactional
    @Override
    public void clearCart(UUID customerId) {
        Cart cart = cartRepo.findCartByCustomerId(customerId).orElseThrow(() -> new NoSuchElementException("Cart doesn't exist"));
        cartItemRepo.clearCart(cart.getCartId());
    }

    private DTOCartItemResponse toDto(CartItem c){
        BigDecimal totalPrice = c.getPriceAt().multiply(BigDecimal.valueOf(c.getQuantity()));
        return new DTOCartItemResponse(
                c.getCartItemId(),
                c.getProduct().getProductId(),
                c.getProductName(),
                c.getQuantity(),
                c.getPriceAt(),
                totalPrice,
                c.getAddedAt()
        );
    }
}
