package commerse.eshop.core.model.entity;

import commerse.eshop.core.model.entity.enums.AuditingStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.OffsetDateTime;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "auditing")
public class Auditing {

    // == Fields ==

    @Id
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id", nullable = false)
    private long logId;

    @CreationTimestamp
    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "method_name", nullable = false)
    private String methodName;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)   // <-- key line
    private AuditingStatus status;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    // == Constructors ==

    protected Auditing(){};

    public Auditing(UUID customerId, String methodName, AuditingStatus status, String message){
        this.customerId = customerId;
        this.methodName = methodName;
        this.status = status;
        this.message = message;
    };
}
