package com.project.cloths.Service.Impl;

import org.springframework.context.ApplicationEvent;

public class OrderStatusChangedEvent extends ApplicationEvent {

    public OrderStatusChangedEvent(Object source) {
        super(source);
    }
}