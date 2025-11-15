package commerce.eshop.core.application.product.commands;

import commerce.eshop.core.application.product.factory.ProductFactory;
import commerce.eshop.core.application.product.validation.AuditedProductValidation;
import commerce.eshop.core.application.product.writer.ProductWriter;
import commerce.eshop.core.model.entity.Product;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.web.dto.requests.Products.DTOAddProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AddProduct {

    // == Fields ==
    private final AuditedProductValidation validation;
    private final ProductFactory factory;
    private final ProductWriter writer;

    // == Constructors ==
    @Autowired
    public AddProduct(AuditedProductValidation validation, ProductFactory factory, ProductWriter writer) {
        this.validation = validation;
        this.factory = factory;
        this.writer = writer;
    }

    // == Public Methods ==
    @Transactional
    public Product handle(DTOAddProduct dto){
        validation.checkData(dto);
        String normalizedName = dto.productName().trim();
        validation.checkIfProductExists(normalizedName); // -> If exists fail early
        String normalizedDesc = dto.productDescription().trim();
        Product product = factory.handle(normalizedName, normalizedDesc, dto);
        product = writer.save(product, EndpointsNameMethods.PRODUCT_ADD);
        return product;
    }

}
