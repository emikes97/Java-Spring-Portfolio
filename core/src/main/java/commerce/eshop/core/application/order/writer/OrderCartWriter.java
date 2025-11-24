package commerce.eshop.core.application.order.writer;

import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import commerce.eshop.core.repository.CartItemRepo;
import commerce.eshop.core.repository.DbLockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class OrderCartWriter {

    // == Fields ==
    private final DbLockRepository dbLockRepo;
    private final CartItemRepo ciRepo;
    private final CentralAudit centralAudit;

    // == Constructors ==
    @Autowired
    public OrderCartWriter(DbLockRepository dbLockRepo, CartItemRepo ciRepo, CentralAudit centralAudit) {
        this.dbLockRepo = dbLockRepo;
        this.ciRepo = ciRepo;
        this.centralAudit = centralAudit;
    }

    // == Public Methods ==

    public void reserveStock(UUID cartId, UUID customerId){
        int updated = ciRepo.reserveStockForCart(cartId);
        long expected = ciRepo.countDistinctCartProducts(cartId);
        if(updated != expected){
            /// Client error, The product was out-of-stock when order was placed.
            ResponseStatusException err = new ResponseStatusException(HttpStatus.CONFLICT, "INSUFFICIENT_STOCK");
            throw centralAudit.audit(err, customerId, EndpointsNameMethods.ORDER_PLACE, AuditingStatus.WARNING, "INSUFFICIENT_STOCK");
        }
    }

    public BigDecimal sumCartOutstanding(UUID cartId){
        return ciRepo.sumCartTotalOutstanding(cartId);
    }

    public boolean lockCart(UUID cartId){
        return dbLockRepo.tryLockCart(cartId);
    }
}
