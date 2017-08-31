package ru.javabegin.training.coffee;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CoffeeDAO {
    
    public List<CoffeeType> listCoffeeType(boolean withDisabled) throws SQLException{
        List<CoffeeType> result = new ArrayList<>();
        String sql = "SELECT * FROM coffeetype";
        try(Connection connection = DBConnectionManager.getInstance().getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);)
        {
            while (resultSet.next()) {
                boolean disabled = "Y".equals(resultSet.getString("disabled"));
                if (!withDisabled && disabled)
                    continue;

                long id = resultSet.getInt("id");
                String type = resultSet.getString("type_name");
                float price = resultSet.getFloat("price");
                
                CoffeeType coffee = new CoffeeType(id, type, price, disabled);
                result.add(coffee);
            }
        }

        return result;
    }
    
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
    
//    public static void main(String[] args) {
//        CoffeeDAO coffeeDAO = new CoffeeDAO();
//        System.out.println("CoffeeTypeById=2:");
//        System.out.println(coffeeDAO.getCoffeeTypeById(2));
//    }

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
    
//    public static void main(String[] args){
//        CoffeeDAO coffeeDAO = new CoffeeDAO();
//        System.out.println("m: " + coffeeDAO.getConfigValue("m"));
//        System.out.println("n: " + coffeeDAO.getConfigValue("n"));
//        System.out.println("x: " + coffeeDAO.getConfigValue("x"));
//    }

    public void calculateCost(CoffeeOrder order, List<CoffeeOrderItem> orderItems) throws SQLException {
        final String sqlCreateTable = "CREATE TEMPORARY TABLE IF NOT EXISTS coffee_count "
                + "(type_id INTEGER not NULL, count INTEGER, cost FLOAT, PRIMARY KEY ( type_id ));";
        final String sqlClearTable = "TRUNCATE TABLE coffee_count;";
        final String sqlInsertCount = "INSERT INTO coffee_count (type_id, count) VALUES (?, ?)";
        final String sqlGetCostForType = "SELECT type_id, cost FROM coffee_count";
        
        try (Connection connection = DBConnectionManager.getInstance().getConnection();
            Statement statement = connection.createStatement();
            PreparedStatement insertCountStatement = connection.prepareStatement(sqlInsertCount);
            CallableStatement calculateCostStatement = connection.prepareCall("{call calculate_cost_order(?, ?)}")) 
        {
            statement.executeUpdate(sqlCreateTable);
            statement.executeUpdate(sqlClearTable);
            
            for (CoffeeOrderItem item : orderItems) { //fill temporary table
                insertCountStatement.setLong(1, item.getCoffeeType().getId());
                insertCountStatement.setInt(2, item.getQuantity());
                insertCountStatement.executeUpdate();
            }
            
            calculateCostStatement.registerOutParameter("coffee", java.sql.Types.FLOAT);
            calculateCostStatement.registerOutParameter("delivery", java.sql.Types.FLOAT);
            calculateCostStatement.executeQuery();
            
            //retriew cost for each type from temporary table
            ResultSet resultSet = statement.executeQuery(sqlGetCostForType);
            while (resultSet.next()) {
                int typeId = resultSet.getInt("type_id");
                float cost = resultSet.getFloat("cost");
                
                for (CoffeeOrderItem item : orderItems) {
                    if(item.getCoffeeType().getId() == typeId)
                    {
                        item.setCost(cost);
                        break;
                    }
                }
            }
            
            float coffeeCost = calculateCostStatement.getFloat("coffee");
            order.setCoffeeCost(coffeeCost);
            
            float deliveryCost = calculateCostStatement.getFloat("delivery");
            order.setDeliveryCost(deliveryCost);
            
            order.setTotalCost(coffeeCost + deliveryCost);
        }
    }
    
//    public static void main(String[] args) {
//        CoffeeDAO coffeeDAO = new CoffeeDAO();
//        try {
//            System.out.println("listCoffeeType:");
//            coffeeDAO.listCoffeeType(false).stream().forEach(System.out::println);
//        } catch (SQLException ex) {
//            Logger.getLogger(CoffeeDAO.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }

    private long getNextID(String tableName, Statement statement) throws SQLException {
        final String sql = String.format("SELECT MAX(id) FROM %s", tableName);
        ResultSet resultSet = statement.executeQuery(sql);
        if(resultSet.next())
            return resultSet.getInt(1) + 1;
        
        return -1;
    }
    
//    public static void main(String[] args) {
//        CoffeeDAO coffeeDAO = new CoffeeDAO();
//        System.out.println("NextID:");
//        System.out.println(coffeeDAO.getNextID("coffeetype"));
//    }
    
    public void createOrder(CoffeeOrder order, List<CoffeeOrderItem> orderItems) throws SQLException {
        Connection connection = DBConnectionManager.getInstance().getConnection();
        try (Statement statement = connection.createStatement();) 
        {
            long orderID = getNextID("coffeeorder", statement);
            order.setId(orderID);
            String formattedDate = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(order.getOrderDate());
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
    }
    
//    private static CoffeeOrderItem createCoffeeOrderItem(CoffeeDAO coffeeDAO, CoffeeOrder order, int coffeeType, int quantity){
//        CoffeeOrderItem result = new CoffeeOrderItem();
//        result.setId(coffeeDAO.getNextID("coffeeorderitem"));
//        result.setCoffeeOrder(order);
//        result.setCoffeeType(coffeeDAO.getCoffeeTypeById(coffeeType));
//        result.setQuantity(quantity);
//        return result;
//    }
//    
//    public static void main(String[] args) {
//        CoffeeDAO coffeeDAO = new CoffeeDAO();
//        
//        CoffeeOrder order = new CoffeeOrder();
//        order.setId(coffeeDAO.getNextID("coffeeorder"));
//        order.setOrderDate(new Date());
//        order.setName("vital");
//        order.setDeliveryAddress("Ratomka");
//        order.setTotalCost(50f);
//        
//        List<CoffeeOrderItem> orderItems = new ArrayList<CoffeeOrderItem>();
//        orderItems.add(createCoffeeOrderItem(coffeeDAO, order, 2, 1));
//        orderItems.add(createCoffeeOrderItem(coffeeDAO, order, 3, 2));
//        
//        coffeeDAO.createOrder(order, orderItems);
//    }
}
