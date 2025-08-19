package commerse.eshop.core.repository;

import commerse.eshop.core.model.entity.Customer;
import commerse.eshop.core.model.entity.CustomerPaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerPaymentMethodRepo extends JpaRepository<CustomerPaymentMethod, UUID> {

    // Fetch the default payment method for Customer (card)
    Optional<CustomerPaymentMethod> findByCustomerAndIsDefaultTrue(Customer customer);

    // Fetch all payment methods by customer
    List<CustomerPaymentMethod> findByCustomer(Customer customer);

    // Fetch PaymentMethod by customer UUID and paymentmethod UUID
    Optional<CustomerPaymentMethod> findByCustomer_CustomerIdAndCustomerPaymentId(UUID customerId, UUID paymentId);

    // Update default method to false
    @Modifying
    @Query(value = "update customer_payment_methods set is_default = false where customer_id = :custId and is_default = true", nativeQuery = true)
    int updateDefaultMethodToFalse(@Param("custId") UUID customerId);

    // Remove the payment method with Customer UUID and payment UUID
    @Modifying
    long deleteByCustomer_CustomerIdAndCustomerPaymentId(UUID customerId, UUID paymentId);

    // Fetch All payment methods by Customer ID pageable
    Page<CustomerPaymentMethod> findByCustomer_CustomerId(UUID customerId, Pageable pageable);

    // == Marketing ==
    long countByProvider(String provider);
    long countByBrand(String brand);
}
