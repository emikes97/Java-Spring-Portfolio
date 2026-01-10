package commerce.eshop.core.repository;

import commerce.eshop.core.model.outbox.CheckoutJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckoutJobRepo extends JpaRepository<CheckoutJob, Long> {
}
