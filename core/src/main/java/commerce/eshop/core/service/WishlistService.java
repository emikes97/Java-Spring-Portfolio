package commerce.eshop.core.service;

import commerce.eshop.core.web.dto.requests.Wishlist.DTOWishlistRequest;
import commerce.eshop.core.web.dto.response.Wishlist.DTOWishlistResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface WishlistService {

    /// Add a wish to your wishlist
    DTOWishlistResponse addNewWish(UUID customerId, long productId);

    /// Find all your wishes
    Page<DTOWishlistResponse> findAllWishes(UUID customerId, Pageable pageable);

    /// Find a wish
    DTOWishlistResponse findWish(UUID customerId, long wishId);

    /// Remove a wish
    void removeWish(UUID customerId, long wishId);

    /// Clear all wishes
    void clearWishlist(UUID customerId);
}
