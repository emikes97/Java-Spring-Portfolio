package commerce.eshop.core.repository;

import commerce.eshop.core.model.entity.Auditing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditingRepo extends JpaRepository<Auditing, Long> {


}
