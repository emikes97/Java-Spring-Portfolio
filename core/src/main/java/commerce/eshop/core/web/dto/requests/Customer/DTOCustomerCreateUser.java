package commerce.eshop.core.web.dto.requests.Customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DTOCustomerCreateUser(
        @NotBlank @Size(min=10, max = 20) String phoneNumber,
        @NotBlank @Size(min = 10, max=255) String email,
        @NotBlank @Size(min=3, max=50) String userName,
        @NotBlank String password,
        @Size(max=100) String name,
        @Size(max=100) String surname) {}