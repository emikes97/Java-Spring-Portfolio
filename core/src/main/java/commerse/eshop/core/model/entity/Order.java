package commerse.eshop.core.model.entity;

import commerse.eshop.core.model.entity.enums.OrderStatus;
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
    @Column(name = "address_to_send", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> addressToSend;

    @DecimalMin("0.01")
    @Column(name = "total_outstanding", nullable = false)
    private BigDecimal totalOutstanding;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "order_status", nullable = false, columnDefinition = "order_status", length = 32)
    private OrderStatus orderStatus = OrderStatus.PENDING_PAYMENT;

    @CreationTimestamp
    @Column(name = "order_created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // set when completed; null while pending
    @Column(name = "order_completed_at")
    private OffsetDateTime completedAt;

    // == Constructors ==

    protected Order(){}

    public Order(Customer customer, Map<String, Object> addressToSend, BigDecimal totalOutstanding, OffsetDateTime createdAt)
    {
        this.customer = customer;
        this.addressToSend = addressToSend;
        this.totalOutstanding = totalOutstanding;
        this.createdAt = createdAt;
    }

    // == Private Methods ==
    // == Public Methods ==

    // == ToString ==
    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", customer=" + customer +
                ", addressToSend=" + addressToSend +
                ", totalOutstanding=" + totalOutstanding +
                ", createdAt=" + createdAt +
                ", updatedAt=" + completedAt +
                '}';
    }
}
