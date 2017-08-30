package ru.javabegin.training.coffee;

public class CoffeeType {
    private Long id;
    private String type;
    private Float price;
    private Boolean disabled;

    public CoffeeType(Long id, String type, Float price, Boolean disabled) {
        this.id = id;
        this.type = type;
        this.price = price;
        this.disabled = disabled;
    }

    public CoffeeType() {
    }

    @Override
    public String toString() {
        return "Coffee{" + "id=" + id + ", type=" + type + ", price=" + price + ", disabled=" + disabled + '}';
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Float getPrice() {
        return price;
    }

    public Boolean getDisabled() {
        return disabled;
    }
}
