package com.dds.shared;

import java.io.Serializable;

public class OrderItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private final DrinkBrand drinkBrand;
    private final int quantity;

    public OrderItem(DrinkBrand drinkBrand, int quantity) {
        this.drinkBrand = drinkBrand;
        this.quantity = quantity;
    }

    public DrinkBrand getDrinkBrand() {
        return drinkBrand;
    }

    public int getQuantity() {
        return quantity;
    }
}
