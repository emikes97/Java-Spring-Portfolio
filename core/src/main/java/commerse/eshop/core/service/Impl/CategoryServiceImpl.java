package commerse.eshop.core.service.Impl;

import com.sun.jdi.request.DuplicateRequestException;
import commerse.eshop.core.model.entity.Category;
import commerse.eshop.core.repository.CategoryRepo;
import commerse.eshop.core.service.CategoryService;
import commerse.eshop.core.web.dto.requests.Category.DTOAddCategory;
import commerse.eshop.core.web.dto.requests.Category.DTOUpdateCategory;
import commerse.eshop.core.web.dto.response.Category.DTOCategoryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepo categoryRepo;

    @Autowired
    public CategoryServiceImpl(CategoryRepo categoryRepo){
        this.categoryRepo = categoryRepo;
    }

    @Transactional
    @Override
    public DTOCategoryResponse addNewCategory(DTOAddCategory dto) {

        boolean duplicate = categoryRepo.existsByCategoryNameIgnoreCase(dto.categoryName());

        if (duplicate){
            throw new DuplicateRequestException("Category already exists");
        }

        Category category = new Category(dto.categoryName(), dto.categoryDescription());

        categoryRepo.saveAndFlush(category);

        return toDto(category);
    }

    @Transactional
    @Override
    public DTOCategoryResponse updateCategory(DTOUpdateCategory dto, long categoryId) {

        Category category = categoryRepo.findById(categoryId).orElseThrow(
                () -> new NoSuchElementException("The requested category doesn't exist"));

        if (dto.categoryName() != null && !dto.categoryName().isBlank())
            category.setCategoryName(dto.categoryName());
        if (dto.categoryDescription() != null && !dto.categoryDescription().isBlank())
            category.setCategoryDescription(dto.categoryDescription());

        categoryRepo.save(category);

        return toDto(category);
    }

    @Override
    public void deleteCategory(long categoryId) {
        long deleted = categoryRepo.deleteCategory(categoryId);
        log.info("{}, {} was deleted", deleted, categoryId);
    }

    private DTOCategoryResponse toDto(Category c){
        return new DTOCategoryResponse(
                c.getCategoryName(),
                c.getCategoryDescription()
        );
    }

}
