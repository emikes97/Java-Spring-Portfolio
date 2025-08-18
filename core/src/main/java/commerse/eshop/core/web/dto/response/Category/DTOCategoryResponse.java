package commerse.eshop.core.web.dto.response.Category;

import jakarta.validation.constraints.NotBlank;

public record DTOCategoryResponse(@NotBlank String categoryName,
                                  @NotBlank String categoryDescription) {
}
