package com.project.cloths.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.cloths.Entity.Category;
import com.project.cloths.Entity.Product;
import com.project.cloths.Model.RequestModel;
import com.project.cloths.Model.ResponseModel2;
import com.project.cloths.Service.ProductService;
import com.project.cloths.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

//    @GetMapping
//    public List<Product> getAllProducts() {
//        return productService.getAllProducts();
//    }

    @GetMapping("/{id}")
    public Optional<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @GetMapping
    public List<Product> getProducts(@RequestParam(value = "categoryId", required = false) Long categoryId) {
        return productService.getProducts(categoryId);
    }

    @PostMapping
    public ResponseModel2<Product> addProduct(@RequestBody RequestModel requestModel) {
        Map<String, Object> body = requestModel.getBody();

        // Kiểm tra body có chứa category không
        if (!body.containsKey("categoryId")) {
            throw new IllegalArgumentException("Category is required");
        }

        // Map body -> Product
        Product product = objectMapper.convertValue(body, Product.class);

        // Lấy Category từ category
        Object categoryObj = body.get("categoryId");
        if (!(categoryObj instanceof Number)) {
            throw new IllegalArgumentException("Category must be a number");
        }
        Long categoryId = ((Number) categoryObj).longValue();
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));

        product.setCategory(category); // Gán category vào product

        // Lưu vào DB
        Product savedProduct = productService.addProduct(product);

        return new ResponseModel2<>("201", "Product created successfully", savedProduct);
    }
}
