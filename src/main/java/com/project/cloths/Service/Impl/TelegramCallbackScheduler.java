package com.project.cloths.Service.Impl;

import com.project.cloths.Entity.TelegramConfig;
import com.project.cloths.Service.OrderService;
import com.project.cloths.repository.TelegramConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TelegramCallbackScheduler {

    @Autowired
    private TelegramCallbackHandler telegramCallbackHandler;

    @Autowired
    private TelegramConfigRepository telegramConfigRepository;

    @Autowired
    private OrderService orderService;

    private boolean hasPendingOrShippedOrders = false;
    private long lastCheckedCount = -1;

    @Scheduled(fixedRate = 10000)
    public void checkTelegramCallbacks() {
        long currentCount = orderService.countPendingOrShippedOrders();
        if (currentCount != lastCheckedCount) {
            hasPendingOrShippedOrders = currentCount > 0;
            lastCheckedCount = currentCount;
            System.out.println("Checked database: hasPendingOrShippedOrders=" + hasPendingOrShippedOrders + ", count=" + currentCount);
        }

        if (!hasPendingOrShippedOrders) {
            System.out.println("No orders in PENDING or SHIPPED state. Skipping callback check.");
            return;
        }

        List<TelegramConfig> configs = telegramConfigRepository.findAll();
        if (configs.isEmpty()) {
            System.out.println("No Telegram configurations found in database. Cannot handle callback.");
            return;
        }

        for (TelegramConfig config : configs) {
            telegramCallbackHandler.handleCallback(config.getBotToken(), config.getChatId());
        }
    }

    @EventListener
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        resetCache();
    }

    public void resetCache() {
        lastCheckedCount = -1;
        System.out.println("Cache reset: will check database on next schedule.");
    }
}