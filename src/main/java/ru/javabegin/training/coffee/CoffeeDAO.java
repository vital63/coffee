package ru.javabegin.training.coffee;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CoffeeDAO {
    
    public List<CoffeeType> listCoffeeType(boolean withDisabled) {
        List<CoffeeType> result = new ArrayList<>();
        String sql = "SELECT * FROM coffeetype";
        try(
            Connection connection = DBConnectionManager.getInstance().getConnection();
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
        catch(SQLException e)
        {
            Logger.getLogger(CoffeeDAO.class.getName()).log(Level.SEVERE, null, e);
        }

        return result;
    }
    
    public CoffeeType getCoffeeTypeById(long id) {
        CoffeeType result = null;
        String sql = String.format("SELECT * FROM coffeetype WHERE id='%s'", id);
        try (
                Connection connection = DBConnectionManager.getInstance().getConnection();
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
        catch (SQLException e) {
            Logger.getLogger(CoffeeDAO.class.getName()).log(Level.SEVERE, null, e);
        }

        return result;
    }
    
//    public static void main(String[] args) {
//        CoffeeDAO coffeeDAO = new CoffeeDAO();
//        System.out.println("CoffeeTypeById=2:");
//        System.out.println(coffeeDAO.getCoffeeTypeById(2));
//    }

    private String getConfigValue(String parameter){
        String result = null;
        String sql = String.format("SELECT value FROM configuration WHERE id='%s'", parameter);
        try (
                Connection connection = DBConnectionManager.getInstance().getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql);) {
            if (resultSet.next()) {
                result = resultSet.getString("value");
            }
        } catch (SQLException e){
            Logger.getLogger(CoffeeDAO.class.getName()).log(Level.SEVERE, null, e);
        }    
        return result;
    }
    
//    public static void main(String[] args){
//        CoffeeDAO coffeeDAO = new CoffeeDAO();
//        System.out.println("m: " + coffeeDAO.getConfigValue("m"));
//        System.out.println("n: " + coffeeDAO.getConfigValue("n"));
//        System.out.println("x: " + coffeeDAO.getConfigValue("x"));
//    }
    //TODO: Move in db
    public void calculateCost(CoffeeOrder order, List<CoffeeOrderItem> orderItems){
        int n = Integer.parseInt(getConfigValue("n"));
        float m = Float.parseFloat(getConfigValue("m"));
        float x = Float.parseFloat(getConfigValue("x"));
        
        float coffeeCost = 0;
        for(CoffeeOrderItem item: orderItems){
            int discountN = (int)(item.getQuantity() / n);
            item.setCost((item.getQuantity() - discountN) * item.getCoffeeType().getPrice());
            coffeeCost += item.getCost();
        }
        float deliveryCost = coffeeCost > x ? 0 : m;
                
        order.setDeliveryCost(deliveryCost);
        order.setCoffeeCost(coffeeCost);
        order.setTotalCost(coffeeCost + deliveryCost);
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
    
    private long getNextID(String tableName){
        final String sql = String.format("SELECT MAX(id) FROM %s", tableName);
        try (   Connection connection = DBConnectionManager.getInstance().getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql);) 
        {
            if(resultSet.next())
                return resultSet.getInt(1) + 1;
        } catch (SQLException e) {
            Logger.getLogger(CoffeeDAO.class.getName()).log(Level.SEVERE, null, e);
        }
        return -1;
    }
    
//    public static void main(String[] args) {
//        CoffeeDAO coffeeDAO = new CoffeeDAO();
//        System.out.println("NextID:");
//        System.out.println(coffeeDAO.getNextID("coffeetype"));
//    }
    
    public void createOrder(CoffeeOrder order, List<CoffeeOrderItem> orderItems){
        long orderID = getNextID("coffeeorder");
        order.setId(orderID);
        String formattedDate = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(order.getOrderDate());
        final String sql = String.format(Locale.US,
            "INSERT INTO coffeeorder (id, order_date, name, delivery_address, cost)\n"
            + "VALUES (%d, '%s', '%s', '%s', %f)",
            orderID, formattedDate, order.getName(), order.getDeliveryAddress(), order.getTotalCost());
        Connection connection = DBConnectionManager.getInstance().getConnection();
        
        try{
            try (Statement statement = connection.createStatement();) 
            {
                connection.setAutoCommit(false);
                if (statement.executeUpdate(sql) > 0);
                {
                    long orderItemID = getNextID("coffeeorderitem");
                    for(CoffeeOrderItem orderItem: orderItems){
                        createOrderItem(orderItem, statement, orderItemID++);
                    }
                    connection.commit();
                }
            } catch (SQLException e) {
                Logger.getLogger(CoffeeDAO.class.getName()).log(Level.SEVERE, null, e);
                connection.rollback();
            }
            finally{
                connection.close();
            }
        }catch(SQLException e){
            Logger.getLogger(CoffeeDAO.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    private void createOrderItem(CoffeeOrderItem orderItem, Statement statement, long orderItemID) throws SQLException{
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
