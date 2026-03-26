package com.dds.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomerOrder implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String customerName;
    private final Branch branch;
    private final List<OrderItem> items;

    public CustomerOrder(String customerName, Branch branch, List<OrderItem> items) {
        this.customerName = customerName;
        this.branch = branch;
        this.items = new ArrayList<>(items);
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
}
