package commerse.eshop.core.web.controller;

import commerse.eshop.core.service.ProductService;
import commerse.eshop.core.web.dto.requests.Products.DTOAddProduct;
import commerse.eshop.core.web.dto.response.Product.DTOProductResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService){
        this.productService = productService;
    }

    // Add new product
    ///
    @PostMapping
    public DTOProductResponse addProduct(@RequestBody @Valid DTOAddProduct dto){
        return productService.addProduct(dto);
    }

    // Get the product details
    ///
    @GetMapping("/{productId}")
    public DTOProductResponse getProduct(@PathVariable long productId){
        return productService.getProduct(productId);
    }

    // Get all products
    ///
    @GetMapping("/category/{categoryId}")
    public Page<DTOProductResponse> getAllProducts(@PathVariable long categoryId, Pageable pageable){
        return productService.getAllProducts(categoryId, pageable);
    }

    // Increase Quantity of products
    // @PreAuthorize("hasRole('ADMIN')")
    ///
    @PostMapping("/{productId}")
    public void increaseQuantity(@PathVariable long productId, @RequestParam @Min(1) int quantity){
        productService.increaseQuantity(productId, quantity);
    }

    // Link product
    // @PreAuthorize("hasRole('ADMIN')")
    ///
    @PutMapping("/{productId}/categories/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void link(@PathVariable long productId, @PathVariable long categoryId){
        productService.linkProduct(productId, categoryId);
    }

    // Unlink product
    // @PreAuthorize("hasRole('ADMIN')")
    ///
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{productId}/categories/{categoryId}")
    public void unlink(@PathVariable long productId, @PathVariable long categoryId){
        productService.delinkProduct(productId, categoryId);
    }

    // Delete product
    // @PreAuthorize("hasRole('ADMIN')")
    ///
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{productId}")
    public void removeProduct(@PathVariable long productId){
        productService.removeProduct(productId);
    }
}
