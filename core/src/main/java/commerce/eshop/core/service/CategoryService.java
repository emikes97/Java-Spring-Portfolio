package commerce.eshop.core.service;

import commerce.eshop.core.web.dto.requests.Category.DTOAddCategory;
import commerce.eshop.core.web.dto.requests.Category.DTOUpdateCategory;
import commerce.eshop.core.web.dto.response.Category.DTOCategoryResponse;

public interface CategoryService {

    // == Add or Update Categories
    DTOCategoryResponse addNewCategory(DTOAddCategory dto);
    DTOCategoryResponse updateCategory(DTOUpdateCategory dto, long categoryId);

    // == Delete a category
    void deleteCategory(long categoryId);
}
