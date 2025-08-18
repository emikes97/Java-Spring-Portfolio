package commerse.eshop.core.web.dto.requests.Category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DTOUpdateCategory(@Size(max=100) String categoryName,
                                @Size(max=255) String categoryDescription
                             ) {}
