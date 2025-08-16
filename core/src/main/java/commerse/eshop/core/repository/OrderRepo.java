package commerse.eshop.core.repository;

import commerse.eshop.core.model.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepo extends JpaRepository<Order, UUID> {

    // All orders for a customer
    List<Order> findByCustomer_CustomerId(UUID customerId);
    Page<Order> findByCustomer_CustomerId(UUID customerId, Pageable pageable);

    // Open / Completed (newest first)
    List<Order> findByCustomer_CustomerIdAndCompletedAtIsNullOrderByCreatedAtDesc(UUID customerId);
    List<Order> findByCustomer_CustomerIdAndCompletedAtIsNotNullOrderByCreatedAtDesc(UUID customerId);

    // Handy: latest order
    Optional<Order> findFirstByCustomer_CustomerIdOrderByCreatedAtDesc(UUID customerId);
}
