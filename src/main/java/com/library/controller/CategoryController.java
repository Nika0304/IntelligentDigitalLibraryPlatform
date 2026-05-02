package com.library.controller;

import com.library.model.Category;
import com.library.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController
{

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService)
    {
        this.categoryService = categoryService;
    }

    // get all categories
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories()
    {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    // get category by id
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id)
    {
        try
        {
            return ResponseEntity.ok(categoryService.getCategoryById(id));
        }
        catch (RuntimeException e)
        {
            return handleCategoryException(e);
        }
    }

    // create category
    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody Category category)
    {
        try
        {
            return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(category));
        }
        catch (RuntimeException e)
        {
            return handleCategoryException(e);
        }
    }

    // update category
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id,
                                            @RequestBody Category category)
    {
        try
        {
            return ResponseEntity.ok(categoryService.updateCategory(id, category));
        }
        catch (RuntimeException e)
        {
            return handleCategoryException(e);
        }
    }

    // delete category
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id)
    {
        try
        {
            categoryService.deleteCategory(id);
            return ResponseEntity.noContent().build();
        }
        catch (RuntimeException e)
        {
            return handleCategoryException(e);
        }
    }

    private ResponseEntity<String> handleCategoryException(RuntimeException e)
    {
        String message = e.getMessage();

        if (message != null && message.toLowerCase().contains("not found"))
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }

        if (message != null && (
                message.toLowerCase().contains("already exists") ||
                        message.toLowerCase().contains("duplicate")
        ))
        {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }
}