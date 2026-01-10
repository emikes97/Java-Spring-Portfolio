package commerce.eshop.core.model.entity;

import commerce.eshop.core.application.util.enums.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
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

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {

    // == Fields ==
    @Id
    @Column(name="order_id", updatable=false, nullable=false)
    private UUID orderId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Customer customer;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "address_to_send", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> addressToSend;

    @DecimalMin("0.00")
    @Column(name = "total_outstanding", nullable = false, precision = 14, scale = 2)
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

    protected Order(){} /// For JPA only

    public Order(Customer customer, Map<String, Object> addressToSend, BigDecimal totalOutstanding)
    {
        this.customer = customer;
        this.addressToSend = addressToSend;
        this.totalOutstanding = totalOutstanding;
    }

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
