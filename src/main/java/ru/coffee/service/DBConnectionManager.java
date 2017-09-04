package ru.coffee.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DBConnectionManager {
    
    private String driver;
    private String dbURL;
    private String user;
    private String password;
    
    private DBConnectionManager() {
    }
    
    private static class DBConnectionManagerHolder {
        private static final DBConnectionManager INSTANCE = new DBConnectionManager();
    }

    public static DBConnectionManager getInstance() {
        return DBConnectionManagerHolder.INSTANCE;
    }
    
    public Connection getConnection() throws SQLException{
        Connection result = null;
        try {
            loadDBProperties();
            Class.forName(driver);
            result = DriverManager.getConnection(dbURL, user, password);
        } catch (ClassNotFoundException e) {
            throw new SQLException(e);
        }
        return result;
    }

    private void loadDBProperties() throws SQLException{
        ResourceBundle dbBundle = ResourceBundle.getBundle("db");
        driver = dbBundle.getString("db.driver");
        dbURL = dbBundle.getString("db.url");
        user = dbBundle.getString("db.user");
        password = dbBundle.getString("db.password");      
    }
    
//    public static void main(String[] args) {
//        DBConnectionManager manager = DBConnectionManager.getInstance();
//        try {
//            Connection c1 = manager.getConnection();
//            Connection c2 = manager.getConnection();
//            
//            System.out.println("c1 = c2: " + (c1 == c2));
//            
//            System.out.println("c1.isValid: " + c1.isValid(0));
//            System.out.println("c2.isValid: " + c2.isValid(0));
//            
//            c1.close();
//            
//            System.out.println("c1.isValid: " + c1.isValid(0));
//            System.out.println("c2.isValid: " + c2.isValid(0));
//
//            c2.close();
//            
//            System.out.println("c1.isValid: " + c1.isValid(0));
//            System.out.println("c2.isValid: " + c2.isValid(0));        
//        } catch (SQLException ex) {
//            Logger.getLogger(DBConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
}
