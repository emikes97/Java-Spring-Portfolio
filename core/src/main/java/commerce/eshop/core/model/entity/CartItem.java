package commerce.eshop.core.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Setter
@Getter
@Entity
@Table(
        name = "cart_item",
        uniqueConstraints = @UniqueConstraint(columnNames = {"cart_id", "product_id"})
)
public class CartItem {

    // == Fields ==

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "cart_item_id", nullable = false)
    private long cartItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="cart_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Product product;

    @NotBlank
    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Min(1)
    private int quantity = 1;

    @DecimalMin(value = "0.00")
    @Column(name = "price_at", nullable = false, precision = 14, scale = 2)
    private BigDecimal priceAt;

    @CreationTimestamp
    @Column(name = "added_at", nullable = false, updatable = false)
    private OffsetDateTime addedAt;

    // == Constructors ==

    protected CartItem(){} /// For JPA only

    public CartItem(Cart cart, Product product, String productName, int quantity, BigDecimal priceAt){
        this.cart = cart;
        this.product = product;
        this.productName = productName;
        this.quantity = quantity;
        this.priceAt = priceAt;
    }

    // == ToString ==

    @Override
    public String toString() {
        return "CartItem{" +
                "cartItemId=" + cartItemId +
                ", product=" + product +
                ", quantity=" + quantity +
                ", productName='" + productName + '\'' +
                ", priceAt=" + priceAt +
                ", addedAt=" + addedAt +
                '}';
    }
}
