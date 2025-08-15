package commerse.eshop.core.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "categories")
public class Category {

    // == Constants ==
    // == Fields ==

    @Id
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id", nullable = false)
    private long categoryId;

    @Column(name = "category_name", nullable = false)
    private String categoryName;

    @Column(name = "category_description", nullable = false)
    private String categoryDescription;

    @OneToMany(mappedBy = "category")
    private Set<ProductCategory> productCategories = new HashSet<>();

    // == Constructors ==

    protected Category(){}

    // == Private Methods ==
    // == Public Methods ==
    // == ToString ==
}
