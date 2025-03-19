package com.springbackend.webbackend.service;

import com.springbackend.webbackend.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    Optional<Category> getCategoryByName(String name);
    List<Category> getAllCategories();
    Category createCategory(Category category);
}

