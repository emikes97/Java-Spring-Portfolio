package commerce.eshop.core.application.category.validation;

import commerce.eshop.core.util.CentralAudit;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.util.enums.AuditingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

@Component
public class AuditedCategoryValidation {
    // == Fields ==
    private final CentralAudit centralAudit;

    // == Constructors ==
    @Autowired
    public AuditedCategoryValidation(CentralAudit centralAudit) {
        this.centralAudit = centralAudit;
    }

    // == Public Methods ==
    public void checkIfCategoryExists(boolean duplicate){
        if (duplicate){
            throw centralAudit.audit(new DuplicateKeyException("Category already exists"), null,
                    EndpointsNameMethods.CATEGORY_CREATE, AuditingStatus.ERROR,"Category already exists");
        }
    }
}
