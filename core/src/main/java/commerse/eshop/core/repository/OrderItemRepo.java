package commerse.eshop.core.repository;

import commerse.eshop.core.model.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.UUID;

public interface OrderItemRepo extends JpaRepository<OrderItem, Long> {

    // == Snapshot cart_items to Order Items.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "insert into order_item (order_id, product_id, product_name, price_at, quantity, snap_shot_at) select :orderId," +
            "product_id, product_name, price_at, quantity, now() from cart_item where cart_id = :cartId", nativeQuery = true)
    int snapShotFromCart(@Param("orderId") UUID orderId, @Param("cartId") UUID cartId);

    @Query(value = "select coalesce(sum(price_at * quantity), 0) from order_item where order_id = :orderId", nativeQuery = true)
    BigDecimal sumOrderTotal(@Param("orderId") UUID orderId);

    @Modifying
    @Query(value = "delete from cart_item where cart_id = :cartId", nativeQuery = true)
    int clearCart(@Param("cartId") UUID cartId);
}
