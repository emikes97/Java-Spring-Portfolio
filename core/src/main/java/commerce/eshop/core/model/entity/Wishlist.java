package commerce.eshop.core.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "wishlist", uniqueConstraints = @UniqueConstraint(columnNames = "customer_id"))
public class Wishlist {

    // == Fields ==

    @Id
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "wishlist_id", nullable = false, updatable = false)
    private UUID wishlistId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    @JsonIgnore
    private Customer customer;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // == Constructors ==
    protected Wishlist(){}

    public Wishlist(Customer c){
        this.customer = c;
        attachTo(c);
    }

    // == Private Methods ==
    /// Attach Wishlist to Customer and Customer to Wishlist. Only 1 unique Wishlist per customer ///
    private void attachTo(Customer c){
        this.customer = c;
        c.setWishlist(this);
    }
}
