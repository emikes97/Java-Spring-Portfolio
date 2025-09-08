package commerce.eshop.core.repository;

import commerce.eshop.core.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepo extends JpaRepository<Product, Long> {

    // == Check if item exists by name
    boolean existsByProductNameIgnoreCase(String productName);

    // == Retrieve all products from a category
    @Query("""
        SELECT p FROM Product p
        JOIN p.productCategories pc
        WHERE pc.category.categoryId = :categoryId
    """)
    Page<Product> findAllByCategoryId(@Param("categoryId") long categoryId, Pageable pageable);
}
