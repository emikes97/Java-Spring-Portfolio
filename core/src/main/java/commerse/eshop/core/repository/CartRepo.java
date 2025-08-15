package commerse.eshop.core.repository;

import commerse.eshop.core.model.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CartRepo extends JpaRepository<Cart, UUID> {

    // Fetch cart by UUID.
    Optional<Cart> findByCartId(UUID cartId);

    // Fetch cart by Customer
    Optional<Cart> findByCustomerCustomerId(UUID customerId);

    // Fetch Customer UUID from Cart
    @Query(value = "select customer_id from cart where cart_id = :cartId", nativeQuery = true)
    Optional<UUID> findCustomerIdByCartId(@Param("cardId") UUID cartId);
}
