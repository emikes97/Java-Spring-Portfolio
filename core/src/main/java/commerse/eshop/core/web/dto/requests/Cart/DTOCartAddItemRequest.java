package commerse.eshop.core.web.dto.requests.Cart;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DTOCartAddItemRequest(
        @NotNull long productId,
        @Positive(message = "quantity must be > 0") int quantity) {}
