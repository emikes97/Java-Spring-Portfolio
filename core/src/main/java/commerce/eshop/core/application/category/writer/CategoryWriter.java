package commerce.eshop.core.application.category.writer;

import commerce.eshop.core.model.entity.Category;
import commerce.eshop.core.repository.CategoryRepo;
import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Component
public class CategoryWriter {

    // == Fields ==
    private final CategoryRepo repo;
    private final CentralAudit centralAudit;

    // == Constructors ==
    @Autowired
    public CategoryWriter(CategoryRepo repo, CentralAudit centralAudit) {
        this.repo = repo;
        this.centralAudit = centralAudit;
    }

    // == Public Methods ==

    public Category save(Category cat, String endpoint){
        try {
            cat = repo.saveAndFlush(cat);
            return cat;
        } catch (DataIntegrityViolationException e){
            throw centralAudit.audit(e,null, endpoint, AuditingStatus.ERROR,e.toString());
        }
    }

    public void delete(long catId, String endpoint){
        try {
            repo.deleteCategory(catId);
            repo.flush(); // forces constraint check now so catch can create from it
        } catch (NoSuchElementException | DataIntegrityViolationException ex){
            throw centralAudit.audit(ex, null, endpoint, AuditingStatus.ERROR, ex.toString());
        }
    }
}
