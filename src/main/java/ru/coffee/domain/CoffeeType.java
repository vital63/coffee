package ru.coffee.domain;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "coffeetype", schema = "", catalog = "coffee")
public class CoffeeType implements Serializable {
    @Id
    @Column(name = "id")
    private Long id;
    
    @Column(name = "type_name")
    private String type;
    
    @Column(name = "price")
    private Float price;
    
    @Column(name = "disabled")
    private String disabledString;

    public CoffeeType(Long id, String type, Float price, Boolean disabled) {
        this.id = id;
        this.type = type;
        this.price = price;
        setDisabled(disabled);
    }

    public CoffeeType() {
    }

    public CoffeeType(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Coffee{" + "id=" + id + ", type=" + type + ", price=" + price + ", disabled=" + getDisabled() + '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public Boolean getDisabled() {
        return "Y".equals(disabledString);
    }

    public void setDisabled(Boolean disabled) {
        disabledString = (disabled != null && disabled) ? "Y" : "";
    }
    
    public String getDisabledString() {
        return disabledString;
    }
    
    public void setDisabledString(String disabledString) {
        this.disabledString = disabledString;
    }
}