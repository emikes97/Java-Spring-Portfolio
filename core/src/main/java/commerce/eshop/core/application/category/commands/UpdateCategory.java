package commerce.eshop.core.application.category.commands;

import commerce.eshop.core.application.category.writer.CategoryWriter;
import commerce.eshop.core.model.entity.Category;
import commerce.eshop.core.service.DomainLookupService;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.web.dto.requests.Category.DTOUpdateCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class UpdateCategory {

    // == Fields ==
    private final DomainLookupService domainLookupService;
    private final CategoryWriter writer;

    // == Constructors ==
    @Autowired
    public UpdateCategory(DomainLookupService domainLookupService, CategoryWriter writer) {
        this.domainLookupService = domainLookupService;
        this.writer = writer;
    }

    // == Public Methods ==
    @Transactional
    public Category handle(DTOUpdateCategory dto, long categoryId){

        Category category = domainLookupService.getCategoryOrThrow(categoryId, EndpointsNameMethods.CATEGORY_UPDATE);

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

        category = writer.save(category, EndpointsNameMethods.CATEGORY_UPDATE);
        return category;
    }
}
