package commerce.eshop.core.model.entity;

import commerce.eshop.core.util.enums.EmailKind;
import commerce.eshop.core.util.enums.EmailStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "emails_sent")
public class EmailsSent {

    // == Fields ==

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "email_id", nullable = false, updatable = false)
    private UUID emailId;

    @Column(name = "customer_id", updatable = false)
    private UUID customerId;

    @Column(name = "order_id", updatable = false)
    private UUID orderId;

    @Column(name = "payment_id", updatable = false)
    private UUID paymentId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "to_email", nullable = false)
    private String toEmail;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false)
    private EmailStatus status = EmailStatus.QUEUED;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type", nullable = false)
    private EmailKind type;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "email_text", nullable = false)
    private String emailText;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

    // == Constructors ==
    protected EmailsSent(){}

    public EmailsSent(UUID customerId, UUID orderOrPayment, String customerName, String toEmail, EmailKind type, String subject,
                      String emailText){

        // == Assign order id or payment id only for confirmations of the below actions ==
        if(type == EmailKind.ORDER_CONFIRMATION || type == EmailKind.ORDER_CANCEL_CONFIRMATION){
            this.orderId = orderOrPayment;
        } else if (type == EmailKind.PAYMENT_CONFIRMATION || type == EmailKind.PAYMENT_FAILED_CONFIRMATION){
            this.paymentId = orderOrPayment;
        }

        this.customerId = customerId;
        this.customerName = customerName;
        this.toEmail = toEmail;
        this.type = type;
        this.subject = subject;
        this.emailText = emailText;
    }
}
