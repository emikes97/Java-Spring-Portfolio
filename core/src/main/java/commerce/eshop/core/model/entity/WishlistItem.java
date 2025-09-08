package commerce.eshop.core.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;
import java.util.Objects;

@Setter
@Getter
@Entity
@Table(
        name = "wishlist_item",
        uniqueConstraints = @UniqueConstraint(columnNames = {"wishlist_id", "product_id"})
)
public class WishlistItem {

    // == Fields ==

    @Id
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wish_id", nullable = false, updatable = false)
    private long wishId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wishlist_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Wishlist wishlist;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Product product;

    /// Product Name Snapshot
    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @CreationTimestamp
    @Column(name = "added_at", nullable = false, updatable = false)
    private OffsetDateTime addedAt;

    // == Constructors ==

    protected WishlistItem(){}

    public WishlistItem(Wishlist wishlist, Product product, String productName){
        this.wishlist = wishlist;
        this.product = product;
        this.productName = productName;
    }

    // == boolean & hashCode override ==

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WishlistItem)) return false;
        WishlistItem other = (WishlistItem) o;
        return wishlist.equals(other.wishlist) && product.equals(other.product);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wishlist, product);
    }

    // == ToString ==

    @Override
    public String toString() {
        return "WishlistItem{" +
                "product=" + product +
                ", productName='" + productName + '\'' +
                ", addedAt=" + addedAt +
                ", wishId=" + wishId +
                '}';
    }
}
