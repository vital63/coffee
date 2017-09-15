package ru.coffee.domain;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "coffeeorderitem", schema = "", catalog = "coffee")
public class CoffeeOrderItem {
    
    @Id
    @Column(name = "id")
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "type_id")
    private CoffeeType coffeeType;
    
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id")
    private CoffeeOrder coffeeOrder;
    
    @Column(name = "quantity")
    private Integer quantity;
    
    @Transient
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
