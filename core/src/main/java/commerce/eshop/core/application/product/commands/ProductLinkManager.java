package commerce.eshop.core.application.product.commands;

import commerce.eshop.core.application.product.writer.ProductWriter;
import commerce.eshop.core.service.DomainLookupService;
import commerce.eshop.core.util.CentralAudit;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.util.enums.AuditingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Component
public class ProductLinkManager {

    // == Fields ==
    private final ProductWriter writer;
    private final CentralAudit centralAudit;

    // == Constructors ==
    @Autowired
    public ProductLinkManager(ProductWriter writer, CentralAudit centralAudit) {
        this.writer = writer;
        this.centralAudit = centralAudit;
    }

    // == Public Methods ==
    @Transactional
    public int link(long productId, long categoryId){
        try {
            return writer.link(productId, categoryId);
        } catch (DataIntegrityViolationException dup){
            throw centralAudit.audit(dup,null, EndpointsNameMethods.PRODUCT_LINK, AuditingStatus.ERROR, dup.toString());
        }
    }

    @Transactional
    public int unlink(long productId, long categoryId){
        try {
            return writer.unlink(productId, categoryId);
        } catch (DataIntegrityViolationException | NoSuchElementException ex){
            throw centralAudit.audit(ex,null, EndpointsNameMethods.PRODUCT_UNLINK, AuditingStatus.ERROR, ex.toString());
        }
    }
}