package com.project.cloths.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "order_status_history")
public class OrderStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order order;

    @Enumerated(EnumType.STRING) // thêm annotation này
    private OrderStatus status;

    @Temporal(TemporalType.TIMESTAMP)
    private Date changedAt;

    @Override
    public String toString() {
        return "OrderStatusHistory{id=" + id + ", orderId=" + (order != null ? order.getId() : null) + ", status='" + (status != null ? status.name() : null) + "', changedAt=" + changedAt + "}";
    }
}