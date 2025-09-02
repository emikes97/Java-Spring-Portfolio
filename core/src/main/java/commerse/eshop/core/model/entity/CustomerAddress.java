package commerse.eshop.core.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Setter
@Getter
@Table(name = "customers_address")
public class CustomerAddress {

    // == Fields ==
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "addr_id", nullable = false)
    private long addrId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", updatable = false, nullable = false)
    @JsonIgnore
    private Customer customer;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String country;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String street;

    @NotBlank
    @Column(nullable = false, length = 75)
    private String city;

    @NotBlank
    @Column(name = "postal_code", nullable = false, length = 50)
    private String postalCode;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;


    // == Constructors ==
    protected CustomerAddress(){} /// For JPA only

    public CustomerAddress(Customer customer, String country, String street,
                           String city, String postalCode, boolean isDefault) {
        this.customer = customer;
        this.country = country;
        this.street = street;
        this.city = city;
        this.postalCode = postalCode;
        this.isDefault = isDefault;
    }

    // == Private Methods ==
    /// Normalize the text to ensure no invisible characters exist.
    @PrePersist @PreUpdate
    private void normalize() {
        if (country != null) country = country.trim();
        if (street  != null) street  = street.trim();
        if (city    != null) city    = city.trim();
        if (postalCode != null) postalCode = postalCode.trim();
    }

    // == ToString ==
    @Override
    public String toString() {
        return "CustomerAddress{" +
                "addrId=" + addrId +
                ", country='" + country + '\'' +
                ", street='" + street + '\'' +
                ", city='" + city + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", isDefault=" + isDefault +
                '}';
    }
}
