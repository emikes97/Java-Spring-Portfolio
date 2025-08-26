package commerse.eshop.core.repository;

import commerse.eshop.core.model.entity.Transaction;
import commerse.eshop.core.model.entity.enums.TransactionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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


    @Lock(LockModeType.PESSIMISTIC_WRITE) // SELECT ... FOR UPDATE
    @Query("select t from Transaction t where t.idempotencyKey = :key")
    Optional<Transaction> findByIdempotencyKeyForUpdate(@Param("key") String key);
}
