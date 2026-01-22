package commerce.eshop.core.model.outbox;

import commerce.eshop.core.model.states.CheckoutStates;
import commerce.eshop.core.web.dto.requests.Order.DTOOrderCustomerAddress;
import commerce.eshop.core.web.dto.requests.Transactions.DTOTransactionRequest;
import commerce.eshop.core.web.dto.requests.Transactions.PaymentVariants.UseNewCard;
import commerce.eshop.core.web.dto.requests.Transactions.PaymentVariants.UseSavedMethod;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "checkout_job")
public class CheckoutJob {

    // == Fields ==

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    @Setter(AccessLevel.NONE)
    private long id;

    @Column(updatable = false, nullable = false)
    private UUID idemkey;

    @Column(name = "order_id", nullable = false, unique = true, insertable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private UUID orderId;

    @Column(name = "customer_id", updatable = false, nullable = false)
    private UUID customerId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "customer_address", updatable = false, columnDefinition = "jsonb")
    private Map<String, Object> customerAddress;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "transaction_method", updatable = false, columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> customerPayment;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "state", nullable = false)
    private CheckoutStates state = CheckoutStates.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // == Constructors ==

    protected CheckoutJob(){}

    public CheckoutJob(UUID idemkey, UUID customerId, DTOOrderCustomerAddress dtoAddress, DTOTransactionRequest dtoTransaction){
        this.idemkey = idemkey;
        this.customerId = customerId;
        this.customerAddress = mapAddress(dtoAddress);
        this.customerPayment = mapPayment(dtoTransaction);
    }

    // == Public Methods ==

    public DTOTransactionRequest toTransactionRequest(Map<String, Object> payment) {
        if (payment == null || payment.isEmpty()) {
            throw new IllegalStateException("checkout_job.transaction_method is missing");
        }

        String type = asString(payment.get("type"));

        return switch (type) {
            case "USE_SAVED_METHOD" -> {
                UUID pmId = asUUID(payment.get("customerPaymentMethodId"));
                yield new DTOTransactionRequest(new UseSavedMethod(pmId));
            }
            case "USE_NEW_CARD" -> {
                String brand = asString(payment.get("brand"));
                String token = asString(payment.get("token"));
                int expMonth = asInt(payment.get("expMonth"));
                int expYear = asInt(payment.get("expYear"));
                String holderName = asString(payment.get("holderName"));

                yield new DTOTransactionRequest(new UseNewCard(brand, token, expMonth, expYear, holderName));
            }
            default -> throw new IllegalStateException("Unsupported payment type: " + type);
        };
    }

    @Override
    public String toString() {
        return "CheckoutJob{" +
                "id=" + id +
                ", idemkey=" + idemkey +
                ", orderId=" + orderId +
                ", customerId=" + customerId +
                ", customerAddress=" + customerAddress +
                ", customerPayment=" + customerPayment +
                ", state=" + state +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    // == Private Methods ==

    private Map<String,Object> mapAddress(DTOOrderCustomerAddress dto){
        Map<String, Object> map = new HashMap<>();

        map.put("country", dto.country());
        map.put("street", dto.street());
        map.put("city", dto.city());
        map.put("postalCode", dto.postalCode());

        return map;
    }

    private Map<String, Object> mapPayment(DTOTransactionRequest dto){
        Map<String, Object> map = new HashMap<>();

        var i = dto.instruction();

        if (i instanceof UseSavedMethod saved){
            map.put("type", "USE_SAVED_METHOD");
            map.put("customerPaymentMethodId", saved.customerPaymentMethodId());
            return map;
        }

        if (i instanceof UseNewCard card){
            map.put("type", "USE_NEW_CARD");
            map.put("token", card.tokenRef());
            map.put("brand", card.brand());
            map.put("expMonth", card.expMonth());
            map.put("expYear", card.expYear());
            map.put("holderName", card.holderName());
            return map;
        }

        throw new IllegalArgumentException("Unsupported Method " + i.getClass().getSimpleName());
    }

    private String asString(Object v) {
        if (v == null) throw new IllegalStateException("Missing required field");
        return v.toString();
    }

    private int asInt(Object v) {
        if (v == null) throw new IllegalStateException("Missing required field");
        if (v instanceof Number n) return n.intValue();
        return Integer.parseInt(v.toString());
    }

    private UUID asUUID(Object v) {
        if (v == null) throw new IllegalStateException("Missing required field");
        if (v instanceof UUID u) return u;
        return UUID.fromString(v.toString());
    }
}