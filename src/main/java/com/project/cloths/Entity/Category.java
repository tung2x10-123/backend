package com.project.cloths.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @JsonIgnore // Ngăn serialize products khi lấy Product
    private List<Product> products;

    @Override
    public String toString() {
        return "Category{id=" + id + ", name='" + name + "'}";
    }
}