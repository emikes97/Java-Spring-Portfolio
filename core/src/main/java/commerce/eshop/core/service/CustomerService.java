package commerce.eshop.core.service;

import commerce.eshop.core.web.dto.requests.Customer.DTOCustomerCreateUser;
import commerce.eshop.core.web.dto.response.Customer.DTOCustomerCartItemResponse;
import commerce.eshop.core.web.dto.response.Customer.DTOCustomerOrderResponse;
import commerce.eshop.core.web.dto.response.Customer.DTOCustomerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CustomerService {

    // == Customer Methods == //

    // == Read Only == //
    DTOCustomerResponse getProfile(UUID customerId);
    DTOCustomerResponse getProfile(String phoneOrMail);

    Page<DTOCustomerOrderResponse> getOrders(UUID customerId, Pageable pageable);
    Page<DTOCustomerCartItemResponse> getCartItems(UUID customerId, Pageable pageable);

    // == Create User //
    DTOCustomerResponse createUser(DTOCustomerCreateUser dto);

    // == Update Profile - @Transaction //
    void updateName(UUID customerId, String password, String name);
    void updateSurname(UUID customerId, String password, String lastName);
    void updateFullName(UUID customerId, String password, String name, String lastName);
    void updateUserName(UUID customerId, String password, String userName);
    void updateUserPassword(UUID customerId, String currentPassword, String newPassword);
}
