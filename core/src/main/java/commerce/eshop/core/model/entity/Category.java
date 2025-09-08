package commerce.eshop.core.model.entity;

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

    protected Category(){} /// For JPA only

    public Category(String categoryName, String categoryDescription){
        this.categoryName = categoryName;
        this.categoryDescription = categoryDescription;
    }

    // == Private Methods ==
    /// Normalize the text for categoryName and categoryDescription to ensure no duplicates and no funny bugs,
    /// with invisible characters.
    @PrePersist @PreUpdate
    private void normalize() {
        if (categoryName != null) categoryName = categoryName.trim();
        if (categoryDescription != null) categoryDescription = categoryDescription.trim();
    }

    // == ToString ==
    @Override
    public String toString() {
        return "Category{" +
                "categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                ", categoryDescription='" + categoryDescription + '\'' +
                ", productCategories=" + productCategories +
                '}';
    }
}
