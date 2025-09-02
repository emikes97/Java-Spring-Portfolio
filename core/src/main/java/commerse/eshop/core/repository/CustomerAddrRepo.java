package commerse.eshop.core.repository;

import commerse.eshop.core.model.entity.Customer;
import commerse.eshop.core.model.entity.CustomerAddress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerAddrRepo extends JpaRepository<CustomerAddress, Long> {

    // Fetch the default address of the customer
    Optional<CustomerAddress> findByCustomerCustomerIdAndIsDefaultTrue(UUID customerId);

    // == Fetch the address by UUID and ID to ensure its the correct user
    Optional<CustomerAddress> findByAddrIdAndCustomer_CustomerId(Long addressId, UUID customerId);

    // Fetch all addresses that are bound to customer_id
    Page<CustomerAddress> findByCustomerCustomerId(UUID customerId, Pageable pageable);

    // Delete an address via id and customer UUID
    long deleteByAddrIdAndCustomer_CustomerId(Long addressId, UUID customerId);

    // == Set default flag to false
    @Modifying
    @Query(value = "update customers_address set is_default = false where customer_id = :custId and is_default = true", nativeQuery = true)
    int clearDefaultsForCustomer(@Param(value = "custId")UUID customerId);
}
