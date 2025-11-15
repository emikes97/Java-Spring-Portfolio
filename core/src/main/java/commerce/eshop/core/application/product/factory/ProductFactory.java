package commerce.eshop.core.application.product.factory;

import commerce.eshop.core.model.entity.Product;
import commerce.eshop.core.web.dto.requests.Products.DTOAddProduct;
import org.springframework.stereotype.Component;

@Component
public class ProductFactory {

    // == Public Methods ==
    public Product handle(String normalizedName, String normalizedDesc, DTOAddProduct dto){
        return new Product(normalizedName, normalizedDesc, dto.productDetails(), dto.productAvailableStock(), dto.productPrice(), dto.isActive());
    }
}
