package commerse.eshop.core.service.Impl;

import commerse.eshop.core.model.entity.Category;
import commerse.eshop.core.model.entity.Product;
import commerse.eshop.core.model.entity.ProductCategory;
import commerse.eshop.core.repository.CategoryRepo;
import commerse.eshop.core.repository.ProductCategoryRepo;
import commerse.eshop.core.repository.ProductRepo;
import commerse.eshop.core.service.ProductService;
import commerse.eshop.core.web.dto.requests.Products.DTOAddProduct;
import commerse.eshop.core.web.dto.response.Product.DTOProductResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepo productRepo;
    private final CategoryRepo categoryRepo;
    private final ProductCategoryRepo productCategoryRepo;

    @Autowired
    public ProductServiceImpl(ProductRepo productRepo, CategoryRepo categoryRepo, ProductCategoryRepo productCategoryRepo){
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
        this.productCategoryRepo = productCategoryRepo;
    }

    @Transactional
    @Override
    public DTOProductResponse addProduct(DTOAddProduct dto) {

        String normalizedName = dto.productName().trim();
        String normalizedDesc = dto.productDescription().trim();

        if (productRepo.existsByProductNameIgnoreCase(normalizedName))
            throw new IllegalStateException("Product already exists");

        Product productToAdd = new Product(normalizedName, normalizedDesc, dto.productDetails(),
                dto.productAvailableStock(), dto.productPrice(), dto.isActive());

        try{
            productRepo.save(productToAdd);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Product already exists: " + normalizedName);
        }

        return toDto(productToAdd);
    }

    @Transactional(readOnly = true)
    @Override
    public DTOProductResponse getProduct(long id) {
        Product product = productRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Product with the provided ID doesn't exist"));
        return toDto(product);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<DTOProductResponse> getAllProducts(long categoryId, Pageable pageable) {
        Page<Product> products = productRepo.findAllByCategoryId(categoryId, pageable);
        return products.map(this::toDto);
    }

    @Transactional
    @Override
    public void increaseQuantity(long productId, int quantity) {

        if (quantity <= 0)
            throw new RuntimeException("Quantity can't be negative/or zero for increasing the stock");

        Product product = productRepo.findById(productId).orElseThrow( () ->
                new NoSuchElementException("Product couldn't be found"));

        product.setProductAvailableStock(product.getProductAvailableStock() + quantity);

        productRepo.save(product);
    }

    @Transactional
    @Override
    public void decreaseQuantity(long productId, int quantity) {
        Product product = productRepo.findById(productId).orElseThrow( () ->
                new NoSuchElementException("Product couldn't be found"));

        if (product.getProductAvailableStock() < quantity)
            throw new IllegalStateException("Insufficient stock: available=" + product.getProductAvailableStock() + ", requested=" + quantity);

        product.setProductAvailableStock(product.getProductAvailableStock() - quantity);
        productRepo.save(product);
    }

    @Transactional
    @Override
    public void linkProduct(long productId, long categoryId) {

        Product product = productRepo.findById(productId).orElseThrow(() ->
                new NoSuchElementException("Product with product ID = " + productId + " doesn't exist."));
        Category category = categoryRepo.findById((categoryId)).orElseThrow(() ->
                new NoSuchElementException("Category with Category ID " + categoryId + " doesn't exists"));

        if (!productCategoryRepo.existsByProduct_ProductIdAndCategory_CategoryId(productId, categoryId)) {
            ProductCategory productCategory = new ProductCategory();
            productCategory.setProduct(product);
            productCategory.setCategory(category);
            productCategoryRepo.save(productCategory);
        }

        // Do nothing if link exists
    }

    @Transactional
    @Override
    public void delinkProduct(long productId, long categoryId) {
        int removed = productCategoryRepo.deleteByProduct_ProductIdAndCategory_CategoryId(productId, categoryId);
        if (removed == 0) {
            throw new NoSuchElementException("Link doesn't exist");
        }
    }

    @Transactional
    @Override
    public void removeProduct(long id) {
        productRepo.deleteById(id);
    }

    private DTOProductResponse toDto(Product p){
        return new DTOProductResponse(
                p.getProductId(),
                p.getProductName(),
                p.getDescription(),
                p.getProductDetails(),
                p.getProductAvailableStock(),
                p.getPrice()
        );}
}
