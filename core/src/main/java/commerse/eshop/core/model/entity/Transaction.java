package commerse.eshop.core.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
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
    @Column(name = "trans_id", nullable = false)
    private UUID transId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", columnDefinition = "jsonb", nullable = false)
    private Order order;

    @Column(name="customer_id", nullable = false)
    private String customerId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payment_method", nullable = false)
    private Map<String, Object> paymentMethod;

    @DecimalMin("0.01")
    @Column(name = "total_outstanding", nullable = false)
    private BigDecimal totalOutstanding;

    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private OffsetDateTime submittedAt;

    // Will be updated at the final check if the transaction has been successful.
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // == Constructors ==

    protected Transaction(){}

    // == Private Methods ==
    // == Public Methods ==
    // == ToString ==

    @Override
    public String toString() {
        return "Transaction{" +
                "transId=" + transId +
                ", order=" + order +
                ", customerId='" + customerId + '\'' +
                ", totalOutstanding=" + totalOutstanding +
                ", submittedAt=" + submittedAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
