package commerse.eshop.core.web.dto.requests.Cart;

import jakarta.validation.constraints.NotNull;

public record DTOCartRemoveItemRequest(
    @NotNull long productId,
    Integer quantity) {}
