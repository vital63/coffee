package ru.coffee.dao;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import ru.coffee.domain.CoffeeOrder;
import ru.coffee.domain.CoffeeOrderItem;
import ru.coffee.domain.CoffeeType;

public interface CoffeeDAO {
    void calculateCost(CoffeeOrder order, List<CoffeeOrderItem> orderItems) throws SQLException;
    void createOrder(CoffeeOrder order, List<CoffeeOrderItem> orderItems) throws SQLException;
    CoffeeType getCoffeeTypeById(long id) throws SQLException;
    long getNextID(String tableName) throws SQLException;
    CoffeeOrder getOrder(long id, List<CoffeeOrderItem> orderItems) throws SQLException, ParseException;
    List<CoffeeType> listCoffeeType(Locale locale, boolean withDisabled) throws SQLException;
}
