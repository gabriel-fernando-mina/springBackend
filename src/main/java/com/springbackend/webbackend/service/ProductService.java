package com.springbackend.webbackend.service;

import com.springbackend.webbackend.dto.ProductDTO;
import com.springbackend.webbackend.model.Category;
import com.springbackend.webbackend.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface ProductService {
    Page<ProductDTO> getAllProducts(String name, Long categoryId, Double minPrice, Double maxPrice, Pageable pageable);
    Optional<Product> getProductById(Long id);
    ProductDTO createProduct(ProductDTO productDTO);
    Optional<ProductDTO> updateProduct(Long id, ProductDTO updatedProduct);
    void deleteProduct(Long id);
    Category findCategoryById(Long categoryId);
    Page<ProductDTO> getProductsByCategory(Long categoryId, Pageable pageable);
    Page<ProductDTO> getProductsByCategoryName(String categoryName, Pageable pageable);
}
