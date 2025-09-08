package commerce.eshop.core.repository;

import commerce.eshop.core.model.entity.Order;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepo extends JpaRepository<Order, UUID> {

    // All orders for a customer
    Page<Order> findByCustomer_CustomerId(UUID customerId, Pageable pageable);

    // Find By CustomerId and OrderId
    Optional<Order> findByCustomer_CustomerIdAndOrderId(UUID customerId, UUID orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.orderId = :orderId")
    Optional<Order> findByOrderIdForUpdate(@Param("orderId") UUID orderId);

    // Update Reserved items back to the products stock after a cancelled order.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
    UPDATE products p
    SET product_available_stock = product_available_stock + oi.quantity
    FROM order_item oi
    WHERE oi.order_id = :orderId
      AND oi.product_id = p.product_id
    """, nativeQuery = true)
    int restoreProductStockFromOrder(@Param("orderId") UUID orderId);

    @Query(value = "select count(*) from order_item where order_id = :orderId", nativeQuery = true)
    int countProductRowsToBeUpdated(@Param("orderId") UUID orderId);

}
