package commerce.eshop.core.web.dto.requests.Category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DTOAddCategory(@NotBlank @Size(max=100) String categoryName,
                             @NotBlank @Size(max=255) String categoryDescription
                             ) {}
