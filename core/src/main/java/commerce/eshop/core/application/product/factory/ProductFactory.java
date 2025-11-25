package commerce.eshop.core.application.product.factory;

import commerce.eshop.core.model.entity.Product;
import commerce.eshop.core.web.dto.requests.Products.DTOAddProduct;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ProductFactory {

    // == Public Methods ==
    public Product handle(String normalizedName, String normalizedDesc, DTOAddProduct dto){
        return new Product(normalizedName, normalizedDesc, dto.productDetails(), dto.productAvailableStock(), dto.productPrice(), dto.isActive());
    }

    public DTOAddProduct normaliseNewProduct(Map<String, Object> item){
        int randomQuantity = ThreadLocalRandom.current().nextInt(10, 100);
        String name = String.valueOf(item.getOrDefault("title", "Unnamed Product"));
        String desc = String.valueOf(item.getOrDefault("description", "No description available"));

        Number rawPrice = (Number) item.get("price");
        BigDecimal price = rawPrice != null
                ? BigDecimal.valueOf(rawPrice.doubleValue())
                : BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(10.50, 350.00));

        return new DTOAddProduct(name, desc, item, randomQuantity, price, true);
    }
}
