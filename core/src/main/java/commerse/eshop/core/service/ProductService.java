package commerse.eshop.core.service;

import commerse.eshop.core.web.dto.requests.Products.DTOAddProduct;
import commerse.eshop.core.web.dto.response.Product.DTOProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ProductService {

    // == Add New Product
    DTOProductResponse addProduct(DTOAddProduct dto);

    // == Remove Product
    void removeProduct(long id);

    // == Fetch Product
    Optional<DTOProductResponse> getProduct(long id);

    // == Fetch all products of a category
    Page<DTOProductResponse> getAllProducts(String category, long categoryId, Pageable pageable);
}
