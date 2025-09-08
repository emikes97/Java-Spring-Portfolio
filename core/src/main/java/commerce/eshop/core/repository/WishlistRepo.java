package commerce.eshop.core.repository;

import commerce.eshop.core.model.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface WishlistRepo extends JpaRepository<Wishlist, UUID> {

    // Fetch Wishlist ID from customer
    @Query(value = "select wishlist_id from wishlist where customer_id = :custId", nativeQuery = true)
    Optional<UUID> findWishlistIdByCustomerId(@Param("custId") UUID customerId);

    // Fetch Wishlist from customer
    @Query(value =  "select * from wishlist where customer_id = :custId", nativeQuery = true)
    Optional<Wishlist> findWishlistByCustomerId(@Param("custId") UUID customerId);
}
