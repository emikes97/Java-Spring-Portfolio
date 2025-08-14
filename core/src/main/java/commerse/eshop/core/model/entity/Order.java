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

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {

    // == Constants ==

    // == Fields ==

    // == Auto Generated UUID for the customer
    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="order_id", updatable=false, nullable=false)
    private UUID orderId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "addr_to_send", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> addresToSend;

    @DecimalMin("0.01")
    @Column(name = "total_outstanding", nullable = false)
    private BigDecimal totalOutstanding;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // set when completed; null while pending
    // Will be updated once transaction has been finalized, should be done via Async as its not crucial for
    // the customer as the verification will be sent via sms/mail.
    // Order // Transaction is for validation - on our side in case of an issue.
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // == Constructors ==

    protected Order(){}

    // == Private Methods ==
    // == Public Methods ==

    // == ToString ==
    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", customer=" + customer +
                ", addresToSend=" + addresToSend +
                ", totalOutstanding=" + totalOutstanding +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
