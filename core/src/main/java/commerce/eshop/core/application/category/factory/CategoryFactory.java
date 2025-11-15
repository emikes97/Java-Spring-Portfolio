package commerce.eshop.core.application.category.factory;

import commerce.eshop.core.model.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryFactory {

    public Category handle(String catName, String catDesc){
        return new Category(catName, catDesc);
    }
}
