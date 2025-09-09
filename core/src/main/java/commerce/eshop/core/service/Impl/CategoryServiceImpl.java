package commerce.eshop.core.service.Impl;

import commerce.eshop.core.model.entity.Category;
import commerce.eshop.core.util.CentralAudit;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.util.enums.AuditMessage;
import commerce.eshop.core.util.enums.AuditingStatus;
import commerce.eshop.core.repository.CategoryRepo;
import commerce.eshop.core.service.CategoryService;
import commerce.eshop.core.web.dto.requests.Category.DTOAddCategory;
import commerce.eshop.core.web.dto.requests.Category.DTOUpdateCategory;
import commerce.eshop.core.web.dto.response.Category.DTOCategoryResponse;
import commerce.eshop.core.web.mapper.CategoryServiceMapper;
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
    private final CentralAudit centralAudit;
    private final CategoryServiceMapper categoryServiceMapper;

    // == Constructors ==

    @Autowired
    public CategoryServiceImpl(CategoryRepo categoryRepo, CentralAudit centralAudit, CategoryServiceMapper categoryServiceMapper){
        this.categoryRepo = categoryRepo;
        this.centralAudit = centralAudit;
        this.categoryServiceMapper = categoryServiceMapper;
    }

    // == Public Methods ==

    @Transactional
    @Override
    public DTOCategoryResponse addNewCategory(DTOAddCategory dto) {

        boolean duplicate = categoryRepo.existsByCategoryNameIgnoreCase(dto.categoryName());

        // First duplicate check
        if (duplicate){
            throw centralAudit.audit(new DuplicateKeyException("Category already exists"), null,
                    EndpointsNameMethods.CATEGORY_CREATE, AuditingStatus.ERROR,"Category already exists");
        }

        Category category = new Category(dto.categoryName(), dto.categoryDescription());

        // Race condition check
        try {
            categoryRepo.saveAndFlush(category);
            centralAudit.info(null, EndpointsNameMethods.CATEGORY_UPDATE, AuditingStatus.SUCCESSFUL,
                    AuditMessage.CATEGORY_UPDATE_SUCCESS.getMessage());
            return categoryServiceMapper.toDto(category);
        } catch (DataIntegrityViolationException e) {
            throw centralAudit.audit(e,null, EndpointsNameMethods.CATEGORY_CREATE, AuditingStatus.ERROR,"Category already exists");
        }
    }

    @Transactional
    @Override
    public DTOCategoryResponse updateCategory(DTOUpdateCategory dto, long categoryId) {

        Category category = getCategoryOrThrow(categoryId, EndpointsNameMethods.CATEGORY_UPDATE);

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
            centralAudit.info(null, EndpointsNameMethods.CATEGORY_UPDATE, AuditingStatus.SUCCESSFUL,
                    AuditMessage.CATEGORY_UPDATE_SUCCESS.getMessage());
            return categoryServiceMapper.toDto(category);
        } catch (DataIntegrityViolationException dup){
            Throwable most = dup.getMostSpecificCause();

            String msg = (most.getMessage() != null && !most.getMessage().isBlank())
                    ? most.getMessage()
                    : dup.toString();

            throw centralAudit.audit(dup, null, EndpointsNameMethods.CATEGORY_UPDATE, AuditingStatus.ERROR, msg);
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
            throw centralAudit.audit(dive,null, EndpointsNameMethods.CATEGORY_DELETE, AuditingStatus.ERROR, msg);
        }

        if (deleted == 0){
            NoSuchElementException ex = new NoSuchElementException("The requested category doesn't exist");
            throw centralAudit.audit(ex, null, EndpointsNameMethods.CATEGORY_DELETE, AuditingStatus.WARNING, ex.toString());
        }

        centralAudit.info(null, EndpointsNameMethods.CATEGORY_DELETE, AuditingStatus.SUCCESSFUL, AuditMessage.CATEGORY_DELETE_SUCCESS.getMessage());
    }

    // == Private Methods ==

    private Category getCategoryOrThrow(long categoryId, String method){
        try{
           final Category category = categoryRepo.findById(categoryId).orElseThrow(
                    () -> new NoSuchElementException("The requested category doesn't exist"));
           return category;
        } catch (NoSuchElementException e){
            throw centralAudit.audit(e, null, method, AuditingStatus.ERROR);
        }
    }
}
