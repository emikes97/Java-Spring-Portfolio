package commerse.eshop.core.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Table(name = "customers")
@Entity
public class Customer {

    // == Fields ==
    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="customer_id", updatable=false, nullable=false)
    private UUID customerId;

    @Column(name = "phone_number", nullable = false, unique = true)
    @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$")
    @Size(max = 20)
    private String phoneNumber;

    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(length = 100)
    private String name;

    @Column(length = 100)
    private String surname;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "is_subscribed", nullable = false)
    private boolean isSubscribed;

    /// Link PaymentMethods to Customer -- Reverse ///
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CustomerPaymentMethod> paymentMethods = new HashSet<>();

    /// Link Cart to Customer -- Reverse ///
    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private Cart cart;

    // == Constructor ==
    protected Customer(){} /// For JPA only

    public Customer(String phoneNumber, String email, String username, String passwordHash, String name, String surname){
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
        this.name = name != null ? name : "";
        this.surname = surname != null ? surname : "";
        this.isSubscribed = false;
    }

    // == Normalization ==
    /// Ensure no invisible characters and no duplicate usernames/mails.
    @PrePersist
    @PreUpdate
    private void normalize(){
        if (email != null) email = email.trim().toLowerCase();
        if (username != null) username = username.trim().toLowerCase();
    }

    // == ToString ==
    @Override
    public String toString() {
        return "Customer{" +
                "email='" + email + '\'' +
                ", phone_number='" + phoneNumber + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", createdAt=" + createdAt +
                ", subscribed=" + isSubscribed +
                ", username='" + username + '\'' +
                '}';
    }
}
