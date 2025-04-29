package com.project.cloths.Service;

import com.project.cloths.Entity.Product;
import java.util.List;
import java.util.Optional;

public interface ProductService {
//    List<Product> getAllProducts();
    Optional<Product> getProductById(Long id);
    List<Product> getProducts(Long id);
    Product addProduct(Product product);
}
