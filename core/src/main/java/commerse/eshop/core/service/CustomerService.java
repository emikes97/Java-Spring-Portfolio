package commerse.eshop.core.service;

import commerse.eshop.core.model.entity.CartItem;
import commerse.eshop.core.model.entity.Order;
import commerse.eshop.core.web.dto.response.Customer.DTOCustomerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CustomerService {

    // == Customer Methods == //

    // == Read Only == //
    DTOCustomerResponse getProfile(UUID customerId);
    Page<Order> getOrders(UUID customerId, Pageable pageable);
    Page<CartItem> getCartItems(UUID customerId, Pageable pageable);

    // == Update Profile - @Transaction //
    void updateName(UUID customerId, String password, String name);
    void updateSurname(UUID customerId, String password, String lastName);
    void updateFullName(UUID customerId, String password, String name, String lastName);
    void updateUserName(UUID customerId, String password, String userName);
    void updateUserPassword(UUID customerId, String currentPassword, String newPassword);
}
