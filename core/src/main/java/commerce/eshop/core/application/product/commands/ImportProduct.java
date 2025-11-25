package commerce.eshop.core.application.product.commands;

import commerce.eshop.core.application.async.external.contracts.ImportClientProduct;
import commerce.eshop.core.application.product.factory.ProductFactory;
import commerce.eshop.core.application.product.validation.AuditedProductValidation;
import commerce.eshop.core.application.product.writer.ProductWriter;
import commerce.eshop.core.model.entity.Product;
import commerce.eshop.core.web.dto.requests.Products.DTOAddProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
public class ImportProduct {

    // == Fields ==
    private final ProductFactory productFactory;
    private final ProductWriter productWriter;
    private final AuditedProductValidation auditedProductValidation;
    private final ImportClientProduct importClientProduct;

    // == Constructors ==
    @Autowired
    public ImportProduct(ProductFactory productFactory, ProductWriter productWriter, AuditedProductValidation auditedProductValidation, ImportClientProduct importClientProduct) {
        this.productFactory = productFactory;
        this.productWriter = productWriter;
        this.auditedProductValidation = auditedProductValidation;
        this.importClientProduct = importClientProduct;
    }

    // == Public Methods ==
    @Transactional
    public Product handle(){
        Map<String, Object> importedProduct = importClientProduct.getProduct();
        DTOAddProduct productToAdd = productFactory.normaliseNewProduct(importedProduct);
        Product product = productFactory.handle(productToAdd.productName(), productToAdd.productDescription(), productToAdd);
        auditedProductValidation.checkIfProductExists(product.getProductName());
        return productWriter.save(product, "ImportNewProduct");
    }
}
