package com.dds.shared;

import java.io.Serializable;

public class BranchSales implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Branch branch;
    private final double salesAmount;

    public BranchSales(Branch branch, double salesAmount) {
        this.branch = branch;
        this.salesAmount = salesAmount;
    }

    public Branch getBranch() {
        return branch;
    }

    public double getSalesAmount() {
        return salesAmount;
    }
}
