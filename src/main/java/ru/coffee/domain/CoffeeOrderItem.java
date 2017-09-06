package ru.coffee.domain;

import java.util.Objects;

public class CoffeeOrderItem {
    private Long id;
    private CoffeeType coffeeType;
    private CoffeeOrder coffeeOrder;
    private Integer quantity;
    private float cost;

    public CoffeeOrderItem() {
    }

    public CoffeeOrderItem(Long coffeeTypeID, Integer quantity, float cost) {
        coffeeType = new CoffeeType(coffeeTypeID);
        this.quantity = quantity;
        this.cost = cost;
    }

    public CoffeeOrderItem(Long id, CoffeeType coffeeType, CoffeeOrder coffeeOrder, Integer quantity, float cost) {
        this.id = id;
        this.coffeeType = coffeeType;
        this.coffeeOrder = coffeeOrder;
        this.quantity = quantity;
        this.cost = cost;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CoffeeType getCoffeeType() {
        return coffeeType;
    }

    public void setCoffeeType(CoffeeType coffeeTypeId) {
        this.coffeeType = coffeeTypeId;
    }

    public CoffeeOrder getCoffeeOrder() {
        return coffeeOrder;
    }

    public void setCoffeeOrder(CoffeeOrder coffeeOrder) {
        this.coffeeOrder = coffeeOrder;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    @Override
    public String toString() {
        return "CoffeeOrderItem{" + "id=" + id + ", coffeeType=" + coffeeType + ", coffeeOrder=" + coffeeOrder + ", quantity=" + quantity + ", cost=" + cost + '}';
    }
}
