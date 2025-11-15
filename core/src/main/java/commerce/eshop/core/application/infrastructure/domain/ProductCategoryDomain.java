package commerce.eshop.core.application.infrastructure.domain;

import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.model.entity.Category;
import commerce.eshop.core.model.entity.Product;
import commerce.eshop.core.repository.CategoryRepo;
import commerce.eshop.core.repository.ProductRepo;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;
import java.util.UUID;

@Component
public class ProductCategoryDomain {

    // == Fields ==
    private final ProductRepo pRepo;
    private final CategoryRepo cRepo;
    private final CentralAudit centralAudit;

    // == Constructors ==
    @Autowired
    public ProductCategoryDomain(ProductRepo pRepo, CategoryRepo cRepo, CentralAudit centralAudit) {
        this.pRepo = pRepo;
        this.cRepo = cRepo;
        this.centralAudit = centralAudit;
    }

    // == Public Method ==

    public boolean checkIfProductExistsByProductName(String normalisedName){
        return pRepo.existsByProductNameIgnoreCase(normalisedName);
    }

    public Product retrieveProduct(long productId, String method){
        try {
            return pRepo.findById(productId).orElseThrow(() -> new NoSuchElementException("Product with the provided ID doesn't exist"));
        } catch (NoSuchElementException e){
            throw centralAudit.audit(e, null, method, AuditingStatus.WARNING, e.toString());
        }
    }

    public Product retrieveProduct(UUID customerId, long productId, String method){
        try {
            final Product product = pRepo.findById(productId).orElseThrow(
                    () -> new NoSuchElementException("The provided ID doesn't match with any available product.")
            );
            return product;
        } catch (NoSuchElementException e){
            throw centralAudit.audit(e,customerId, method, AuditingStatus.WARNING, e.toString());
        }
    }

    public Category retrieveCategory(long categoryId, String method){
        try{
            return cRepo.findById(categoryId).orElseThrow(
                    () -> new NoSuchElementException("The requested category doesn't exist"));
        } catch (NoSuchElementException e){
            throw centralAudit.audit(e, null, method, AuditingStatus.ERROR);
        }
    }

    public boolean checkIfCatExists(String catName){
        return cRepo.existsByCategoryNameIgnoreCase(catName);
    }

    // == Paged Queries ==

    public Page<Product> retrievePagedProductsByCategory(long categoryId, Pageable pageable){
        return pRepo.findAllByCategoryId(categoryId, pageable);
    }
}
