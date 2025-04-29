package com.project.cloths.repository;


import com.project.cloths.Entity.Order;
import com.project.cloths.Entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o JOIN FETCH o.items i JOIN FETCH i.product p JOIN FETCH p.category")
    List<Order> findAllWithItemsAndProducts();

    // Thêm query để đếm số đơn hàng ở trạng thái PENDING hoặc SHIPPED
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status IN :statuses")
    long countByStatusIn(List<OrderStatus> statuses);
}
