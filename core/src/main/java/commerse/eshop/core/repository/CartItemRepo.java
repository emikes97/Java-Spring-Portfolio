package commerse.eshop.core.repository;

import commerse.eshop.core.model.entity.CartItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface CartItemRepo extends JpaRepository<CartItem, Long> {

    // == Fetch Cart_id by CartItem ==
    @Query(value = "select cart_id from cart_item where cart_item_id = :cartItemId", nativeQuery = true)
    Optional<UUID> findCartIdByCartItem(@Param("cartItemId") long cartItemId);

    // == Fetch all Cart_Items from a cart UUID.
    Page<CartItem> findByCart_CartId(UUID cartId, Pageable pageable);

    // == Fetch cart_item with ProductId and cartId


    // == Find if Cart Item exists in Cart_Id or not
    @Query(value = "select exists (" +
            "  select 1 from cart_items " +
            "  where cart_id = :cartId and product_id = :productId" +
            ")", nativeQuery = true)
    boolean findIfItemExists(UUID cartId, long productId);

    // == Sum all the total outstanding of cart_items
    @Query(value = "select coalesce(sum(price_at * quantity), 0)::numeric from cart_item where cart_id = :cartId", nativeQuery = true)
    BigDecimal sumCartTotalOutstanding(@Param("cartId") UUID cartId);
}
