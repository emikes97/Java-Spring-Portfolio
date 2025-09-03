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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    // == Fields ==

    private final CategoryRepo categoryRepo;

    // == Constructors ==

    @Autowired
    public CategoryServiceImpl(CategoryRepo categoryRepo){
        this.categoryRepo = categoryRepo;
    }

    // == Public Methods ==

    @Transactional
    @Override
    public DTOCategoryResponse addNewCategory(DTOAddCategory dto) {

        boolean duplicate = categoryRepo.existsByCategoryNameIgnoreCase(dto.categoryName());

        // First duplicate check
        if (duplicate){
            log.warn("The provided category name was duplicate: {} ", dto.categoryName());
            throw new DuplicateKeyException("Category already exists");
        }

        Category category = new Category(dto.categoryName(), dto.categoryDescription());

        // Race condition check
        try {
            categoryRepo.saveAndFlush(category);
            log.info("New category saved: {}", category.getCategoryName());
        } catch (DataIntegrityViolationException e) {
            log.warn("Duplicate category on insert (constraint hit): {}", dto.categoryName());
            throw new DuplicateKeyException("Category already exists");
        }
        return toDto(category);
    }

    @Transactional
    @Override
    public DTOCategoryResponse updateCategory(DTOUpdateCategory dto, long categoryId) {

        Category category = categoryRepo.findById(categoryId).orElseThrow(
                () -> new NoSuchElementException("The requested category doesn't exist"));

        if (dto.categoryName() != null && !dto.categoryName().isBlank()){
            String temp_catName = category.getCategoryName();
            category.setCategoryName(dto.categoryName());
            log.info("Category name with id= {}, has been changed from {} to {}.", categoryId, temp_catName, dto.categoryName());
        }
        if (dto.categoryDescription() != null && !dto.categoryDescription().isBlank()) {
            String temp_catDesc = category.getCategoryDescription();
            category.setCategoryDescription(dto.categoryDescription());
            log.info("Category description with id= {}, has been changed from {} to {}.", categoryId, temp_catDesc, dto.categoryDescription());
        }

        categoryRepo.save(category);

        return toDto(category);
    }

    @Transactional
    @Override
    public void deleteCategory(long categoryId) {
        int deleted = categoryRepo.deleteCategory(categoryId);
        log.info("Deleted categories count={}, categoryId={}", deleted, categoryId);
    }

    // == Private Methods ==

    private DTOCategoryResponse toDto(Category c){
        return new DTOCategoryResponse(
                c.getCategoryName(),
                c.getCategoryDescription()
        );
    }

}
