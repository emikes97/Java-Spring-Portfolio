package commerce.eshop.core.service.Impl;

import commerce.eshop.core.model.entity.Category;
import commerce.eshop.core.model.entity.Product;
import commerce.eshop.core.service.DomainLookupService;
import commerce.eshop.core.util.CentralAudit;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.util.enums.AuditMessage;
import commerce.eshop.core.util.enums.AuditingStatus;
import commerce.eshop.core.repository.CategoryRepo;
import commerce.eshop.core.repository.ProductCategoryRepo;
import commerce.eshop.core.repository.ProductRepo;
import commerce.eshop.core.service.ProductService;
import commerce.eshop.core.util.SortSanitizer;
import commerce.eshop.core.util.sort.ProductSort;
import commerce.eshop.core.web.dto.requests.Products.DTOAddProduct;
import commerce.eshop.core.web.dto.response.Product.DTOProductResponse;
import commerce.eshop.core.web.mapper.ProductServiceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class ProductServiceImpl implements ProductService {

    // == Fields ==
    private final ProductRepo productRepo;
    private final CategoryRepo categoryRepo;
    private final ProductCategoryRepo productCategoryRepo;
    private final SortSanitizer sortSanitizer;
    private final CentralAudit centralAudit;
    private final ProductServiceMapper productServiceMapper;
    private final DomainLookupService domainLookupService;

    // == Constructors ==
    @Autowired
    public ProductServiceImpl(ProductRepo productRepo, CategoryRepo categoryRepo, ProductCategoryRepo productCategoryRepo,
                              SortSanitizer sortSanitizer, CentralAudit centralAudit, ProductServiceMapper productServiceMapper,
                              DomainLookupService domainLookupService){
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
        this.productCategoryRepo = productCategoryRepo;
        this.sortSanitizer = sortSanitizer;
        this.centralAudit = centralAudit;
        this.productServiceMapper = productServiceMapper;
        this.domainLookupService = domainLookupService;
    }


    // == Public Methods ==
    @Transactional
    @Override
    public DTOProductResponse addProduct(DTOAddProduct dto) {

        if(dto == null){
            IllegalArgumentException illegal = new IllegalArgumentException("Product Details can't be empty");
            throw centralAudit.audit(illegal, null, EndpointsNameMethods.PRODUCT_ADD, AuditingStatus.WARNING, illegal.toString());
        }

        String normalizedName = dto.productName().trim();
        String normalizedDesc = dto.productDescription().trim();

        if (productRepo.existsByProductNameIgnoreCase(normalizedName)){
            IllegalStateException illegal = new IllegalStateException("Product already exists");
            throw centralAudit.audit(illegal, null, EndpointsNameMethods.PRODUCT_ADD, AuditingStatus.WARNING, illegal.toString());
        }

        Product productToAdd = new Product(normalizedName, normalizedDesc, dto.productDetails(),
                dto.productAvailableStock(), dto.productPrice(), dto.isActive());

        try{
            productRepo.saveAndFlush(productToAdd);
            centralAudit.info(null, EndpointsNameMethods.PRODUCT_ADD, AuditingStatus.SUCCESSFUL, AuditMessage.PRODUCT_ADD_SUCCESS.getMessage());
            return productServiceMapper.toDto(productToAdd);
        } catch (DataIntegrityViolationException dup) {
            throw centralAudit.audit(dup,null, EndpointsNameMethods.PRODUCT_ADD, AuditingStatus.ERROR, dup.toString());
        }
    }

    @Transactional(readOnly = true)
    @Override
    public DTOProductResponse getProduct(long id) {
        final Product product = domainLookupService.getProductOrThrow(id, EndpointsNameMethods.PRODUCT_GET);
        return productServiceMapper.toDto(product);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<DTOProductResponse> getAllProducts(long categoryId, Pageable pageable) {
        Pageable p = sortSanitizer.sanitize(pageable, ProductSort.PRODUCT_SORT_WHITELIST, ProductSort.MAX_PAGE_SIZE);
        Page<Product> products = productRepo.findAllByCategoryId(categoryId, p);
        centralAudit.info(null, EndpointsNameMethods.PRODUCT_GET_ALL, AuditingStatus.SUCCESSFUL,
                AuditMessage.PRODUCT_GET_ALL_SUCCESS.getMessage());
        return products.map(productServiceMapper::toDto);
    }

    @Transactional
    @Override
    public void increaseQuantity(long productId, int quantity) {

        if (quantity <= 0){
            IllegalArgumentException illegal = new IllegalArgumentException("Quantity can't be negative/or zero for increasing the stock");
            throw centralAudit.audit(illegal, null, EndpointsNameMethods.PRODUCT_INCREASE_QTY, AuditingStatus.WARNING, illegal.toString());
        }

        final  Product product = domainLookupService.getProductOrThrow(productId, EndpointsNameMethods.PRODUCT_INCREASE_QTY);

        try {
            int newStock = Math.addExact(product.getProductAvailableStock(), quantity); // overflow-safe
            product.setProductAvailableStock(newStock);

            productRepo.saveAndFlush(product);
            centralAudit.info(null, EndpointsNameMethods.PRODUCT_INCREASE_QTY, AuditingStatus.SUCCESSFUL,
                    AuditMessage.PRODUCT_INCREASE_QTY_SUCCESS.getMessage());
        } catch (DataIntegrityViolationException dup){
            throw centralAudit.audit(dup,null, EndpointsNameMethods.PRODUCT_INCREASE_QTY, AuditingStatus.ERROR, dup.toString());
        } catch (ArithmeticException overflow){
            throw centralAudit.audit(new ArithmeticException("Stock overflow for product id=" + productId), null,
                    EndpointsNameMethods.PRODUCT_INCREASE_QTY, AuditingStatus.ERROR,
                    "STOCK_OVERFLOW id=" + productId + " by=" + quantity);
        }
    }

    // Internal, shouldn't be exposed.
    @Transactional
    @Override
    public void decreaseQuantity(long productId, int quantity) {

        final  Product product = domainLookupService.getProductOrThrow(productId, EndpointsNameMethods.PRODUCT_DECREASE_QTY);

        if (product.getProductAvailableStock() < quantity){
            IllegalStateException illegal = new IllegalStateException("Insufficient stock: available="
                    + product.getProductAvailableStock() + ", requested=" + quantity);
           throw centralAudit.audit(illegal, null, EndpointsNameMethods.PRODUCT_DECREASE_QTY, AuditingStatus.WARNING, illegal.toString());
        }

        try {
            int newStock = Math.subtractExact(product.getProductAvailableStock(), quantity);
            product.setProductAvailableStock(newStock);
            productRepo.saveAndFlush(product);
            centralAudit.info(null, EndpointsNameMethods.PRODUCT_DECREASE_QTY, AuditingStatus.SUCCESSFUL,
                    AuditMessage.PRODUCT_DECREASE_QTY_SUCCESS.getMessage());
        } catch (DataIntegrityViolationException dup){
            throw centralAudit.audit(dup, null, EndpointsNameMethods.PRODUCT_DECREASE_QTY, AuditingStatus.ERROR, dup.toString());
        }
    }

    @Transactional
    @Override
    public void linkProduct(long productId, long categoryId) {

        final Product product = domainLookupService.getProductOrThrow(productId, EndpointsNameMethods.PRODUCT_LINK);

        final Category category = domainLookupService.getCategoryOrThrow(categoryId, EndpointsNameMethods.PRODUCT_LINK);

        try {
            int inserted = productCategoryRepo.linkIfAbsent(productId, categoryId);
            if (inserted == 1) {
                centralAudit.info(null, EndpointsNameMethods.PRODUCT_LINK, AuditingStatus.SUCCESSFUL,
                        "LINKED productId=" + productId + " categoryId=" + categoryId);
            } else {
                centralAudit.warn(null, EndpointsNameMethods.PRODUCT_LINK, AuditingStatus.WARNING,
                        "ALREADY_LINKED productId=" + productId + " categoryId=" + categoryId);
            }
        } catch (DataIntegrityViolationException dup){
            throw centralAudit.audit(dup,null, EndpointsNameMethods.PRODUCT_LINK, AuditingStatus.ERROR, dup.toString());
        }
    }

    @Transactional
    @Override
    public void delinkProduct(long productId, long categoryId) {

        try {
            int removed = productCategoryRepo.deleteByProduct_ProductIdAndCategory_CategoryId(productId, categoryId);
            if (removed == 0) {
                NoSuchElementException illegal = new NoSuchElementException("LINK_NOT_FOUND productId=" + productId + " categoryId=" + categoryId);
                throw centralAudit.audit(illegal,null, EndpointsNameMethods.PRODUCT_UNLINK, AuditingStatus.WARNING, illegal.toString());
            }
            centralAudit.info(null, EndpointsNameMethods.PRODUCT_UNLINK, AuditingStatus.SUCCESSFUL,
                    AuditMessage.PRODUCT_UNLINK_SUCCESS.getMessage());
        } catch (DataIntegrityViolationException dup){
            throw centralAudit.audit(dup, null, EndpointsNameMethods.PRODUCT_UNLINK, AuditingStatus.ERROR, dup.toString());
        }
    }

    @Transactional
    @Override
    public void removeProduct(long id) {

        final Product product = domainLookupService.getProductOrThrow(id, EndpointsNameMethods.PRODUCT_REMOVE);

        try {
            productRepo.delete(product);
            productRepo.flush();              // fail fast on FK constraints
            centralAudit.info(null, EndpointsNameMethods.PRODUCT_REMOVE, AuditingStatus.SUCCESSFUL,
                    AuditMessage.PRODUCT_REMOVE_SUCCESS.getMessage());
        } catch (DataIntegrityViolationException dup){
            centralAudit.audit(dup, null, EndpointsNameMethods.PRODUCT_REMOVE, AuditingStatus.ERROR, dup.toString());
        }
    }
}
