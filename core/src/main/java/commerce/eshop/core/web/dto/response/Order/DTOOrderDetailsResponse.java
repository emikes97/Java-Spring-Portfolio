package commerce.eshop.core.web.dto.response.Order;

import commerce.eshop.core.web.dto.requests.Order.DTOOrderCustomerAddress;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record DTOOrderDetailsResponse(@NotNull UUID orderId,
                                      @NotNull @DecimalMin("0.00") BigDecimal totalOutstanding,
                                      @NotNull DTOOrderCustomerAddress addressToSend,
                                      @NotNull List<DTOOrderItemsResponse> orderItems,
                                      @NotNull OffsetDateTime orderCreatedAt,
                                      OffsetDateTime orderCompletedAt){}
