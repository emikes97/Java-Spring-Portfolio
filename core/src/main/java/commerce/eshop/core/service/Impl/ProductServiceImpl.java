package commerce.eshop.core.service.Impl;

import commerce.eshop.core.application.product.commands.AddProduct;
import commerce.eshop.core.application.product.commands.ChangeProductQuantity;
import commerce.eshop.core.application.product.commands.ProductLinkManager;
import commerce.eshop.core.application.product.commands.RemoveProduct;
import commerce.eshop.core.application.product.queries.ProductQueries;
import commerce.eshop.core.model.entity.Product;
import commerce.eshop.core.util.CentralAudit;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.util.enums.AuditMessage;
import commerce.eshop.core.util.enums.AuditingStatus;
import commerce.eshop.core.service.ProductService;
import commerce.eshop.core.web.dto.requests.Products.DTOAddProduct;
import commerce.eshop.core.web.dto.response.Product.DTOProductResponse;
import commerce.eshop.core.web.mapper.ProductServiceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class ProductServiceImpl implements ProductService {

    // == Fields ==
    private final AddProduct addProduct;
    private final ProductQueries queries;
    private final ChangeProductQuantity changeProductQuantity;
    private final ProductLinkManager productLinkManager;
    private final RemoveProduct removeProduct;
    private final CentralAudit centralAudit;
    private final ProductServiceMapper productServiceMapper;

    // == Constructors ==
    @Autowired
    public ProductServiceImpl(AddProduct addProduct, ProductQueries queries, ChangeProductQuantity changeProductQuantity,
                              ProductLinkManager productLinkManager, RemoveProduct removeProduct,
                              CentralAudit centralAudit, ProductServiceMapper productServiceMapper){
        this.addProduct = addProduct;
        this.queries = queries;
        this.changeProductQuantity = changeProductQuantity;
        this.productLinkManager = productLinkManager;
        this.removeProduct = removeProduct;
        this.centralAudit = centralAudit;
        this.productServiceMapper = productServiceMapper;
    }


    // == Public Methods ==
    @Override
    public DTOProductResponse addProduct(DTOAddProduct dto) {
        final Product product = addProduct.handle(dto);
        centralAudit.info(null, EndpointsNameMethods.PRODUCT_ADD, AuditingStatus.SUCCESSFUL, AuditMessage.PRODUCT_ADD_SUCCESS.getMessage());
        return productServiceMapper.toDto(product);
    }

    @Override
    public DTOProductResponse getProduct(long id) {
        final Product product = queries.getProduct(id);
        centralAudit.info(null, EndpointsNameMethods.PRODUCT_GET, AuditingStatus.SUCCESSFUL, AuditMessage.PRODUCT_GET_SUCCESS.getMessage());
        return productServiceMapper.toDto(product);
    }

    @Override
    public Page<DTOProductResponse> getAllProducts(long categoryId, Pageable pageable) {
        Page<Product> products = queries.getAllProducts(categoryId, pageable);
        centralAudit.info(null, EndpointsNameMethods.PRODUCT_GET_ALL, AuditingStatus.SUCCESSFUL,
                AuditMessage.PRODUCT_GET_ALL_SUCCESS.getMessage());
        return products.map(productServiceMapper::toDto);
    }

    @Override
    public void increaseQuantity(long productId, int quantity) {
        changeProductQuantity.increaseQuantity(productId, quantity);
        centralAudit.info(null, EndpointsNameMethods.PRODUCT_INCREASE_QTY, AuditingStatus.SUCCESSFUL,
                AuditMessage.PRODUCT_INCREASE_QTY_SUCCESS.getMessage());
    }

    // Internal, shouldn't be exposed.
    @Override
    public void decreaseQuantity(long productId, int quantity) {
        changeProductQuantity.decreaseQuantity(productId, quantity);
        centralAudit.info(null, EndpointsNameMethods.PRODUCT_DECREASE_QTY, AuditingStatus.SUCCESSFUL,
                AuditMessage.PRODUCT_DECREASE_QTY_SUCCESS.getMessage());
    }

    @Override
    public void linkProduct(long productId, long categoryId) {
        int inserted = productLinkManager.link(productId, categoryId);
        if (inserted == 1) {
            centralAudit.info(null, EndpointsNameMethods.PRODUCT_LINK, AuditingStatus.SUCCESSFUL,
                    "LINKED productId=" + productId + " categoryId=" + categoryId);
        } else {
            centralAudit.warn(null, EndpointsNameMethods.PRODUCT_LINK, AuditingStatus.WARNING,
                    "ALREADY_LINKED productId=" + productId + " categoryId=" + categoryId);
        }
    }

    @Override
    public void delinkProduct(long productId, long categoryId) {
        int removed = productLinkManager.unlink(productId, categoryId);
        if (removed == 0) {
            NoSuchElementException illegal = new NoSuchElementException("LINK_NOT_FOUND productId=" + productId + " categoryId=" + categoryId);
            throw centralAudit.audit(illegal,null, EndpointsNameMethods.PRODUCT_UNLINK, AuditingStatus.WARNING, illegal.toString());
        } else {
            centralAudit.info(null, EndpointsNameMethods.PRODUCT_UNLINK, AuditingStatus.SUCCESSFUL,
                    AuditMessage.PRODUCT_UNLINK_SUCCESS.getMessage());
        }
    }

    @Override
    public void removeProduct(long id) {
        removeProduct.handle(id);
        centralAudit.info(null, EndpointsNameMethods.PRODUCT_REMOVE, AuditingStatus.SUCCESSFUL,
                AuditMessage.PRODUCT_REMOVE_SUCCESS.getMessage());
    }
}
