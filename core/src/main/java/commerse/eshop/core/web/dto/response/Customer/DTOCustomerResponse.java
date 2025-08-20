package commerse.eshop.core.web.dto.response.Customer;

import java.util.UUID;

public record DTOCustomerResponse(UUID customerId,
                                  String phoneNumber,
                                  String email,
                                  String username,
                                  String name,
                                  String surname,
                                  String created_at
                                  ) {}
