package ru.coffee.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Locale;
import ru.coffee.domain.CoffeeOrder;
import ru.coffee.domain.CoffeeOrderItem;

public class CoffeeJDBCUtils {

    public static void calculateCost(Connection connection, CoffeeOrder order, List<CoffeeOrderItem> orderItems) throws SQLException {
        try (Statement statement = connection.createStatement();
             CallableStatement calculateCostStatement = connection.prepareCall("{call calculate_cost_order(?, ?)}")) 
        {
            CreateCoffeeCountTable(connection, orderItems);

            //call special stored procedure that fill cost in temp table
            calculateCostStatement.registerOutParameter("coffee", java.sql.Types.FLOAT);
            calculateCostStatement.registerOutParameter("delivery", java.sql.Types.FLOAT);
            calculateCostStatement.executeQuery();

            //retriew cost for each type from temporary table
            final String sqlGetCostForType = "SELECT type_id, cost FROM coffee_count";
            ResultSet resultSet = statement.executeQuery(sqlGetCostForType);
            while (resultSet.next()) {
                int typeId = resultSet.getInt("type_id");
                float cost = resultSet.getFloat("cost");
                orderItems.stream().filter(item -> item.getCoffeeType().getId() == typeId)
                    .findFirst().ifPresent(item -> item.setCost(cost));
            }

            float coffeeCost = calculateCostStatement.getFloat("coffee");
            order.setCoffeeCost(coffeeCost);

            float deliveryCost = calculateCostStatement.getFloat("delivery");
            order.setDeliveryCost(deliveryCost);

            order.setTotalCost(coffeeCost + deliveryCost);
        }
    }

    
    private static void CreateCoffeeCountTable(Connection connection, List<CoffeeOrderItem> orderItems) throws SQLException {
        final String sqlCreateTable = "CREATE TEMPORARY TABLE IF NOT EXISTS coffee_count "
                + "(type_id INTEGER not NULL, count INTEGER, cost FLOAT, PRIMARY KEY ( type_id ));";
        
        final String sqlClearTable = "TRUNCATE TABLE coffee_count;";

        final String sqlInsertCount = "INSERT INTO coffee_count (type_id, count) VALUES (?, ?)";

        try(Statement statement = connection.createStatement();)
        {
            statement.executeUpdate(sqlCreateTable);
            statement.executeUpdate(sqlClearTable);

            PreparedStatement insertCountStatement = connection.prepareStatement(sqlInsertCount);

            for (CoffeeOrderItem item : orderItems) { //fill temporary table
                insertCountStatement.setLong(1, item.getCoffeeType().getId());
                insertCountStatement.setInt(2, item.getQuantity());
                insertCountStatement.executeUpdate();
            }
        }
    }
    
    public static String getSqlQueryCoffeeType(Locale locale, boolean withDisabled) {
        String result;
        if (locale != null && "ru".equalsIgnoreCase(locale.toLanguageTag())) {
            result = "SELECT t.id, tr.type_name, t.price, t.disabled FROM coffee.coffeetype t "
                    + "LEFT JOIN coffeetypetranslate_ru tr on tr.id=t.id ";
        } else {
            result = "SELECT * FROM coffeetype ";
        }
        if (!withDisabled) {
            result += "WHERE (disabled is null or disabled<>'Y') ";
        }
        return result;
    }
}
