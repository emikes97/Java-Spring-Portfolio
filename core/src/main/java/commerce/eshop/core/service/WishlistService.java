package commerce.eshop.core.service;

import commerce.eshop.core.web.dto.requests.Wishlist.DTOWishlistRequest;
import commerce.eshop.core.web.dto.response.Wishlist.DTOWishlistResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface WishlistService {

    /// Add a wish to your wishlist
    DTOWishlistResponse addNewWish(UUID customerId, DTOWishlistRequest dto);

    /// Find all your wishes
    Page<DTOWishlistResponse> findAllWishes(UUID customerId);

    /// Find a wish
    DTOWishlistResponse findWish(UUID customerId, long wishId);

    /// Remove a wish
    void removeWish(UUID customerId, long wishId);

    /// Clear all wishes
    void clearWishlist(UUID customerId);
}
