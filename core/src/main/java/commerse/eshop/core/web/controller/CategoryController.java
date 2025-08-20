package commerse.eshop.core.web.controller;

import commerse.eshop.core.service.CategoryService;
import commerse.eshop.core.web.dto.requests.Category.DTOAddCategory;
import commerse.eshop.core.web.dto.requests.Category.DTOUpdateCategory;
import commerse.eshop.core.web.dto.response.Category.DTOCategoryResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService){
        this.categoryService = categoryService;
    }

    // Create Category
    ///url -X POST http://localhost:8080/api/v1/categories -H 'Content-Type: application/json' -d '{"categoryName":"Laptops","categoryDescription":"Portable computers"}'
    @PostMapping
    public ResponseEntity<DTOCategoryResponse> addNewCategory(@RequestBody @Valid DTOAddCategory dto){
        DTOCategoryResponse res = categoryService.addNewCategory(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    // Update Category
    ///curl -X PATCH http://localhost:8080/api/v1/categories/6 -H 'Content-Type: application/json' -d '{"categoryDescription":"Portable computers"}'
    @PatchMapping("/{categoryId}")
    public DTOCategoryResponse update(@PathVariable long categoryId, @RequestBody @Valid DTOUpdateCategory dto){
        return categoryService.updateCategory(dto, categoryId);
    }

    // Delete Category
    ///curl -X DELETE http://localhost:8080/api/v1/categories/6
    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long categoryId){
        categoryService.deleteCategory(categoryId);
    }

}
