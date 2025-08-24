package commerse.eshop.core.model.entity;

import commerse.eshop.core.model.entity.enums.TransactionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "transactions")
public class Transaction {

    // == Constants ==
    // == Fields ==

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.NONE)
    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Order order;

    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$")
    @Column(name="customer_id_snapshot", nullable = false, length = 36)
    private String customerId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payment_method", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> paymentMethod;

    @DecimalMin(value = "0.00", inclusive = false)
    @Column(name = "total_outstanding", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalOutstanding;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, length = 16)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "provider_reference", length = 100)
    private String providerReference;

    @Column(name = "idempotency_key", unique = true, length = 100)
    private String idempotencyKey;

    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private OffsetDateTime submittedAt;

    // Will be updated at the final     check if the transaction has been successful.
    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    // == Constructors ==

    protected Transaction(){}

    // == Private Methods ==
    // == Public Methods ==
    // == ToString ==

    @Override
    public String toString() {
        return "Transaction{" +
                "transId=" + transactionId +
                ", order=" + order +
                ", customerId='" + customerId + '\'' +
                ", totalOutstanding=" + totalOutstanding +
                ", submittedAt=" + submittedAt +
                ", updatedAt=" + completedAt +
                '}';
    }
}
