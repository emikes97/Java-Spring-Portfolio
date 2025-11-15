package commerce.eshop.core.service.Impl;

import commerce.eshop.core.application.category.commands.AddCategory;
import commerce.eshop.core.application.category.commands.DeleteCategory;
import commerce.eshop.core.application.category.commands.UpdateCategory;
import commerce.eshop.core.model.entity.Category;
import commerce.eshop.core.util.CentralAudit;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.util.enums.AuditMessage;
import commerce.eshop.core.util.enums.AuditingStatus;
import commerce.eshop.core.service.CategoryService;
import commerce.eshop.core.web.dto.requests.Category.DTOAddCategory;
import commerce.eshop.core.web.dto.requests.Category.DTOUpdateCategory;
import commerce.eshop.core.web.dto.response.Category.DTOCategoryResponse;
import commerce.eshop.core.web.mapper.CategoryServiceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl implements CategoryService {

    // == Fields ==
    private final AddCategory addCategory;
    private final UpdateCategory updateCategory;
    private final DeleteCategory deleteCategory;
    private final CentralAudit centralAudit;
    private final CategoryServiceMapper categoryServiceMapper;

    // == Constructors ==

    @Autowired
    public CategoryServiceImpl(AddCategory addCategory, UpdateCategory updateCategory, DeleteCategory deleteCategory,
                               CentralAudit centralAudit, CategoryServiceMapper categoryServiceMapper){
        this.addCategory = addCategory;
        this.updateCategory = updateCategory;
        this.deleteCategory = deleteCategory;
        this.centralAudit = centralAudit;
        this.categoryServiceMapper = categoryServiceMapper;
    }

    // == Public Methods ==

    @Override
    public DTOCategoryResponse addNewCategory(DTOAddCategory dto) {
        final Category category = addCategory.handle(dto);
        centralAudit.info(null, EndpointsNameMethods.CATEGORY_UPDATE, AuditingStatus.SUCCESSFUL,
                AuditMessage.CATEGORY_CREATE_SUCCESS.getMessage());
        return categoryServiceMapper.toDto(category);
    }

    @Override
    public DTOCategoryResponse updateCategory(DTOUpdateCategory dto, long categoryId) {
        Category category = updateCategory.handle(dto, categoryId);
        centralAudit.info(null, EndpointsNameMethods.CATEGORY_UPDATE, AuditingStatus.SUCCESSFUL,
                AuditMessage.CATEGORY_UPDATE_SUCCESS.getMessage());
        return categoryServiceMapper.toDto(category);
    }

    @Override
    public void deleteCategory(long categoryId) {
        deleteCategory.handle(categoryId);
        centralAudit.info(null, EndpointsNameMethods.CATEGORY_DELETE, AuditingStatus.SUCCESSFUL, AuditMessage.CATEGORY_DELETE_SUCCESS.getMessage());
    }
}
