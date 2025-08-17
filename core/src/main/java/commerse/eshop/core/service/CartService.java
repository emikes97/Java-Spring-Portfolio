package commerse.eshop.core.service;

import commerse.eshop.core.web.dto.response.Cart.DTOCartItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface CartService {

    // Retrieve a set list of all current cart_items --
    Page<DTOCartItemResponse> viewAllCartItems(UUID customerId, Pageable pageable);

    // Retrieve an item from cart
    DTOCartItemResponse findItem(UUID customerId, long productId);

    // == Cart add items -- Overloaded methods ==
    DTOCartItemResponse addCartItem(UUID customerId, long productId); // Default Quantity 1 || If product already in cart = cart.qnt +1
    DTOCartItemResponse addCartItem(UUID customerId, long productId, int quantity); // If product already in cart = cart.qnt + quantity

    // == Cart Remove Item
    void removeCartItem(UUID customerId, long productId); // Removes all quantities of the item if no specified quantity is provided
    void removeCartItem(UUID customerId, long productId, int quantity); // Remove the quantity of product // if < 0 then remove the item altogether

    // == Clear Cart ==
    void clearCart(UUID customerId);
}
