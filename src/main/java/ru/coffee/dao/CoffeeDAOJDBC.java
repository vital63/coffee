package ru.coffee.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import ru.coffee.domain.CoffeeOrder;
import ru.coffee.domain.CoffeeOrderItem;
import ru.coffee.domain.CoffeeType;
import ru.coffee.service.DBConnectionManager;

//@Repository
public class CoffeeDAOJDBC implements CoffeeDAO {
    
    @Override
    public List<CoffeeType> listCoffeeType(Locale locale, boolean withDisabled) throws SQLException{
        List<CoffeeType> result = new ArrayList<>();
        String sql =  CoffeeDAO.getSqlQueryCoffeeType(locale, withDisabled);
        
        try(Connection connection = DBConnectionManager.getInstance().getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);)
        {
            while (resultSet.next()) {
                boolean disabled = "Y".equals(resultSet.getString("disabled"));

                long id = resultSet.getInt("id");
                String type = resultSet.getString("type_name");
                float price = resultSet.getFloat("price");
                
                CoffeeType coffee = new CoffeeType(id, type, price, disabled);
                result.add(coffee);
            }
        }

        return result;
    }
    
    @Override
    public CoffeeType getCoffeeTypeById(long id) throws SQLException {
        CoffeeType result = null;
        String sql = String.format("SELECT * FROM coffeetype WHERE id='%s'", id);
        try(Connection connection = DBConnectionManager.getInstance().getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);) 
        {
            if (resultSet.next()) {
                String type = resultSet.getString("type_name");
                float price = resultSet.getFloat("price");
                boolean disabled = "Y".equals(resultSet.getString("disabled"));

                result = new CoffeeType(id, type, price, disabled);
            }
        }
        return result;
    }
    
    private String getConfigValue(String parameter) throws SQLException {
        String result = null;
        String sql = String.format("SELECT value FROM configuration WHERE id='%s'", parameter);
        try (
                Connection connection = DBConnectionManager.getInstance().getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql);) {
            if (resultSet.next()) {
                result = resultSet.getString("value");
            }
        }   
        return result;
    }
    
    @Override
    public void calculateCost(CoffeeOrder order, List<CoffeeOrderItem> orderItems) throws SQLException {
        try (Connection connection = DBConnectionManager.getInstance().getConnection();
            Statement statement = connection.createStatement();
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
                    .findFirst().ifPresent(item -> item .setCost(cost));
            }
            
            float coffeeCost = calculateCostStatement.getFloat("coffee");
            order.setCoffeeCost(coffeeCost);
            
            float deliveryCost = calculateCostStatement.getFloat("delivery");
            order.setDeliveryCost(deliveryCost);
            
            order.setTotalCost(coffeeCost + deliveryCost);
        }
    }
    
    private void CreateCoffeeCountTable(Connection connection, List<CoffeeOrderItem> orderItems) throws SQLException {
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
    
    @Override
    public long getNextID(String tableName) throws SQLException {
        try (Connection connection = DBConnectionManager.getInstance().getConnection();
             Statement statement = connection.createStatement();) 
        {
            return getNextID(tableName, statement);
        }
    }

    private long getNextID(String tableName, Statement statement) throws SQLException {
        final String sql = String.format("SELECT MAX(id) FROM %s", tableName);
        ResultSet resultSet = statement.executeQuery(sql);
        if(resultSet.next()){
            return resultSet.getInt(1) + 1;
        }
        return -1;
    }
    
    @Override
    public CoffeeOrder getOrder(long id, List<CoffeeOrderItem> orderItems) throws SQLException, ParseException{
        CoffeeOrder result = null;
        if(orderItems == null)
            return result;
        
        orderItems.clear();
        final String sql = String.format("SELECT * FROM coffeeorder WHERE id = %d", id);
        
        try (Connection connection = DBConnectionManager.getInstance().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql); )
        {
            if(!resultSet.next()){
                return result;
            }
            
            assert(id == resultSet.getInt("id"));
            String dateString = resultSet.getString("order_date");
            Date orderDate = coffeeOrderDateFormat.parse(dateString);
            String name = resultSet.getString("name");
            String deliveryAddress = resultSet.getString("delivery_address");
            float totalCost = resultSet.getFloat("cost");
            
            result = new CoffeeOrder(id, orderDate, name, deliveryAddress, totalCost);
            
            getOrderItems(result, orderItems, statement);
        }
        return result;
    }
    
    private void getOrderItems(CoffeeOrder order, List<CoffeeOrderItem> orderItems, Statement statement) throws SQLException{
        final String sql = String.format("SELECT * FROM coffeeorderitem WHERE order_id = %d", order.getId());
        ResultSet resultSet = statement.executeQuery(sql);
        while (resultSet.next()){
            long id = resultSet.getLong("id");
            
            long typeId = resultSet.getLong("type_id");
            CoffeeType coffeeType = getCoffeeTypeById(typeId);
            
            long orderId = resultSet.getLong("order_id");
            assert(orderId == order.getId());
            
            int quantity = resultSet.getInt("quantity");
            
            orderItems.add(new CoffeeOrderItem(id, coffeeType, order, quantity, 0));
        }
    }
    
    public static SimpleDateFormat coffeeOrderDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

    @Override
    public void createOrder(CoffeeOrder order, List<CoffeeOrderItem> orderItems) throws SQLException {
        Connection connection = DBConnectionManager.getInstance().getConnection();
        try (Statement statement = connection.createStatement();) 
        {
            long orderID = getNextID("coffeeorder", statement);
            order.setId(orderID);
            String formattedDate = coffeeOrderDateFormat.format(order.getOrderDate());
            final String sql = String.format(Locale.US,
                    "INSERT INTO coffeeorder (id, order_date, name, delivery_address, cost)\n"
                            + "VALUES (%d, '%s', '%s', '%s', %f)",
                    orderID, formattedDate, order.getName(), order.getDeliveryAddress(), order.getTotalCost());
            
            connection.setAutoCommit(false);
            if (statement.executeUpdate(sql) > 0)
            {
                for(CoffeeOrderItem orderItem: orderItems){
                    createOrderItem(orderItem, statement);
                }
                connection.commit();
            }else
                connection.rollback();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
        finally{
            connection.close();
        }
    }
    
    private void createOrderItem(CoffeeOrderItem orderItem, Statement statement) throws SQLException{
        long orderItemID = getNextID("coffeeorderitem", statement);
        final String sql = String.format(
            "INSERT INTO coffeeorderitem (id, type_id, order_id, quantity) VALUES (%d, %d, %d, %d)", 
            orderItemID, orderItem.getCoffeeType().getId(), orderItem.getCoffeeOrder().getId(),
            orderItem.getQuantity());
        statement.executeUpdate(sql);
        orderItem.setId(orderItemID);
    }
}
