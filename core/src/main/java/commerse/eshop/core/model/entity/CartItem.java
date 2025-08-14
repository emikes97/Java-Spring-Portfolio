package commerse.eshop.core.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;

@Setter
@Getter
@Entity
@Table(name = "cart_item")
public class CartItem {

    // == Constants ==
    // == Fields ==

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "cart_item_id", nullable = false)
    private long cartItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="cart_id", nullable = false)
    private Cart cart;

    //private long Product product;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Min(1)
    private int quantity = 1;

    @Min(0)
    @Column(name = "price_at", nullable = false)
    private BigDecimal priceAt;

    @CreationTimestamp
    @Column(name = "added_at", nullable = false, updatable = false)
    private java.time.OffsetDateTime addedAt;

    // == Constructors ==

    protected CartItem(){}

    // == Private Methods ==
    // == Public Methods ==

    public void attachTo(Cart c){
        this.cart = c;
        c.getCartItems().add(this);
    }

    // == ToString ==
}
