package com.dds.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AdminReport implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<OrderRecord> orders;
    private final List<BranchSales> branchSales;
    private final double totalBusinessSales;
    private final Map<Branch, Map<DrinkBrand, Integer>> stockSnapshot;
    private final List<String> alerts;

    public AdminReport(List<OrderRecord> orders,
                       List<BranchSales> branchSales,
                       double totalBusinessSales,
                       Map<Branch, Map<DrinkBrand, Integer>> stockSnapshot,
                       List<String> alerts) {
        this.orders = new ArrayList<>(orders);
        this.branchSales = new ArrayList<>(branchSales);
        this.totalBusinessSales = totalBusinessSales;
        this.stockSnapshot = stockSnapshot;
        this.alerts = new ArrayList<>(alerts);
    }

    public List<OrderRecord> getOrders() {
        return Collections.unmodifiableList(orders);
    }

    public List<BranchSales> getBranchSales() {
        return Collections.unmodifiableList(branchSales);
    }

    public double getTotalBusinessSales() {
        return totalBusinessSales;
    }

    public Map<Branch, Map<DrinkBrand, Integer>> getStockSnapshot() {
        return stockSnapshot;
    }

    public List<String> getAlerts() {
        return Collections.unmodifiableList(alerts);
    }
}
