package com.library.service;

import com.library.model.Category;
import com.library.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService
{

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository)
    {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getAllCategories()
    {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id)
    {
        validateId(id);

        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

    public Category createCategory(Category category)
    {
        validateCategory(category);

        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, Category updatedCategory)
    {
        validateId(id);
        validateCategory(updatedCategory);

        Category existingCategory = getCategoryById(id);

        existingCategory.setName(updatedCategory.getName());

        return categoryRepository.save(existingCategory);
    }

    public void deleteCategory(Long id)
    {
        validateId(id);

        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found with id: " + id);
        }

        categoryRepository.deleteById(id);
    }

    private void validateId(Long id)
    {
        if (id == null || id <= 0)
        {
            throw new RuntimeException("Invalid category id");
        }
    }

    private void validateCategory(Category category)
    {
        if (category == null)
        {
            throw new RuntimeException("Category cannot be null");
        }

        if (category.getName() == null || category.getName().trim().isEmpty())
        {
            throw new RuntimeException("Category name cannot be empty");
        }
    }
}