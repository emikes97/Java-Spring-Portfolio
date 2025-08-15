package commerse.eshop.core.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "cart")
public class Cart {

    // == Constants ==
    // == Fields ==
    // == Auto Generated UUID for the cart_id
    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="cart_id", updatable=false, nullable=false)
    private UUID cartId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    private Customer customer;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /// Link Cart_Item to Cart -- Reverse ///
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CartItem> cartItems = new HashSet<>();

    // == Constructors ==

    protected Cart(){}

    // == Private Methods ==

    // == Public Methods ==
    /// Attach Cart to Customer and Customer to Cart. Only 1 unique Cart per customer ///
    public void attachTo(Customer c){
        this.customer = c;
        c.setCart(this);
    }

    // == ToString ==

    @Override
    public String toString() {
        return "Cart{" +
                "cartId=" + cartId +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
