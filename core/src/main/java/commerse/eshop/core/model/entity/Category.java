package commerse.eshop.core.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "categories",
        uniqueConstraints = @UniqueConstraint(columnNames = "category_name"))
public class Category {

    // == Constants ==
    // == Fields ==

    @Id
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id", nullable = false)
    private long categoryId;

    @NotBlank @Size(max = 50)
    @Column(name = "category_name", nullable = false, columnDefinition = "citext")
    private String categoryName;

    @NotBlank
    @Column(name = "category_description", nullable = false, columnDefinition = "text")
    private String categoryDescription;

    @OneToMany(mappedBy = "category")
    @JsonIgnore
    private Set<ProductCategory> productCategories = new HashSet<>();

    // == Constructors ==

    protected Category(){}

    public Category(String categoryName, String categoryDescription){
        this.categoryName = categoryName;
        this.categoryDescription = categoryDescription;
    }

    // == Private Methods ==

    // == Public Methods ==
    @PrePersist @PreUpdate
    private void normalize() {
        if (categoryName != null) categoryName = categoryName.trim();
        if (categoryDescription != null) categoryDescription = categoryDescription.trim();
    }

    // == ToString ==
}
