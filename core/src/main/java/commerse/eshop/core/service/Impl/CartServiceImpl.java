package commerse.eshop.core.service.Impl;

import commerse.eshop.core.model.entity.Cart;
import commerse.eshop.core.model.entity.CartItem;
import commerse.eshop.core.model.entity.Product;
import commerse.eshop.core.repository.CartItemRepo;
import commerse.eshop.core.repository.CartRepo;
import commerse.eshop.core.repository.ProductRepo;
import commerse.eshop.core.service.CartService;
import commerse.eshop.core.web.dto.response.Cart.DTOCartItemResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

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

    @Override
    public Optional<DTOCartItemResponse> findItem(UUID customerId, long productId) {
        return Optional.empty();
    }

    @Transactional
    @Override
    public DTOCartItemResponse addCartItem(UUID customerId, long productId) {
        
        Product product = productRepo.findById(productId).orElseThrow(() -> new NoSuchElementException("Product doesn't exist."));
        Cart cart = cartRepo.findCartByCustomerId(customerId).orElseThrow(() -> new NoSuchElementException("Cart doesn't exist for the provided UUID"));


        CartItem cartItem = new CartItem(cart, product, product.getProductName(), DEFAULT_QUANTITY, product.getPrice(), OffsetDateTime.now());

        cartItemRepo.save(cartItem);
        
        return toDto(cartItem);
    }

    @Override
    public DTOCartItemResponse addCartItem(UUID customerId, long productId, int quantity) {
        return null;
    }

    @Override
    public DTOCartItemResponse removeCartItem(UUID customerId, long productId) {
        return null;
    }

    @Override
    public DTOCartItemResponse removeCartItem(UUID customerId, long productId, int quantity) {
        return null;
    }

    @Override
    public DTOCartItemResponse clearCart(UUID customerId) {
        return null;
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
