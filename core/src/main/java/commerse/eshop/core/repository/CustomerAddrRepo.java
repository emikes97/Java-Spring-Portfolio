package commerse.eshop.core.repository;

import commerse.eshop.core.model.entity.Customer;
import commerse.eshop.core.model.entity.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerAddrRepo extends JpaRepository<CustomerAddress, Long> {

    // Fetch the default address of the customer
    Optional<CustomerAddress> findByCustomerCustomerIdAndIsDefaultTrue(UUID customerId);

    // Fetch all the addresses by Customer
    List<CustomerAddress> findByCustomer(Customer customer);

    // Fetch all addresses that are bound to customer_id
    List<CustomerAddress> findByCustomerCustomerId(UUID customerId);

    // Fetch all addresses by City - Could be used for Marketing
    List<CustomerAddress> findByCityIgnoreCase(String city);

    // Fetch all addresses by Postal code - Could be used for Marketing
    List<CustomerAddress> findByPostalCode(String postalCode);
}
