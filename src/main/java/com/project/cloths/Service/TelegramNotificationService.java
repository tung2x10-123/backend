package com.project.cloths.Service;

import com.project.cloths.Entity.OrderStatus;

public interface TelegramNotificationService {
    void sendNotification(String message);
    void sendNotificationWithButtons(String message, Long orderId, OrderStatus orderStatus);
    void sendNotificationWithPhoto(String message, String photoUrl, Long orderId, OrderStatus orderStatus);
}