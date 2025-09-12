package commerce.eshop.core.web.controller;

import commerce.eshop.core.service.WishlistService;
import commerce.eshop.core.web.dto.response.Wishlist.DTOWishlistResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/{customerId}/wishlist")
public class WishController {

    private final WishlistService wishlistService;

    public WishController(WishlistService wishlistService){
        this.wishlistService = wishlistService;
    }

    // Add a new wishlist item
    /// curl
    @PostMapping("/item")
    public DTOWishlistResponse addNewWish(@PathVariable UUID customerId, @RequestParam long itemId){
        return wishlistService.addNewWish(customerId, itemId);
    }

    // Get all wishes
    /// curl
    @PostMapping("/allWishes")
    public Page<DTOWishlistResponse> findAllWishes(@PathVariable UUID customerId, Pageable pageable){
        return wishlistService.findAllWishes(customerId, pageable);
    }

    // Get wish
    /// curl
    @PostMapping("/{itemId}")
    public DTOWishlistResponse findWish(@PathVariable UUID customerId, @PathVariable long itemId){
        return wishlistService.findWish(customerId, itemId);
    }

    // Remove Wish
    /// curl
    @PostMapping("/remove/{itemId}")
    public void removeWish(@PathVariable UUID customerId, @PathVariable long itemId){

    }
}
