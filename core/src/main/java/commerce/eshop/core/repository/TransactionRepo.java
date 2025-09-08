package commerce.eshop.core.repository;

import commerce.eshop.core.model.entity.Transaction;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepo extends JpaRepository<Transaction, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE) // SELECT ... FOR UPDATE
    @Query("select t from Transaction t where t.idempotencyKey = :key")
    Optional<Transaction> findByIdempotencyKeyForUpdate(@Param("key") String key);
}
