package commerce.eshop.core.web.dto.requests.Customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DTOCustomerUpdateName(@NotBlank String password,
                                    @NotBlank @Size(min=2, max=100) String name) {
}
