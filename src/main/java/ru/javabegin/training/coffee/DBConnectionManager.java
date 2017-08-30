package ru.javabegin.training.coffee;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBConnectionManager {
    //TODO: move into external properties file
    private final String driver = "com.mysql.jdbc.Driver";
    private final String dbURL = "jdbc:mysql:///coffee";
    private final String user = "root";
    private final String pwd = "root";
    
    private DBConnectionManager() {
    }
    
    private static class DBConnectionManagerHolder {
        private static final DBConnectionManager INSTANCE = new DBConnectionManager();
    }

    public static DBConnectionManager getInstance() {
        return DBConnectionManagerHolder.INSTANCE;
    }
    
    public Connection getConnection() {
        Connection result = null;
        try {
            Class.forName(driver);
            result = DriverManager.getConnection(dbURL, user, pwd);
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(DBConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
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
