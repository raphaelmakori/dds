package com.dds.client;

import com.dds.shared.AdminReport;
import com.dds.shared.Branch;
import com.dds.shared.BranchSales;
import com.dds.shared.DrinkBrand;
import com.dds.shared.OrderItem;
import com.dds.shared.OrderRecord;

import java.time.format.DateTimeFormatter;
import java.util.Map;

public final class ReportPrinter {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ReportPrinter() {
    }

    public static void print(AdminReport report) {
        System.out.println();
        System.out.println("================ ADMIN REPORT ================");
        System.out.println("1. Customers and Branches Where Orders Were Made");
        if (report.getOrders().isEmpty()) {
            System.out.println("No orders have been placed yet.");
        } else {
            for (OrderRecord order : report.getOrders()) {
                System.out.println("Order #" + order.getOrderId()
                        + " | Customer: " + order.getCustomerName()
                        + " | Branch: " + order.getBranch().getDisplayName()
                        + " | Time: " + order.getOrderTime().format(FORMATTER));
                for (OrderItem item : order.getItems()) {
                    System.out.println("   - " + item.getDrinkBrand().getDisplayName()
                            + " x " + item.getQuantity());
                }
                System.out.println("   Amount: KES " + String.format("%.2f", order.getTotalAmount()));
            }
        }

        System.out.println();
        System.out.println("2. Sales Amount at Each Branch and Headquarter");
        for (BranchSales branchSales : report.getBranchSales()) {
            System.out.println(branchSales.getBranch().getDisplayName() + ": KES "
                    + String.format("%.2f", branchSales.getSalesAmount()));
        }

        System.out.println();
        System.out.println("3. Final Total Amount Made by the Business");
        System.out.println("Total Business Sales: KES " + String.format("%.2f", report.getTotalBusinessSales()));

        System.out.println();
        System.out.println("4. Current Stock by Branch");
        for (Map.Entry<Branch, Map<DrinkBrand, Integer>> entry : report.getStockSnapshot().entrySet()) {
            System.out.println(entry.getKey().getDisplayName() + ":");
            for (Map.Entry<DrinkBrand, Integer> stock : entry.getValue().entrySet()) {
                System.out.println("   - " + stock.getKey().getDisplayName() + ": " + stock.getValue() + " bottles");
            }
        }

        System.out.println();
        System.out.println("5. Low Stock Alerts");
        if (report.getAlerts().isEmpty()) {
            System.out.println("No low-stock alerts at the moment.");
        } else {
            for (String alert : report.getAlerts()) {
                System.out.println("ALERT: " + alert);
            }
        }
        System.out.println("==============================================");
        System.out.println();
    }
}
