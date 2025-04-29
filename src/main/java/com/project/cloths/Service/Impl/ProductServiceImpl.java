package com.project.cloths.Service.Impl;

import com.project.cloths.Entity.Category;
import com.project.cloths.Entity.Product;
import com.project.cloths.Service.ProductService;
import com.project.cloths.repository.CategoryRepository;
import com.project.cloths.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

//    @Override
//    public List<Product> getAllProducts() {
//        return productRepository.findAll();  // Fix getAllProducts() trả về dữ liệu
//    }

    @Override
    public List<Product> getProducts(Long categoryId) {
        if (categoryId != null) {
            return productRepository.findByCategoryId(categoryId);
        }
        return productRepository.findAll();
    }

    @Override
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }
    @Override
    @Transactional  // Đảm bảo quá trình lưu dữ liệu
    public Product addProduct(Product product) {
        if (product.getCategory() != null && product.getCategory().getId() != null) {
            Optional<Category> categoryOpt = categoryRepository.findById(product.getCategory().getId());
            if (categoryOpt.isPresent()) {
                product.setCategory(categoryOpt.get());
            } else {
                throw new RuntimeException("Category not found with ID: " + product.getCategory().getId());
            }
        } else {
            throw new RuntimeException("Category ID is required");
        }

        return productRepository.save(product);
    }
}
