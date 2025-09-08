package commerce.eshop.core.repository;

import commerce.eshop.core.model.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CartRepo extends JpaRepository<Cart, UUID> {

    // Fetch cart by Customer
    Optional<Cart> findByCustomerCustomerId(UUID customerId);

    // Fetch Cart UUID from Cart from Customer UUID
    @Query(value = "select cart_id from cart where customer_id = :custId", nativeQuery = true)
    Optional<UUID> findCartIdByCustomerId(@Param("custId") UUID customerId);

    // Fetch Cart from Customer UUID
    @Query(value = "select * from cart where customer_id = :custId", nativeQuery = true)
    Optional<Cart> findCartByCustomerId(@Param("custId") UUID customerId);
}
