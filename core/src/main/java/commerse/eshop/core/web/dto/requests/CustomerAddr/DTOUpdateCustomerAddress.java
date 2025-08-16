package commerse.eshop.core.web.dto.requests.CustomerAddr;

import jakarta.validation.constraints.Size;

public record DTOUpdateCustomerAddress(@Size(max=150) String country,
                                       @Size(max=100) String street,
                                       @Size(max=75) String city,
                                       @Size(max=50) String postalCode,
                                       boolean isDefault) {
}