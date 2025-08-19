package commerse.eshop.core.web.dto.response.Order;

import commerse.eshop.core.web.dto.requests.Order.DTOOrderCustomerAddress;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record DTOOrderDetailsResponse(@NotBlank UUID orderId,
                                      @NotNull @DecimalMin("0.00") BigDecimal totalOutstanding,
                                      @NotNull DTOOrderCustomerAddress addressToSend,
                                      @NotNull Map<String, Object> orderItems,
                                      @NotNull OffsetDateTime orderCreatedAt,
                                      OffsetDateTime orderCompletedAt){}
