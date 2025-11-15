package commerce.eshop.core.application.product.validation;

import commerce.eshop.core.application.product.writer.ProductWriter;
import commerce.eshop.core.util.CentralAudit;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.util.enums.AuditingStatus;
import commerce.eshop.core.web.dto.requests.Products.DTOAddProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuditedProductValidation {
    // == Fields ==
    private final ProductWriter writer;
    private final CentralAudit centralAudit;

    // == Constructors ==
    @Autowired
    public AuditedProductValidation(ProductWriter writer, CentralAudit centralAudit) {
        this.writer = writer;
        this.centralAudit = centralAudit;
    }

    // == Public Methods ==
    public void checkData(DTOAddProduct dto){
        if(dto == null){
            IllegalArgumentException illegal = new IllegalArgumentException("Product Details can't be empty");
            throw centralAudit.audit(illegal, null, EndpointsNameMethods.PRODUCT_ADD, AuditingStatus.WARNING, illegal.toString());
        }
    }

    public void checkIfProductExists(String normalisedName){
        if(writer.exists(normalisedName)){
            IllegalStateException illegal = new IllegalStateException("Product already exists");
            throw centralAudit.audit(illegal, null, EndpointsNameMethods.PRODUCT_ADD, AuditingStatus.WARNING, illegal.toString());
        }
    }

    public void checkIfQuantityIsValid(int quantity){
        if (quantity <= 0){
            IllegalArgumentException illegal = new IllegalArgumentException("Quantity can't be negative/or zero for increasing the stock");
            throw centralAudit.audit(illegal, null, EndpointsNameMethods.PRODUCT_INCREASE_QTY, AuditingStatus.WARNING, illegal.toString());
        }
    }

    public void checkAvailableQuantity(int availableStock, int quantity){
        if (availableStock < quantity){
            IllegalStateException illegal = new IllegalStateException("Insufficient stock: available="
                    + availableStock + ", requested=" + quantity);
            throw centralAudit.audit(illegal, null, EndpointsNameMethods.PRODUCT_DECREASE_QTY, AuditingStatus.WARNING, illegal.toString());
        }
    }
}
