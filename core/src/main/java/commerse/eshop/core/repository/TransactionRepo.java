package commerse.eshop.core.repository;

import commerse.eshop.core.model.entity.Transaction;
import commerse.eshop.core.model.entity.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepo extends JpaRepository<Transaction, UUID> {

    // == Fetch the Transaction by the Order id.
    Optional<Transaction> findByOrder_OrderId(UUID orderId);

    // == Fetch all transactions by customer.
    List<Transaction> findByCustomerId(String customerId);

    // == Helpers ==
    Optional<Transaction> findByIdempotencyKey(String key);
    List<Transaction> findByOrder_OrderIdAndStatus(UUID orderId, TransactionStatus status);
}
