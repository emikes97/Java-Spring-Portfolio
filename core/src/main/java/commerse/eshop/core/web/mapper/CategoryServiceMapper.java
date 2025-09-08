package commerse.eshop.core.web.mapper;

import commerse.eshop.core.model.entity.Category;
import commerse.eshop.core.web.dto.response.Category.DTOCategoryResponse;
import org.springframework.stereotype.Component;

@Component
public class CategoryServiceMapper {

    public DTOCategoryResponse toDto(Category c){
        return new DTOCategoryResponse(
                c.getCategoryName(),
                c.getCategoryDescription()
        );
    }
}
