package commerce.eshop.core.util;

import commerce.eshop.core.service.AuditingService;
import commerce.eshop.core.util.enums.AuditingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CentralAudit {

    // == Fields ==
    private final AuditingService auditingService;

    // == Constructors ==
    @Autowired
    public CentralAudit(AuditingService auditingService){
        this.auditingService = auditingService;
    }

    // == Public Methods ==

    public void info(UUID customerId, String method, AuditingStatus status, String message){
        auditingService.log(customerId, method, AuditingStatus.SUCCESSFUL, message);
    }

    public void warn(UUID customerId, String method, AuditingStatus status, String message){
        auditingService.log(customerId, method, AuditingStatus.WARNING, message);
    }

    public  <T extends  RuntimeException> T audit(T ex, UUID customerId, String method, AuditingStatus status, String message){
        auditingService.log(customerId, method, status, message);
        return ex;
    }

    public  <T extends RuntimeException> T audit(T ex, UUID customerId, String method, AuditingStatus status) {
        return audit(ex, customerId, method, status, ex.toString());
    }
}
