package commerse.eshop.core.model.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "products")
public class Product {

    // == Constants ==
    // == Fields ==
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "product_id", nullable = false)
    private long product_id;

    @NotBlank
    @Column(name = "product_name", nullable = false, length = 200)
    private String name;

    @NotBlank
    @Column(name = "product_description", nullable = false, length = 255)
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "product_details", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> productDetails;

    @Min(0)
    @Column(name = "product_available_stock", nullable = false)
    private int productAvailableStock;

    @Min(0)
    @Column(name = "product_price", nullable = false)
    private BigDecimal price;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @OneToMany(mappedBy = "product")
    private Set<ProductCategory> productCategories = new HashSet<>();


    // == Constructors ==

    protected Product(){}

    // == Private Methods ==
    // == Public Methods ==

    // == ToString ==

    @Override
    public String toString() {
        return "Product{" +
                "product_id=" + product_id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", productDetails=" + productDetails +
                ", availStock=" + productAvailableStock +
                ", price=" + price +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", isActive=" + isActive +
                '}';
    }
}
