package commerse.eshop.core.repository;

import commerse.eshop.core.model.entity.Customer;
import commerse.eshop.core.model.entity.CustomerPaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerPaymentMethodRepo extends JpaRepository<CustomerPaymentMethod, UUID> {

    // Fetch the default payment method for Customer (card)
    Optional<CustomerPaymentMethod> findByCustomerAndIsDefaultTrue(Customer customer);

    // Fetch all payment methods for Customer
    List<CustomerPaymentMethod> findByCustomer(Customer customer);

    // == Overload methods . Search by UUID ==
    Optional<CustomerPaymentMethod> findByCustomer_CustomerIdAndIsDefaultTrue(UUID customerId);
    List<CustomerPaymentMethod> findByCustomer_CustomerId(UUID customerId);

    // == Marketing ==
    long countByProvider(String provider);
    long countByBrand(String brand);
}
