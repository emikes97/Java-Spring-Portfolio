package commerce.eshop.core.web.controller;

import commerce.eshop.core.service.WishlistService;
import commerce.eshop.core.web.dto.response.Wishlist.DTOWishlistResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers/{customerId}/wishlist")
public class WishController {

    private final WishlistService wishlistService;

    public WishController(WishlistService wishlistService){
        this.wishlistService = wishlistService;
    }

    // Add a new wishlist item
    /// curl -X POST "http://localhost:8080/api/v1/customers/{CUSTOMER_ID}/wishlist/items?productId=123"
    @PostMapping("/item")
    @ResponseStatus(HttpStatus.CREATED)
    public DTOWishlistResponse addNewWish(@PathVariable UUID customerId, @RequestParam long itemId){
        return wishlistService.addNewWish(customerId, itemId);
    }

    // Get all wishes
    /// curl -X GET "http://localhost:8080/api/v1/customers/{CUSTOMER_ID}/wishlist/items?page=0&size=20&sort=createdAt,desc"
    @GetMapping("/wishes")
    public Page<DTOWishlistResponse> findAllWishes(@PathVariable UUID customerId, Pageable pageable){
        return wishlistService.findAllWishes(customerId, pageable);
    }

    // Get wish
    /// curl -X GET "http://localhost:8080/api/v1/customers/{CUSTOMER_ID}/wishlist/items/123"
    @GetMapping("/wishes/{itemId}")
    public DTOWishlistResponse findWish(@PathVariable UUID customerId, @PathVariable long itemId){
        return wishlistService.findWish(customerId, itemId);
    }

    // Remove Wish
    /// curl -X DELETE "http://localhost:8080/api/v1/customers/{CUSTOMER_ID}/wishlist/items/123"
    @DeleteMapping("/remove/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeWish(@PathVariable UUID customerId, @PathVariable long itemId){
        wishlistService.removeWish(customerId, itemId);
    }

    // Remove all wishes
    /// curl -X DELETE "http://localhost:8080/api/v1/customers/{CUSTOMER_ID}/wishlist/items"
    @PostMapping("/remove/all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearWishlist(@PathVariable UUID customerId){
        wishlistService.clearWishlist(customerId);
    }
}
