package com.library.controller;

import com.library.model.Category;
import com.library.service.CategoryService;
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

    //get all categories
    @GetMapping
    public List<Category> getAllCategories()
    {
        return categoryService.getAllCategories();
    }

    //get category by id
    @GetMapping("/{id}")
    public Category getCategoryById(@PathVariable Long id)
    {
        return categoryService.getCategoryById(id);
    }

    //create category
    @PostMapping
    public Category createCategory(@RequestBody Category category)
    {
        return categoryService.createCategory(category);
    }

    //update category
    @PutMapping("/{id}")
    public Category updateCategory(@PathVariable Long id,
                                   @RequestBody Category category) {
        return categoryService.updateCategory(id, category);
    }

    //delete category
    @DeleteMapping("/{id}")
    public void deleteCategory(@PathVariable Long id)
    {
        categoryService.deleteCategory(id);
    }

}
