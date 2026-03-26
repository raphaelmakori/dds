package com.dds.shared;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private final long orderId;
    private final String customerName;
    private final Branch branch;
    private final List<OrderItem> items;
    private final double totalAmount;
    private final LocalDateTime orderTime;

    public OrderRecord(long orderId, String customerName, Branch branch, List<OrderItem> items,
                       double totalAmount, LocalDateTime orderTime) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.branch = branch;
        this.items = new ArrayList<>(items);
        this.totalAmount = totalAmount;
        this.orderTime = orderTime;
    }

    public long getOrderId() {
        return orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public Branch getBranch() {
        return branch;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getOrderTime() {
        return orderTime;
    }
}
