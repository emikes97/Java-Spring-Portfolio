package commerse.eshop.core.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "order_item")
public class OrderItem {

    // == Constants ==
    // == Fields ==

    @Id
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id", nullable = false)
    private long orderItemId;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "product_name", length = 200)
    private String productName;

    private int quantity = 1;

    @Column(name = "price_at", nullable = false)
    private BigDecimal priceAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // == Constructors ==

    protected OrderItem(){}

    // == Private Methods ==
    // == Public Methods ==
    // == ToString ==

    @Override
    public String toString() {
        return "OrderItem{" +
                "orderItemId=" + orderItemId +
                ", product=" + product +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", priceAt=" + priceAt +
                ", createdAt=" + createdAt +
                ", order=" + order +
                '}';
    }
}
