package commerce.eshop.core.application.product.commands;

import commerce.eshop.core.application.product.writer.ProductWriter;
import commerce.eshop.core.model.entity.Product;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RemoveProduct {
    // == Fields ==
    private final DomainLookupService domainLookupService;
    private final ProductWriter writer;

    // == Constructors ==
    @Autowired
    public RemoveProduct(DomainLookupService domainLookupService, ProductWriter writer) {
        this.domainLookupService = domainLookupService;
        this.writer = writer;
    }

    // == Public Methods ==
    @Transactional
    public void handle(long id){
        final Product product = domainLookupService.getProductOrThrow(id, EndpointsNameMethods.PRODUCT_REMOVE);
        writer.delete(product);
    }
}
