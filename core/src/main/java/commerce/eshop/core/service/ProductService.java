package commerce.eshop.core.service;

import commerce.eshop.core.web.dto.requests.Products.DTOAddProduct;
import commerce.eshop.core.web.dto.response.Product.DTOProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    // == Add New Product
    DTOProductResponse addProduct(DTOAddProduct dto);
    DTOProductResponse importRandomProduct();

    // == Fetch Product
    DTOProductResponse getProduct(long id);

    // == Fetch all products of a category
    Page<DTOProductResponse> getAllProducts(long categoryId, Pageable pageable);

    // == Increase quantity of a product
    void increaseQuantity(long productId, int quantity);

    // == Decrease quantity of a product
    void decreaseQuantity(long productId, int quantity);

    // == Link Product with Category
    void linkProduct(long productId, long categoryId);

    // == De-link product from category
    void delinkProduct(long productId, long categoryId);

    // == Remove Product
    void removeProduct(long id);
}
