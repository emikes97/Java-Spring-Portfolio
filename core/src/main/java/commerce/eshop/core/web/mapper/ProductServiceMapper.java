package commerce.eshop.core.web.mapper;

import commerce.eshop.core.model.entity.Product;
import commerce.eshop.core.web.dto.response.Product.DTOProductResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductServiceMapper {

    public DTOProductResponse toDto(Product p){
        return new DTOProductResponse(
                p.getProductId(),
                p.getProductName(),
                p.getDescription(),
                p.getProductDetails(),
                p.getProductAvailableStock(),
                p.getPrice()
        );}
}
