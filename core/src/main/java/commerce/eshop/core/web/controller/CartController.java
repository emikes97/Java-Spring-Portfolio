package commerce.eshop.core.web.controller;

import commerce.eshop.core.service.CartService;
import commerce.eshop.core.web.dto.requests.Cart.DTOCartAddItemRequest;
import commerce.eshop.core.web.dto.requests.Cart.DTOCartRemoveItemRequest;
import commerce.eshop.core.web.dto.response.Cart.DTOCartItemResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers/{customerId}/cart")
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService){
        this.cartService = cartService;
    }


    // View all cart_items in cart
    /// curl -i -X GET "http://localhost:8080/api/v1/customers/00000000-0000-0000-0000-000000000101/cart?page=0&size=10"
    @GetMapping
    public Page<DTOCartItemResponse> viewAllCartItems(@PathVariable UUID customerId, Pageable pageable){
        return cartService.viewAllCartItems(customerId, pageable);
    }

    // View a single item in cart
    ///curl -i -X GET "http://localhost:8080/api/v1/customers/00000000-0000-0000-0000-000000000101/cart/items/1"
    @GetMapping("/items/{productId}")
    public DTOCartItemResponse viewItem(@PathVariable UUID customerId, @PathVariable long productId){
        return cartService.findItem(customerId, productId);
    }

    // Add A single quantity or multiple
    ///curl -i -X POST "http://localhost:8080/api/v1/customers/499008e1-13fa-4db8-983f-a6fc175f2445/cart" ^
    ///   -H "Content-Type: application/json" ^
    ///   -d "{\"productId\":1,\"quantity\":2}"
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DTOCartItemResponse addItem(@PathVariable UUID customerId, @RequestBody @Valid DTOCartAddItemRequest dto){
        return cartService.addCartItem(customerId,dto.productId(),dto.quantity());
    }

    // Delete an item from cart
    ///curl -i -X DELETE "http://localhost:8080/api/v1/customers/00000000-0000-0000-0000-000000000101/cart/items" ^
    ///   -H "Content-Type: application/json" ^
    ///   -d "{\"productId\":1,\"quantity\":2}"
    ///
    /// ----
    ///curl -i -X DELETE "http://localhost:8080/api/v1/customers/00000000-0000-0000-0000-000000000101/cart/items" ^
    ///   -H "Content-Type: application/json" ^
    ///   -d "{\"productId\":1,\"quantity\":null}"
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/items")
    public void removeCartItem(@PathVariable UUID customerId, @RequestBody @Valid DTOCartRemoveItemRequest dto){
        cartService.removeCartItem(customerId, dto.productId(), dto.quantity());
    }

    // Delete all items from cart
    ///curl -i -X DELETE "http://localhost:8080/api/v1/customers/00000000-0000-0000-0000-000000000101/cart"
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping
    public void clearCart(@PathVariable UUID customerId){
        cartService.clearCart(customerId);
    }
}
