package com.loyalty.system.service;

import com.loyalty.system.dto.ProductDto;
import com.loyalty.system.model.Product;
import com.loyalty.system.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllActiveProducts(String category, String search) {
        if (search != null && !search.isBlank()) {
            return productRepository.searchProducts(search.trim());
        }
        if (category != null && !category.isBlank() && !category.equalsIgnoreCase("All")) {
            return productRepository.findByCategoryIgnoreCaseAndActiveTrue(category.trim());
        }
        return productRepository.findByActiveTrue();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));
    }

    public List<String> getAllCategories() {
        return productRepository.findDistinctCategories();
    }

    public List<Product> getAllProductsForAdmin() {
        return productRepository.findAll();
    }

    @Transactional
    public Product createProduct(ProductDto dto) {
        Product product = new Product(
            dto.getName().trim(),
            dto.getCategory().trim(),
            dto.getDescription(),
            dto.getPrice(),
            dto.getImageUrl() != null && !dto.getImageUrl().isBlank() ? dto.getImageUrl().trim() : "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=600&auto=format&fit=crop&q=80",
            4.5,
            0,
            dto.getStockQuantity() != null ? dto.getStockQuantity() : 100
        );
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, ProductDto dto) {
        Product product = getProductById(id);
        product.setName(dto.getName().trim());
        product.setCategory(dto.getCategory().trim());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        if (dto.getImageUrl() != null && !dto.getImageUrl().isBlank()) {
            product.setImageUrl(dto.getImageUrl().trim());
        }
        if (dto.getStockQuantity() != null) {
            product.setStockQuantity(dto.getStockQuantity());
        }
        if (dto.getActive() != null) {
            product.setActive(dto.getActive());
        }
        return productRepository.save(product);
    }

    @Transactional
    public void toggleProductActive(Long id) {
        Product product = getProductById(id);
        product.setActive(!product.getActive());
        productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        product.setActive(false);
        productRepository.save(product);
    }
}
