package com.project.cloths.Service;

import com.project.cloths.Entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();
    Category addCategory(Category category);
}
