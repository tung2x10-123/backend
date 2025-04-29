package com.project.cloths.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @JsonManagedReference
//    @JsonIgnore
    private Product product;

    private int quantity;

    @Transient
    private Long productId;

    @Override
    public String toString() {
        return "CartItem{id=" + id + ", quantity=" + quantity + ", productId=" + (product != null ? product.getId() : null) + "}";
    }
}