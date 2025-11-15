package commerce.eshop.core.application.category.commands;

import commerce.eshop.core.application.category.factory.CategoryFactory;
import commerce.eshop.core.application.category.validation.AuditedCategoryValidation;
import commerce.eshop.core.application.category.writer.CategoryWriter;
import commerce.eshop.core.model.entity.Category;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.web.dto.requests.Category.DTOAddCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AddCategory {

    // == Fields ==
    private final DomainLookupService domainLookupService;
    private final AuditedCategoryValidation validation;
    private final CategoryFactory factory;
    private final CategoryWriter writer;

    // == Constructors ==
    @Autowired
    public AddCategory(DomainLookupService domainLookupService, AuditedCategoryValidation validation, CategoryFactory factory, CategoryWriter writer) {
        this.domainLookupService = domainLookupService;
        this.validation = validation;
        this.factory = factory;
        this.writer = writer;
    }

    // == Public Methods ==
    @Transactional
    public Category handle(DTOAddCategory dto){
        boolean duplicate = domainLookupService.checkIfCatExists(dto.categoryName());
        validation.checkIfCategoryExists(duplicate);
        Category category = factory.handle(dto.categoryName(), dto.categoryDescription());
        category = writer.save(category, EndpointsNameMethods.CATEGORY_CREATE);
        return category;
    }
}
