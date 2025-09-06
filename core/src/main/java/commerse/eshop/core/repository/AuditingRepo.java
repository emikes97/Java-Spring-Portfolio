package commerse.eshop.core.repository;

import commerse.eshop.core.model.entity.Auditing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditingRepo extends JpaRepository<Auditing, Long> {


}
