package commerce.eshop.core.web.dto.response.Order;

import commerce.eshop.core.web.dto.requests.Order.DTOOrderCustomerAddress;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record DTOOrderPlacedResponse(@NotBlank UUID orderId,
                                     @NotNull @DecimalMin("0.00") BigDecimal totalOutstanding,
                                     @NotNull DTOOrderCustomerAddress addressToSend,
                                     @NotNull OffsetDateTime orderCreatedAt,
                                     OffsetDateTime orderCompletedAt) {}
