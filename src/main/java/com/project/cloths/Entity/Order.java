package com.project.cloths.Entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;
    private String customerAddress;
    private String customerPhone;

    @Temporal(TemporalType.TIMESTAMP)
    private Date orderDate;

    private double totalPrice;

    @Enumerated(EnumType.STRING) // thêm annotation này
    private OrderStatus status;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id")
    @JsonManagedReference
    private List<CartItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    @Override
    public String toString() {
        return "Order{id=" + id + ", customerName='" + customerName + "', totalPrice=" + totalPrice + ", status='" + (status != null ? status.name() : null) + "'}";
    }
}