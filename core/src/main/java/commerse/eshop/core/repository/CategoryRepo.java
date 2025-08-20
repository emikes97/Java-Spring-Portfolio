package commerse.eshop.core.repository;

import commerse.eshop.core.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepo extends JpaRepository<Category, Long> {

    // List<Category> findAll();  // inherited from JpaRepository

    // == Find Category by Name
    Optional<Category> findByCategoryName(String categoryName);
    Optional<Category> findByCategoryNameIgnoreCase(String categoryName);
    boolean existsByCategoryNameIgnoreCase(String categoryName); // Ignore case

    // == Find all Categories
    @Query(value = "select category_name from categories", nativeQuery = true)
    List<String> findAllCategoryNames();

    // == Find all ascending
    List<Category> findAllByOrderByCategoryNameAsc();

    // == Delete by ID
    @Modifying
    @Query(value = "delete from categories where category_id = :catId", nativeQuery = true)
    int deleteCategory(@Param("catId") long catId);
}
