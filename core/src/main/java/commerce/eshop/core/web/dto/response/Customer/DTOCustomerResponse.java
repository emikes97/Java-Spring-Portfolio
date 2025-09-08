package commerce.eshop.core.web.dto.response.Customer;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DTOCustomerResponse(UUID customerId,
                                  String phoneNumber,
                                  String email,
                                  String username,
                                  String name,
                                  String surname,
                                  OffsetDateTime createdAt
                                  ) {}
