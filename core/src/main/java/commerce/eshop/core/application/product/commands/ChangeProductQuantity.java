package commerce.eshop.core.application.product.commands;

import commerce.eshop.core.application.product.validation.AuditedProductValidation;
import commerce.eshop.core.application.product.writer.ProductWriter;
import commerce.eshop.core.model.entity.Product;
import commerce.eshop.core.service.DomainLookupService;
import commerce.eshop.core.util.CentralAudit;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.util.enums.AuditingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ChangeProductQuantity {

    // == Fields ==
    private final DomainLookupService domainLookupService;
    private final AuditedProductValidation validation;
    private final ProductWriter writer;
    private final CentralAudit centralAudit;

    // == Constructors ==
    @Autowired
    public ChangeProductQuantity(DomainLookupService domainLookupService, AuditedProductValidation validation, ProductWriter writer, CentralAudit centralAudit) {
        this.domainLookupService = domainLookupService;
        this.validation = validation;
        this.writer = writer;
        this.centralAudit = centralAudit;
    }

    // == Public Methods ==
    @Transactional
    public void increaseQuantity(long productId, int quantity){
        validation.checkIfQuantityIsValid(quantity);
        final Product product = domainLookupService.getProductOrThrow(productId, EndpointsNameMethods.PRODUCT_INCREASE_QTY);
        int newStock = Math.addExact(product.getProductAvailableStock(), quantity); // overflow-safe
        product.setProductAvailableStock(newStock);
        writer.save(product, EndpointsNameMethods.PRODUCT_INCREASE_QTY);
    }

    @Transactional
    public void decreaseQuantity(long productId, int quantity){
        final  Product product = domainLookupService.getProductOrThrow(productId, EndpointsNameMethods.PRODUCT_DECREASE_QTY);
        validation.checkAvailableQuantity(product.getProductAvailableStock(), quantity); // Fail early if not enough stock
        try {
            int newStock = Math.subtractExact(product.getProductAvailableStock(), quantity);
            product.setProductAvailableStock(newStock);
            writer.save(product, EndpointsNameMethods.PRODUCT_DECREASE_QTY);
        } catch (DataIntegrityViolationException ex){
            centralAudit.audit(ex, null, EndpointsNameMethods.PRODUCT_DECREASE_QTY, AuditingStatus.ERROR, ex.toString());
        }
    }
}
