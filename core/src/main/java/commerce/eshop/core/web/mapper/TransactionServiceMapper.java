package commerce.eshop.core.web.mapper;

import commerce.eshop.core.model.entity.Transaction;
import commerce.eshop.core.web.dto.requests.Transactions.PaymentVariants.PaymentInstruction;
import commerce.eshop.core.web.dto.requests.Transactions.PaymentVariants.UseNewCard;
import commerce.eshop.core.web.dto.requests.Transactions.PaymentVariants.UseSavedMethod;
import commerce.eshop.core.web.dto.response.Transactions.DTOTransactionResponse;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class TransactionServiceMapper {

    public Map<String, Object> toSnapShot(PaymentInstruction paymentInstruction){
        Map<String, Object> m = new LinkedHashMap<>();

        if (paymentInstruction instanceof UseNewCard card){
            m.put("token", card.tokenRef());     // ✅ token reference instead of panMasked/cvc <-- more changes will happen in 'terms of paying'
            m.put("brand", card.brand());
            m.put("expMonth", card.expMonth());
            m.put("expYear", card.expYear());
            m.put("holderName", card.holderName());
            return m;
        }
        if (paymentInstruction instanceof UseSavedMethod saved) {
            m.put("type", "USE_SAVED_METHOD");
            m.put("customerPaymentMethodId", saved.customerPaymentMethodId());
            return m;
        }

        m.put("type", "UNKNOWN");
        return m;
    }

    public DTOTransactionResponse toDto(Transaction tr){
        return new DTOTransactionResponse(
                tr.getTransactionId(),
                tr.getOrder().getOrderId(),
                tr.getCustomerId(),
                tr.getPaymentMethod(),
                tr.getTotalOutstanding(),
                tr.getStatus(),
                tr.getSubmittedAt(),
                tr.getCompletedAt()
        );
    }
}
