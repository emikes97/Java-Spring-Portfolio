package commerce.eshop.core.application.async.internal.payment.confirmPayment.processes.handlePayment;

import commerce.eshop.core.application.async.external.contracts.PaymentProviderClient;
import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import commerce.eshop.core.model.entity.Transaction;
import commerce.eshop.core.repository.TransactionRepo;
import commerce.eshop.core.web.dto.response.Providers.Charging.ProviderChargeResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class CheckPaymentStatusFromClient {

    // == Fields ==
    private final PaymentProviderClient client;
    private final CentralAudit centralAudit;
    private final TransactionRepo repo;

    // == Constructors ==
    @Autowired
    public CheckPaymentStatusFromClient(PaymentProviderClient client, CentralAudit centralAudit, TransactionRepo repo) {
        this.client = client;
        this.centralAudit = centralAudit;
        this.repo = repo;
    }

    // == Public Methods ==

    public ProviderChargeResult handle(Map<String, Object> snap, String methodType, Transaction tr, UUID customerId){

        ProviderChargeResult providerChargeResult = null;

        try{
            switch (methodType){
                case "USE_SAVED_METHOD" -> {
                    Object idObjPaymentMethod = snap.get("customerPaymentMethodId");
                    UUID customerPaymentMethodId = (idObjPaymentMethod instanceof UUID u) ? u : UUID.fromString(String.valueOf(idObjPaymentMethod));
                    return client.chargeWithSavedMethod(tr, customerPaymentMethodId);
                }
                case "USE_NEW_CARD" -> {
                    String brand = (String) snap.get("brand");
                    String tokenRef = (String) snap.get("tokenRef");
                    Integer expMonth = toInt(snap.get("expMonth"));
                    Integer expYear  = toInt(snap.get("expYear"));
                    String holder    = (String) snap.get("holderName");
                    return client.tokenizeAndCharge(tr, brand, tokenRef, expMonth, expYear, holder);
                }
                default -> {
                    throw new IllegalArgumentException("Unsupported payment method");
                }
            }
        } catch (Exception ex) {
            centralAudit.info(customerId, EndpointsNameMethods.PAYMENT_PROCESSING_ASYNC, AuditingStatus.ERROR,
                    ex.toString());
            return new ProviderChargeResult(null, false, "PROVIDER_ERROR", ex.getMessage());
        }
    }

    // == Private Methods ==

    private Integer toInt(Object o){
        if (o == null) return null;
        if (o instanceof Integer i) return i;
        if (o instanceof Number n) return n.intValue();
        return Integer.parseInt(o.toString());
    }
}
