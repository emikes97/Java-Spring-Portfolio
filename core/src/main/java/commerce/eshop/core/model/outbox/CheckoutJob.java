package commerce.eshop.core.model.outbox;

import commerce.eshop.core.model.states.CheckoutStates;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
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

    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_id", updatable = false, nullable = false)
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
}