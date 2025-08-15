package commerse.eshop.core.repository;

import commerse.eshop.core.model.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepo extends JpaRepository<Customer, UUID> {

    // == Find Customer by ==
    Optional<Customer> findByPhone(String phoneNumber);
    Optional<Customer> findByEmail(String email);

    // == Return Accounts with the same Name // SurName == //
    List<Customer> findByName(String name);
    List<Customer> findBySurname(String surname);
}
