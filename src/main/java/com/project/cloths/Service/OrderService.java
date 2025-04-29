package com.project.cloths.Service;

import com.project.cloths.Entity.Order;

import java.util.List;
import java.util.Map;

public interface OrderService {
    List<Order> getAllOrders();
    Order placeOrder(Order order);
    Map<String, Object> createOrder(Map<String, Object> request) throws Exception;
    void updateOrderStatus(Long orderId, Map<String, Object> statusRequest) throws Exception;

    // Thêm method mới
    long countPendingOrShippedOrders();
}
