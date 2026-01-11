package commerce.eshop.core.repository;

import commerce.eshop.core.model.outbox.CheckoutJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CheckoutJobRepo extends JpaRepository<CheckoutJob, Long> {

    @Query(value = "select id from checkout_job where state = 'PENDING' order by created_at for update skip locked limit :batchSize", nativeQuery = true)
    List<Long> lockQueuedBatch(int batchSize);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "update checkout_job set state = 'PROCESSING', updated_at = now() where id in (:ids)", nativeQuery = true)
    int markProcessingQueuedBatch(List<Long> ids);

    @Query(value = "select * from checkout_job where customer_id = :customerId and idemkey = :idemKey", nativeQuery = true)
    Optional<CheckoutJob> findExistingCheckoutJob(UUID customerId, UUID idemKey);

    @Query(value = "select exists ( select 1 from checkout_job where state = 'PENDING')", nativeQuery = true)
    boolean checkIfCheckoutJobsArePending();

    @Query(value = "select * from checkout_job where id = :id", nativeQuery = true)
    Optional<CheckoutJob> getCheckoutJobById(long id);
}
