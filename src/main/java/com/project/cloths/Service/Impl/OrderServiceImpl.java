package com.project.cloths.Service.Impl;

import com.project.cloths.Entity.*;
import com.project.cloths.Service.OrderService;
import com.project.cloths.Service.TelegramNotificationService;
import com.project.cloths.repository.OrderRepository;
import com.project.cloths.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TelegramNotificationService telegramNotificationService;

    @Autowired
    private ApplicationEventPublisher eventPublisher; // Thêm để phát event

    @Override
    public List<Order> getAllOrders() {
        List<Order> orders = orderRepository.findAllWithItemsAndProducts();
        System.out.println("Raw orders from DB - Count: " + orders.size());
        orders.forEach(order -> {
            System.out.println("Order ID: " + order.getId() + ", Items before filter: " + order.getItems().size());
            List<CartItem> validItems = order.getItems().stream()
                    .filter(item -> item.getProduct() != null)
                    .collect(Collectors.toList());
            order.setItems(validItems);
            System.out.println("Order ID: " + order.getId() + ", Items after filter: " + validItems.size());
        });
        System.out.println("Processed orders - Count: " + orders.size());
        return orders;
    }

    @Override
    public Order placeOrder(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public Map<String, Object> createOrder(Map<String, Object> request) throws Exception {
        System.out.println("Received order data: " + request);

        String customerName = (String) request.get("customerName");
        String customerAddress = (String) request.get("customerAddress");
        String customerPhone = (String) request.get("customerPhone");
        String orderDateStr = (String) request.get("orderDate");
        Double totalPrice = Double.parseDouble(request.get("totalPrice").toString());
        List<Map<String, Object>> items = (List<Map<String, Object>>) request.get("items");

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Items list cannot be null or empty");
        }

        for (Map<String, Object> item : items) {
            Long productId = Long.parseLong(item.get("productId").toString());
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product with id " + productId + " not found"));
            System.out.println("Found product: id=" + product.getId() + ", name=" + product.getName());
        }

        Order order = new Order();
        order.setCustomerName(customerName);
        order.setCustomerAddress(customerAddress);
        order.setCustomerPhone(customerPhone);
        order.setOrderDate(new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(orderDateStr));
        order.setTotalPrice(totalPrice);
        order.setStatus(OrderStatus.PENDING);

        List<CartItem> cartItems = items.stream().map(item -> {
            CartItem cartItem = new CartItem();
            Long productId = Long.parseLong(item.get("productId").toString());
            Product product = productRepository.findById(productId).get();
            cartItem.setProduct(product);
            cartItem.setQuantity(Integer.parseInt(item.get("quantity").toString()));
            cartItem.setProductId(productId);
            System.out.println("Created cart item: productId=" + product.getId() + ", quantity=" + cartItem.getQuantity());
            return cartItem;
        }).collect(Collectors.toList());
        order.setItems(cartItems);

        Order savedOrder = orderRepository.save(order);

        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(savedOrder);
        history.setStatus(OrderStatus.PENDING);
        history.setChangedAt(new Date());
        savedOrder.getStatusHistory().add(history);
        orderRepository.save(savedOrder);

        System.out.println("Saved order: id=" + savedOrder.getId() + ", customerName=" + savedOrder.getCustomerName());
        System.out.println("Saved items count: " + (savedOrder.getItems() != null ? savedOrder.getItems().size() : 0));
        if (savedOrder.getItems() != null) {
            savedOrder.getItems().forEach(item -> {
                System.out.println("Saved item: id=" + item.getId() + ", productId=" + (item.getProduct() != null ? item.getProduct().getId() : "null") + ", quantity=" + item.getQuantity());
            });
        }

        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("🔔 ĐƠN HÀNG MỚI\n")
                .append("--------------------\n")
                .append("📦 Mã đơn hàng: ").append(savedOrder.getId()).append("\n")
                .append("👤 Khách hàng: ").append(savedOrder.getCustomerName()).append("\n")
                .append("📍 Địa chỉ: ").append(savedOrder.getCustomerAddress()).append("\n")
                .append("📞 Số điện thoại: ").append(savedOrder.getCustomerPhone()).append("\n")
                .append("⏰ Thời gian đặt: ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(savedOrder.getOrderDate())).append("\n")
                .append("📋 Trạng thái: ").append(savedOrder.getStatus().name()).append("\n")
                .append("🛒 Sản phẩm:\n");
        for (CartItem item : savedOrder.getItems()) {
            Product product = item.getProduct();
            messageBuilder.append("   - ").append(product.getName())
                    .append(" (ID: ").append(product.getId()).append(") x ")
                    .append(item.getQuantity())
                    .append(" | ").append(product.getPrice() * item.getQuantity()).append(" VND\n");
        }
        messageBuilder.append("💰 Tổng tiền: ").append(savedOrder.getTotalPrice()).append(" VND\n")
                .append("--------------------");

        String message = messageBuilder.toString();
        String photoUrl = savedOrder.getItems().isEmpty() ? null : savedOrder.getItems().get(0).getProduct().getImageUrl();
        boolean isValidUrl = photoUrl != null && photoUrl.startsWith("http");
        if (isValidUrl) {
            telegramNotificationService.sendNotificationWithPhoto(message, photoUrl, savedOrder.getId(), savedOrder.getStatus());
        } else {
            telegramNotificationService.sendNotificationWithButtons(message, savedOrder.getId(), savedOrder.getStatus());
        }

        // Phát event sau khi tạo đơn hàng mới
        eventPublisher.publishEvent(new OrderStatusChangedEvent(this));

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("id", savedOrder.getId());
        responseData.put("customerName", savedOrder.getCustomerName());
        responseData.put("customerAddress", savedOrder.getCustomerAddress());
        responseData.put("customerPhone", savedOrder.getCustomerPhone());
        responseData.put("orderDate", savedOrder.getOrderDate().toString());
        responseData.put("totalPrice", savedOrder.getTotalPrice());
        responseData.put("status", savedOrder.getStatus().name());

        if (savedOrder.getItems() != null) {
            List<Map<String, Object>> responseItems = savedOrder.getItems().stream().map(item -> {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("productId", item.getProduct() != null ? item.getProduct().getId() : null);
                itemMap.put("quantity", item.getQuantity());
                return itemMap;
            }).collect(Collectors.toList());
            responseData.put("items", responseItems);
        } else {
            responseData.put("items", null);
        }

        if (savedOrder.getStatusHistory() != null) {
            List<Map<String, Object>> historyItems = savedOrder.getStatusHistory().stream().map(historyItem -> {
                Map<String, Object> historyMap = new HashMap<>();
                historyMap.put("status", historyItem.getStatus());
                historyMap.put("changedAt", historyItem.getChangedAt().toString());
                return historyMap;
            }).collect(Collectors.toList());
            responseData.put("statusHistory", historyItems);
        } else {
            responseData.put("statusHistory", null);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", savedOrder.getId().toString());
        response.put("message", "Order created successfully");
        response.put("data", responseData);

        return response;
    }

    @Override
    public void updateOrderStatus(Long orderId, Map<String, Object> statusRequest) throws Exception {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        String newStatusStr = (String) statusRequest.get("status");
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

        orderRepository.save(order);

        String message;
        if (newStatus == OrderStatus.SHIPPED) {
            message = "✅ ĐƠN HÀNG ĐÃ ĐƯỢC XÁC NHẬN\n" +
                    "--------------------\n" +
                    "📦 Mã đơn hàng: " + orderId + "\n" +
                    "📋 Trạng thái mới: SHIPPED\n" +
                    "⏰ Thời gian cập nhật: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()) + "\n" +
                    "--------------------";
        } else {
            message = "❌ ĐƠN HÀNG ĐÃ BỊ HỦY\n" +
                    "--------------------\n" +
                    "📦 Mã đơn hàng: " + orderId + "\n" +
                    "📋 Trạng thái mới: CANCELLED\n" +
                    "⏰ Thời gian cập nhật: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()) + "\n" +
                    "--------------------";
        }
        telegramNotificationService.sendNotificationWithButtons(message, orderId, newStatus);

        // Phát event sau khi cập nhật trạng thái
        eventPublisher.publishEvent(new OrderStatusChangedEvent(this));
    }

    @Override
    public long countPendingOrShippedOrders() {
        return orderRepository.countByStatusIn(Arrays.asList(OrderStatus.PENDING, OrderStatus.SHIPPED));
    }
}