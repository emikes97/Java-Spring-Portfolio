package commerse.eshop.core.web.controller;

import commerse.eshop.core.service.CartService;
import commerse.eshop.core.web.dto.requests.Cart.DTOCartAddItemRequest;
import commerse.eshop.core.web.dto.response.Cart.DTOCartItemResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    ///
    @GetMapping("/items")
    public Page<DTOCartItemResponse> viewAllCartItems(@PathVariable UUID customerId, Pageable pageable){
        return cartService.viewAllCartItems(customerId, pageable);
    }

    // View a single item in cart
    ///
    @GetMapping("/items/{productId}")
    public DTOCartItemResponse viewItem(@PathVariable UUID customerId, @PathVariable long productId){
        return cartService.findItem(customerId, productId);
    }

    // Add A single quantity or multiple
    /// -- First curl --
    @PostMapping
    public DTOCartItemResponse addItem(@PathVariable UUID customerId, @RequestBody @Valid DTOCartAddItemRequest dto){
        return cartService.addCartItem(customerId,dto.productId(),dto.quantity());
    }


}
