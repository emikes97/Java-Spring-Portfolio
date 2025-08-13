package commerse.eshop.core.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Table(name = "customers")
@Entity
public class Customer {

    // == Fields ==

    // == Auto Generated UUID for the customer
    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="customer_id", updatable=false, nullable=false)
    private java.util.UUID customer_id;
    // ------------------------------------------------------------

    @Column(length = 20)
    @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$")
    @Size(max = 20)
    private String phone_number;

    @Email
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    @lombok.ToString.Exclude
    private String password_hash;

    private String name;
    private String surname;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private java.time.OffsetDateTime created_at;

    private boolean subscribed;

    // == Constructor ==
    protected Customer(){}

    // == Normalization ==
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
                ", phone_number='" + phone_number + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", created_at=" + created_at +
                ", subscribed=" + subscribed +
                ", username='" + username + '\'' +
                '}';
    }
}
