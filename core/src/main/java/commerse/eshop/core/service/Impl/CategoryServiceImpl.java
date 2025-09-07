package commerse.eshop.core.service.Impl;

import commerse.eshop.core.model.entity.Category;
import commerse.eshop.core.model.entity.consts.EndpointsNameMethods;
import commerse.eshop.core.model.entity.enums.AuditMessage;
import commerse.eshop.core.model.entity.enums.AuditingStatus;
import commerse.eshop.core.repository.CategoryRepo;
import commerse.eshop.core.service.AuditingService;
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
    private final AuditingService auditingService;

    // == Constructors ==

    @Autowired
    public CategoryServiceImpl(CategoryRepo categoryRepo, AuditingService auditingService){
        this.categoryRepo = categoryRepo;
        this.auditingService = auditingService;
    }

    // == Public Methods ==

    @Transactional
    @Override
    public DTOCategoryResponse addNewCategory(DTOAddCategory dto) {

        boolean duplicate = categoryRepo.existsByCategoryNameIgnoreCase(dto.categoryName());

        // First duplicate check
        if (duplicate){
            log.warn("The provided category name was duplicate: {} ", dto.categoryName());
            auditingService.log(null, EndpointsNameMethods.CATEGORY_CREATE, AuditingStatus.ERROR,"Category already exists");
            throw new DuplicateKeyException("Category already exists");
        }

        Category category = new Category(dto.categoryName(), dto.categoryDescription());

        // Race condition check
        try {
            categoryRepo.saveAndFlush(category);
            log.info("New category saved: {}", category.getCategoryName());
        } catch (DataIntegrityViolationException e) {
            auditingService.log(null, EndpointsNameMethods.CATEGORY_CREATE, AuditingStatus.ERROR,"Category already exists");
            log.warn("Duplicate category on insert (constraint hit): {}", dto.categoryName());
            throw new DuplicateKeyException("Category already exists");
        }
        auditingService.log(null, EndpointsNameMethods.CATEGORY_CREATE, AuditingStatus.SUCCESSFUL, AuditMessage.CATEGORY_CREATE_SUCCESS.getMessage());
        return toDto(category);
    }

    @Transactional
    @Override
    public DTOCategoryResponse updateCategory(DTOUpdateCategory dto, long categoryId) {

        Category category;

        try{
            category = categoryRepo.findById(categoryId).orElseThrow(
                    () -> new NoSuchElementException("The requested category doesn't exist"));
        } catch (NoSuchElementException e){
            auditingService.log(null, EndpointsNameMethods.CATEGORY_UPDATE, AuditingStatus.ERROR, e.toString());
            throw e;
        }

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

        // Race condition check
        try{
            categoryRepo.saveAndFlush(category);
            auditingService.log(null, EndpointsNameMethods.CATEGORY_UPDATE, AuditingStatus.SUCCESSFUL, AuditMessage.CATEGORY_UPDATE_SUCCESS.getMessage());
            return toDto(category);
        } catch (DataIntegrityViolationException dup){
            Throwable most = dup.getMostSpecificCause();

            String msg = (most.getMessage() != null && !most.getMessage().isBlank())
                    ? most.getMessage()
                    : dup.toString();

            auditingService.log(null, EndpointsNameMethods.CATEGORY_UPDATE, AuditingStatus.ERROR, msg);
            throw dup;
        }
    }

    @Transactional
    @Override
    public void deleteCategory(long categoryId) {

        final int deleted;

        try {
            deleted = categoryRepo.deleteCategory(categoryId);
            categoryRepo.flush(); // forces constraint check now so catch can handle it
        } catch (DataIntegrityViolationException dive){
            Throwable most = dive.getMostSpecificCause(); // never null
            String msg = (most.getMessage() != null && !most.getMessage().isBlank())
                    ? most.getMessage()
                    : dive.toString();
            auditingService.log(null, EndpointsNameMethods.CATEGORY_DELETE, AuditingStatus.ERROR, msg);
            throw dive;
        }

        if (deleted == 0){
            NoSuchElementException ex = new NoSuchElementException("The requested category doesn't exist");
            auditingService.log(null, EndpointsNameMethods.CATEGORY_DELETE, AuditingStatus.WARNING, ex.toString());
            throw ex;
        }

        auditingService.log(null, EndpointsNameMethods.CATEGORY_DELETE, AuditingStatus.SUCCESSFUL, AuditMessage.CATEGORY_DELETE_SUCCESS.getMessage());
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
