package commerse.eshop.core.repository;

import commerse.eshop.core.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepo extends JpaRepository<Product, Long> {

    // == Find Product By name ==
    Optional<Product> findByProductName(String productName);

    // == Check if item exists by name
    boolean existsByProductNameIgnoreCase(String productName);

    // == Retrieve all products from a category
    @Query("""
        SELECT p FROM Product p
        JOIN p.productCategories pc
        WHERE pc.category.categoryId = :categoryId
    """)
    Page<Product> findAllByCategoryId(@Param("categoryId") long categoryId, Pageable pageable);

    // == Find Product stock by ID ==
    @Query(value = "select product_available_stock from products where product_id = :productId", nativeQuery = true)
    Optional<Integer> findAvailableStock(@Param("productId") long id);

    // == Find Product stock by Name ==
    @Query(value = "select product_available_stock from products where product_name = :productName", nativeQuery = true)
    Optional<Integer> findAvailableStock(@Param("productName") String productName);
}
