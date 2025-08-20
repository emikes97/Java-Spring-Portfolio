package commerse.eshop.core.repository;

import commerse.eshop.core.model.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepo extends JpaRepository<Customer, UUID> {

    // == Find Customer by ==
    @Query(value = "select * from customers where phone_number = :input or email = :input", nativeQuery = true)
    Optional<Customer> findByPhoneNumberOrEmail(@Param("input") String phoneOrMail);

    // == Return Accounts with the same Name // SurName == //
    List<Customer> findByName(String name);
    List<Customer> findBySurname(String surname);
}
