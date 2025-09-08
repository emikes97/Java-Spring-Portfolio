package commerce.eshop.core.repository;

import commerce.eshop.core.model.entity.WishlistItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface WishlistItemRepo extends JpaRepository<WishlistItem, Long> {

    /// Get all wishes by wishId
    Page<WishlistItem> findByWishlist_WishlistId(UUID wishlistId, Pageable pageable);

    /// Get a wish by wishlist Id & long id to verify if its for the user we search.
    @Query(value = "select * from wishlist_item where wishlist_id = :wishlistId and wish_id = :wishId", nativeQuery = true)
    Optional<WishlistItem> findWish(@Param("wishlistId") UUID wishlistId, @Param("wishId") long wishId);

    /// Clear wishlist
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "delete from wishlist_item where wishlist_id = :wishlistId", nativeQuery = true)
    int clearWishlist(@Param("wishlistId") UUID wishlistId);

    /// Expected to clear by wishlist
    @Query(value = "select count(*) from wishlist_item where wishlist_id = :wishlistId", nativeQuery = true)
    int countWishlistItems(@Param("wishlistId") UUID wishlistId);
}
