package com.project.cloths.Controller;

import com.project.cloths.Entity.*;
import com.project.cloths.Service.OrderService;
import com.project.cloths.Service.TelegramNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private TelegramNotificationService telegramNotificationService;

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PostMapping
    public Map<String, Object> createOrder(@RequestBody Map<String, Object> request) throws Exception {
        return orderService.createOrder(request);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> statusRequest) {
        Order order = orderService.getAllOrders().stream()
                .filter(o -> o.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        String newStatusStr = statusRequest.get("status");
        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(newStatusStr);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + newStatusStr);
        }

        if (!order.getStatus().canTransitionTo(newStatus)) {
            throw new RuntimeException("Cannot transition from " + order.getStatus().name() + " to " + newStatus.name());
        }

        order.setStatus(newStatus);

        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setStatus(newStatus);
        history.setChangedAt(new Date());
        order.getStatusHistory().add(history);

        orderService.placeOrder(order);

        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("🔔 Cập nhật đơn hàng!\n")
                .append("Mã đơn hàng: ").append(order.getId()).append("\n")
                .append("Khách hàng: ").append(order.getCustomerName()).append("\n")
                .append("Trạng thái mới: ").append(newStatus.name()).append("\n")
                .append("Sản phẩm:\n");
        for (CartItem item : order.getItems()) {
            Product product = item.getProduct();
            messageBuilder.append("- ").append(product.getName())
                    .append(" (ID: ").append(product.getId()).append(") x ")
                    .append(item.getQuantity()).append("\n");
        }
        messageBuilder.append("Tổng tiền: ").append(order.getTotalPrice()).append(" VND");
        String message = messageBuilder.toString();
        telegramNotificationService.sendNotification(message);

        Map<String, Object> response = new HashMap<>();
        response.put("id", order.getId().toString());
        response.put("message", "Order status updated to " + newStatus.name());
        return ResponseEntity.ok(response);
    }
}