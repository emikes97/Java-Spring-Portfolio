package commerse.eshop.core.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "order_item")
public class OrderItem {

    // == Fields ==
    @Id
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id", nullable = false)
    private long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Product product;

    @Column(name = "product_name", length = 200, nullable = false)
    private String productName;

    @Column(nullable = false)
    private int quantity = 1;

    @Column(name = "price_at", nullable = false, precision = 14, scale = 2)
    private BigDecimal priceAt;

    @CreationTimestamp
    @Column(name = "snap_shot_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // == Constructors ==
    protected OrderItem(){}  /// For JPA only

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
