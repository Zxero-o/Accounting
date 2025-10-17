/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao; 

/**
 *
 * @author marionne pascual
 */
import java.sql.*;

public class ConnectionDB {
    private static final String URL = "jdbc:mysql://localhost:3306/accounting_titles";
    private static final String USER = "root";  
    private static final String PASSWORD = "Bjjmye112205";  

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
