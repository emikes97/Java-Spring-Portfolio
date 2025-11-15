package commerce.eshop.core.application.product.writer;

import commerce.eshop.core.model.entity.Product;
import commerce.eshop.core.repository.ProductCategoryRepo;
import commerce.eshop.core.repository.ProductRepo;
import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class ProductWriter {
    // == Fields ==
    private final CentralAudit centralAudit;
    private final ProductRepo repo;
    private final ProductCategoryRepo linker;

    // == Constructors ==
    @Autowired
    public ProductWriter(CentralAudit centralAudit, ProductRepo repo, ProductCategoryRepo linker) {
        this.centralAudit = centralAudit;
        this.repo = repo;
        this.linker = linker;
    }

    // == Public Methods ==
    public Product save(Product product, String endpoint){
        try {
            product = repo.saveAndFlush(product);
            return product;
        } catch (DataIntegrityViolationException | ArithmeticException ex){
            throw centralAudit.audit(ex,null, endpoint, AuditingStatus.ERROR, ex.toString());
        }
    }

    public void delete(Product product){
        try {
            repo.delete(product);
            repo.flush();              // fail fast on FK constraints
        } catch (DataIntegrityViolationException dup){
            throw centralAudit.audit(dup, null, EndpointsNameMethods.PRODUCT_REMOVE, AuditingStatus.ERROR, dup.toString());
        }
    }

    public int link(long productId, long categoryId){
        return linker.linkIfAbsent(productId, categoryId);
    }

    public int unlink(long productId, long categoryId){
        return linker.deleteByProduct_ProductIdAndCategory_CategoryId(productId, categoryId);
    }

    public boolean exists(String name){
        return repo.existsByProductNameIgnoreCase(name);
    }
}
