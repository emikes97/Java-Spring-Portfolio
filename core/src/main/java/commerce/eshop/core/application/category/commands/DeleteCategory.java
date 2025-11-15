package commerce.eshop.core.application.category.commands;

import commerce.eshop.core.application.category.writer.CategoryWriter;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DeleteCategory {

    // == Fields ==
    private final CategoryWriter writer;

    // == Constructors ==
    @Autowired
    public DeleteCategory(CategoryWriter writer) {
        this.writer = writer;
    }

    // == Public Methods ==
    @Transactional
    public void handle(long id){
        writer.delete(id, EndpointsNameMethods.CATEGORY_DELETE);
    }
}
