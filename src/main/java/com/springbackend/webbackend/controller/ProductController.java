package com.springbackend.webbackend.controller;

import com.springbackend.webbackend.dto.ProductDTO;
import com.springbackend.webbackend.model.Product;
import com.springbackend.webbackend.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getAllProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            Pageable pageable) {

        Page<ProductDTO> productDTOs = productService.getAllProducts(name, categoryId, minPrice, maxPrice, pageable);
        return ResponseEntity.ok(productDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(product -> new ProductDTO(
                        product.getId(),
                        product.getName(),
                        product.getPrice(),
                        product.getDescription(),
                        product.getStock(),
                        product.getCategory() != null ? product.getCategory().getId() : null
                ))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> saveProduct(@Valid @RequestBody ProductDTO productDTO, BindingResult result) {
        return processProductRequest(productDTO, result, true);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (productService.getProductById(id).isPresent()) {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDTO productDTO, BindingResult result) {
        ResponseEntity<?> errorResponse = handleValidationErrors(result);
        if (errorResponse != null) {
            return errorResponse;
        }

        Optional<ProductDTO> updatedProduct = productService.updateProduct(id, productDTO);
        return updatedProduct
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductDTO>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId, pageable));
    }

    @GetMapping("/category")
    public ResponseEntity<Page<ProductDTO>> getProductsByCategoryName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getProductsByCategoryName(name, pageable));
    }

   // Para guardar el producto
   private ResponseEntity<?> processProductRequest(ProductDTO productDTO, BindingResult result, boolean isNew, Long... id) {
       ResponseEntity<?> errorResponse = handleValidationErrors(result);
       if (errorResponse != null) {
           return errorResponse;
       }

       if (!isNew) {
           Optional<Product> existingProduct = productService.getProductById(id[0]);
           if (existingProduct.isEmpty()) {
               return ResponseEntity.notFound().build();
           }
           productDTO.setId(id[0]);
       }

       Product product = new Product();
       product.setId(productDTO.getId());
       product.setName(productDTO.getName());
       product.setPrice(productDTO.getPrice());
       product.setDescription(productDTO.getDescription());
       product.setStock(productDTO.getStock());

       // Verificar si categoryId no es null antes de buscar la categoría
       if (productDTO.getCategoryId() != null) {
           product.setCategory(productService.findCategoryById(productDTO.getCategoryId()));
       } else {
           product.setCategory(null);
       }

       // Modificar createProduct para aceptar Product o convertir Product a ProductDTO
       ProductDTO savedProductDTO = productService.createProduct(new ProductDTO(
               product.getId(),
               product.getName(),
               product.getPrice(),
               product.getDescription(),
               product.getStock(),
               (product.getCategory() != null) ? product.getCategory().getId() : null
       ));

       return isNew
               ? ResponseEntity.status(HttpStatus.CREATED).body(savedProductDTO)
               : ResponseEntity.ok(savedProductDTO);
   }

    private ResponseEntity<?> handleValidationErrors(BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : result.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errors);
        }
        return null;
    }
}
