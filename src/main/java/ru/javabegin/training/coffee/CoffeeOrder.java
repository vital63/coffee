package ru.javabegin.training.coffee;

import java.util.Date;

public class CoffeeOrder {
    private Long id;
    private Date orderDate;
    private String name;
    private String deliveryAddress;
    private float coffeeCost;
    private float deliveryCost;
    private Float totalCost;

    public CoffeeOrder() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public float getCoffeeCost() {
        return coffeeCost;
    }

    public void setCoffeeCost(float coffeeCost) {
        this.coffeeCost = coffeeCost;
    }

    public float getDeliveryCost() {
        return deliveryCost;
    }

    public void setDeliveryCost(float deliveryCost) {
        this.deliveryCost = deliveryCost;
    }

    public Float getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(Float totalCost) {
        this.totalCost = totalCost;
    }
}
