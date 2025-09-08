package commerce.eshop.core.web.dto.requests.Customer;

import jakarta.validation.constraints.NotBlank;

public record DTOCustomerUpdatePassword(
        @NotBlank String newPassword,
        @NotBlank String currentPassword) {}
