package commerse.eshop.core.web.dto.requests.Cart;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DTOCartAddItemRequest(
        @NotNull long productId,
        @Size(min = 1) int quantity) {}
