package com.project.cloths.Entity;

public enum OrderStatus {
    PENDING,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus newStatus) {
        switch (this) {
            case PENDING:
                return newStatus == SHIPPED || newStatus == CANCELLED;
            case SHIPPED:
                return newStatus == DELIVERED;
            case DELIVERED:
                return false; // Không thể chuyển trạng thái từ DELIVERED
            case CANCELLED:
                return false; // Không thể chuyển trạng thái từ CANCELLED
            default:
                return false;
        }
    }
}