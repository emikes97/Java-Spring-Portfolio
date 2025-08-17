package commerse.eshop.core.service;

import commerse.eshop.core.model.entity.CartItem;
import commerse.eshop.core.web.dto.response.Cart.CartItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CartService {

    // Retrieve a set list of all current cart_items --
    Page<CartItem> viewAllCartItems(UUID customerId, UUID cartId, Pageable pageable);

    // == Cart add items -- Overloaded methods ==
    CartItemResponse addCartItem(UUID customerId, long productId); // Default Quantity 1 || If product already in cart +1
    CartItemResponse addCartItem(UUID customerId, long productId, int quantity); // If product already in cart + quantity

    // == Cart Remove Item
    CartItemResponse removeCartItem(UUID customerId, long productId); // Removes all quantities of the item if no specified quantity is provided
    CartItemResponse removeCartItem(UUID customerId, long productId, int quantity); // Remove the quantity of product // if < 0 then remove the item altogether

    // == Clear Cart ==
    CartItemResponse clearCart(UUID customerId);


}
