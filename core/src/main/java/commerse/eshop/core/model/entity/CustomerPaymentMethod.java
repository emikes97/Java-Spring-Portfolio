package commerse.eshop.core.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;


@Setter
@Getter
@Entity
@Table(name = "customer_payment_methods")
public class CustomerPaymentMethod {

    // == Constants ==
    private static final int MAX_FUTURE_YEARS = 30;

    // == Fields ==

    // == Auto Generated UUID for the payment Method
    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="customer_payment_id", updatable=false, nullable=false)
    private UUID customerPaymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String provider;

    @Column(name = "provider_payment_token", nullable = false, columnDefinition = "text")
    private String providerPaymentMethodToken;

    @NotBlank
    @Column(nullable = false, length = 25)
    private String brand;

    @Pattern(regexp = "\\d{4}")
    @Column(name = "last_4", length = 4, nullable = false) // char(4)
    private String last4;

    @Min(2000)
    @Column(name = "year_exp", nullable = false)
    private short yearExp;

    @Min(1)
    @Max(12)
    @Column(name = "month_exp", nullable = false)
    private short monthExp;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // == Constructors ==

    protected CustomerPaymentMethod(){}

    public CustomerPaymentMethod(Customer customer, String provider, String brand, String last4, short yearExp, short monthExp, boolean isDefault){
        this.customer = customer;
        this.provider = provider;
        this.brand = brand;
        this.last4 = last4;
        this.yearExp = yearExp;
        this.monthExp = monthExp;
        this.isDefault = isDefault;
    }

    // == Private Methods ==

    @PrePersist
    @PreUpdate
    private void validateExpiry(){
        LocalDate today = LocalDate.now();
        short currentYear = (short) today.getYear();

        if (yearExp < currentYear){
            throw new IllegalArgumentException("Year expiration cannot be in the past.");
        }
        if (yearExp == currentYear && monthExp < today.getMonthValue()){
            throw new IllegalArgumentException("Month expiration cannot be in the past.");
        }
        if (yearExp > currentYear + MAX_FUTURE_YEARS){
            throw new IllegalArgumentException("Year expiration is unrealistically far in the future");
        }
    }

    // == ToString ==

    @Override
    public String toString() {
        return "CustomerPaymentMethod{" +
                "provider='" + provider + '\'' +
                ", brand='" + brand + '\'' +
                ", yearExp=" + yearExp +
                ", monthExp=" + monthExp +
                ", isDefault=" + isDefault +
                ", createdAt=" + createdAt +
                '}';
    }
}
