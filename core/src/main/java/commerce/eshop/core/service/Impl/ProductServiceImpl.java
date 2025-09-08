package commerce.eshop.core.service.Impl;

import commerce.eshop.core.model.entity.Category;
import commerce.eshop.core.model.entity.Product;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.util.enums.AuditMessage;
import commerce.eshop.core.util.enums.AuditingStatus;
import commerce.eshop.core.repository.CategoryRepo;
import commerce.eshop.core.repository.ProductCategoryRepo;
import commerce.eshop.core.repository.ProductRepo;
import commerce.eshop.core.service.AuditingService;
import commerce.eshop.core.service.ProductService;
import commerce.eshop.core.util.SortSanitizer;
import commerce.eshop.core.web.dto.requests.Products.DTOAddProduct;
import commerce.eshop.core.web.dto.response.Product.DTOProductResponse;
import commerce.eshop.core.web.mapper.ProductServiceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class ProductServiceImpl implements ProductService {

    // == Fields ==
    private final ProductRepo productRepo;
    private final CategoryRepo categoryRepo;
    private final ProductCategoryRepo productCategoryRepo;
    private final SortSanitizer sortSanitizer;
    private final AuditingService auditingService;
    private final ProductServiceMapper productServiceMapper;

    // == Whitelisting & Constraints
    /** For DTOProductResponse */
    public static final Map<String, String> PRODUCT_SORT_WHITELIST = Map.ofEntries(
            Map.entry("id", "productId"),
            Map.entry("name", "productName"),
            Map.entry("price", "productPrice")
    );

    // == Constructors ==
    @Autowired
    public ProductServiceImpl(ProductRepo productRepo, CategoryRepo categoryRepo, ProductCategoryRepo productCategoryRepo,
                              SortSanitizer sortSanitizer, AuditingService auditingService, ProductServiceMapper productServiceMapper){
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
        this.productCategoryRepo = productCategoryRepo;
        this.sortSanitizer = sortSanitizer;
        this.auditingService = auditingService;
        this.productServiceMapper = productServiceMapper;
    }


    // == Public Methods ==
    @Transactional
    @Override
    public DTOProductResponse addProduct(DTOAddProduct dto) {

        if(dto == null){
            IllegalArgumentException illegal = new IllegalArgumentException("Product Details can't be empty");
            auditingService.log(null, EndpointsNameMethods.PRODUCT_ADD, AuditingStatus.WARNING, illegal.toString());
            throw illegal;
        }

        String normalizedName = dto.productName().trim();
        String normalizedDesc = dto.productDescription().trim();

        if (productRepo.existsByProductNameIgnoreCase(normalizedName)){
            IllegalStateException illegal = new IllegalStateException("Product already exists");
            auditingService.log(null, EndpointsNameMethods.PRODUCT_ADD, AuditingStatus.WARNING, illegal.toString());
            throw illegal;
        }

        Product productToAdd = new Product(normalizedName, normalizedDesc, dto.productDetails(),
                dto.productAvailableStock(), dto.productPrice(), dto.isActive());

        try{
            productRepo.saveAndFlush(productToAdd);
            auditingService.log(null, EndpointsNameMethods.PRODUCT_ADD, AuditingStatus.SUCCESSFUL, AuditMessage.PRODUCT_ADD_SUCCESS.getMessage());
            return productServiceMapper.toDto(productToAdd);
        } catch (DataIntegrityViolationException dup) {
            auditingService.log(null, EndpointsNameMethods.PRODUCT_ADD, AuditingStatus.ERROR, dup.toString());
            throw dup;
        }
    }

    @Transactional(readOnly = true)
    @Override
    public DTOProductResponse getProduct(long id) {

        final Product product;

        try {
            product = productRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Product with the provided ID doesn't exist"));
            auditingService.log(null, EndpointsNameMethods.PRODUCT_GET, AuditingStatus.SUCCESSFUL, AuditMessage.PRODUCT_GET_SUCCESS.getMessage());
            return productServiceMapper.toDto(product);
        } catch (NoSuchElementException e){
            auditingService.log(null, EndpointsNameMethods.PRODUCT_GET, AuditingStatus.WARNING, e.toString());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Page<DTOProductResponse> getAllProducts(long categoryId, Pageable pageable) {
        Pageable p = sortSanitizer.sanitize(pageable, PRODUCT_SORT_WHITELIST, 25);
        Page<Product> products = productRepo.findAllByCategoryId(categoryId, p);
        auditingService.log(null, EndpointsNameMethods.PRODUCT_GET_ALL, AuditingStatus.SUCCESSFUL,
                AuditMessage.PRODUCT_GET_ALL_SUCCESS.getMessage());
        return products.map(productServiceMapper::toDto);
    }

    @Transactional
    @Override
    public void increaseQuantity(long productId, int quantity) {

        if (quantity <= 0){
            IllegalArgumentException illegal = new IllegalArgumentException("Quantity can't be negative/or zero for increasing the stock");
            auditingService.log(null, EndpointsNameMethods.PRODUCT_INCREASE_QTY, AuditingStatus.WARNING, illegal.toString());
            throw illegal;
        }

        final  Product product;
        try {
            product = productRepo.findById(productId).orElseThrow(
                    () -> new NoSuchElementException("Product couldn't be found"));
        } catch (NoSuchElementException e){
            auditingService.log(null, EndpointsNameMethods.PRODUCT_INCREASE_QTY, AuditingStatus.WARNING, e.toString());
            throw e;
        }


        try {
            int newStock = Math.addExact(product.getProductAvailableStock(), quantity); // overflow-safe
            product.setProductAvailableStock(newStock);

            productRepo.saveAndFlush(product);
            auditingService.log(null, EndpointsNameMethods.PRODUCT_INCREASE_QTY, AuditingStatus.SUCCESSFUL,
                    AuditMessage.PRODUCT_INCREASE_QTY_SUCCESS.getMessage());
        } catch (DataIntegrityViolationException dup){
            auditingService.log(null, EndpointsNameMethods.PRODUCT_INCREASE_QTY, AuditingStatus.ERROR, dup.toString());
            throw dup;
        } catch (ArithmeticException overflow){
            auditingService.log(null, EndpointsNameMethods.PRODUCT_INCREASE_QTY,
                    AuditingStatus.ERROR, "STOCK_OVERFLOW id=" + productId + " by=" + quantity);
            throw new IllegalStateException("Stock overflow for product id=" + productId);
        }
    }

    // Internal, shouldn't be exposed.
    @Transactional
    @Override
    public void decreaseQuantity(long productId, int quantity) {

        final  Product product;

        try {
            product = productRepo.findById(productId).orElseThrow( () ->
                    new NoSuchElementException("Product couldn't be found"));
        } catch (NoSuchElementException e){
            auditingService.log(null, EndpointsNameMethods.PRODUCT_DECREASE_QTY, AuditingStatus.WARNING, e.toString());
            throw e;
        }

        if (product.getProductAvailableStock() < quantity){
            IllegalStateException illegal = new IllegalStateException("Insufficient stock: available="
                    + product.getProductAvailableStock() + ", requested=" + quantity);
            auditingService.log(null, EndpointsNameMethods.PRODUCT_DECREASE_QTY, AuditingStatus.WARNING, illegal.toString());
            throw illegal;
        }

        try {
            int newStock = Math.subtractExact(product.getProductAvailableStock(), quantity);
            product.setProductAvailableStock(newStock);
            productRepo.saveAndFlush(product);
            auditingService.log(null, EndpointsNameMethods.PRODUCT_DECREASE_QTY, AuditingStatus.SUCCESSFUL,
                    AuditMessage.PRODUCT_DECREASE_QTY_SUCCESS.getMessage());
        } catch (DataIntegrityViolationException dup){
            auditingService.log(null, EndpointsNameMethods.PRODUCT_DECREASE_QTY, AuditingStatus.ERROR, dup.toString());
            throw dup;
        }
    }

    @Transactional
    @Override
    public void linkProduct(long productId, long categoryId) {

        final Product product;

        try {
            product = productRepo.findById(productId).orElseThrow(() ->
                    new NoSuchElementException("Product with product ID = " + productId + " doesn't exist."));
        } catch (NoSuchElementException e){
            auditingService.log(null, EndpointsNameMethods.PRODUCT_LINK, AuditingStatus.WARNING, e.toString());
            throw e;
        }

        final Category category;

        try {
            category = categoryRepo.findById((categoryId)).orElseThrow(() ->
                    new NoSuchElementException("Category with Category ID " + categoryId + " doesn't exists"));
        } catch (NoSuchElementException e){
            auditingService.log(null, EndpointsNameMethods.PRODUCT_LINK, AuditingStatus.WARNING, e.toString());
            throw e;
        }

        try {
            int inserted = productCategoryRepo.linkIfAbsent(productId, categoryId);
            if (inserted == 1) {
                auditingService.log(null, EndpointsNameMethods.PRODUCT_LINK, AuditingStatus.SUCCESSFUL,
                        "LINKED productId=" + productId + " categoryId=" + categoryId);
            } else {
                auditingService.log(null, EndpointsNameMethods.PRODUCT_LINK, AuditingStatus.SUCCESSFUL,
                        "ALREADY_LINKED productId=" + productId + " categoryId=" + categoryId);
            }
        } catch (DataIntegrityViolationException dup){
            auditingService.log(null, EndpointsNameMethods.PRODUCT_LINK, AuditingStatus.ERROR, dup.toString());
            throw dup;
        }
    }

    @Transactional
    @Override
    public void delinkProduct(long productId, long categoryId) {

        try {
            int removed = productCategoryRepo.deleteByProduct_ProductIdAndCategory_CategoryId(productId, categoryId);
            if (removed == 0) {
                NoSuchElementException illegal = new NoSuchElementException("LINK_NOT_FOUND productId=" + productId + " categoryId=" + categoryId);
                auditingService.log(null, EndpointsNameMethods.PRODUCT_UNLINK, AuditingStatus.WARNING, illegal.toString());
                throw illegal;
            }
            auditingService.log(null, EndpointsNameMethods.PRODUCT_UNLINK, AuditingStatus.SUCCESSFUL,
                    AuditMessage.PRODUCT_UNLINK_SUCCESS.getMessage());
        } catch (DataIntegrityViolationException dup){
            auditingService.log(null, EndpointsNameMethods.PRODUCT_UNLINK, AuditingStatus.ERROR, dup.toString());
            throw dup;
        }
    }

    @Transactional
    @Override
    public void removeProduct(long id) {
        Product product;
        try {
            product = productRepo.findById(id).orElseThrow(
                    () -> new NoSuchElementException("The product doesn't exist.")
            );
        } catch (NoSuchElementException e){
            auditingService.log(null, EndpointsNameMethods.PRODUCT_REMOVE, AuditingStatus.WARNING, e.toString());
            throw e;
        }

        try {
            productRepo.delete(product);
            productRepo.flush();              // fail fast on FK constraints
            auditingService.log(null, EndpointsNameMethods.PRODUCT_REMOVE, AuditingStatus.SUCCESSFUL,
                    AuditMessage.PRODUCT_REMOVE_SUCCESS.getMessage());
        } catch (DataIntegrityViolationException dup){
            auditingService.log(null, EndpointsNameMethods.PRODUCT_REMOVE, AuditingStatus.ERROR, dup.toString());
            throw dup;
        }
    }
}
