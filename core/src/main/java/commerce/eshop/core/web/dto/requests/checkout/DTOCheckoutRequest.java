package commerce.eshop.core.web.dto.requests.checkout;

import commerce.eshop.core.web.dto.requests.Order.DTOOrderCustomerAddress;
import commerce.eshop.core.web.dto.requests.Transactions.DTOTransactionRequest;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record DTOCheckoutRequest(
        @NotNull @Valid DTOTransactionRequest payment,
        @Valid @Nullable DTOOrderCustomerAddress address
) {}
