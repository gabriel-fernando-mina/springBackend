package com.springbackend.springBackend.service;

import com.springbackend.springBackend.dto.ProductDTO;
import com.springbackend.springBackend.model.Category;
import com.springbackend.springBackend.model.Product;
import com.springbackend.springBackend.model.ProductSpecification;
import com.springbackend.springBackend.repository.CategoryRepository;
import com.springbackend.springBackend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public Page<ProductDTO> getAllProducts(String name, Long categoryId, Double minPrice, Double maxPrice, Pageable pageable) {
        log.info("Obteniendo productos con filtros: name={}, categoryId={}, minPrice={}, maxPrice={}",
                name, categoryId, minPrice, maxPrice);

        Specification<Product> spec = ProductSpecification.filterByCriteria(name, categoryId, minPrice, maxPrice);
        return productRepository.findAll(spec, pageable).map(this::convertToDTO);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Producto con ID " + id + " no encontrado");
        }
        productRepository.deleteById(id);
    }

    @Override
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = convertToEntity(productDTO);
        Product savedProduct = productRepository.save(product);
        return convertToDTO(savedProduct);
    }

    @Override
    public Optional<ProductDTO> updateProduct(Long id, ProductDTO updatedProduct) {
        return productRepository.findById(id).map(existingProduct -> {
            existingProduct.setName(updatedProduct.getName());
            existingProduct.setPrice(updatedProduct.getPrice());
            return convertToDTO(productRepository.save(existingProduct));
        });
    }

    // Metodos auxiliares para la conversión entre entidades y DTOs
    private ProductDTO convertToDTO(Product product) {
        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                product.getStock(),
                product.getCategory() != null ? product.getCategory().getId() : null
        );
    }

    private Product convertToEntity(ProductDTO productDTO) {
        Product product = new Product();
        product.setId(productDTO.getId());
        product.setName(productDTO.getName());
        product.setPrice(productDTO.getPrice());
        product.setDescription(productDTO.getDescription());
        product.setStock(productDTO.getStock());

        if (productDTO.getCategoryId() != null) {
            Category category = new Category();
            category.setId(productDTO.getCategoryId());
            product.setCategory(category);
        }

        return product;
    }

    @Override
    public Category findCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
    }

    @Override
    public Page<ProductDTO> getProductsByCategory(Long categoryId, Pageable pageable) {
        log.info("Obteniendo productos de la categoría con ID: {}", categoryId);
        return productRepository.findByCategoryId(categoryId, pageable).map(this::convertToDTO);
    }

    @Override
    public Page<ProductDTO> getProductsByCategoryName(String categoryName, Pageable pageable) {
        log.info("Obteniendo productos de la categoría con nombre: {}", categoryName);
        Optional<Category> category = categoryRepository.findByName(categoryName);
        return category.map(c -> productRepository.findByCategoryId(c.getId(), pageable).map(this::convertToDTO))
                .orElse(Page.empty());
    }
}
