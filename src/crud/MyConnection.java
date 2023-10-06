package crud;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Administrator
 */
public class MyConnection {
    public Connection getConection(){
        try{
            Class.forName("com.mysql.jdbc.Driver");
            String URL="jdbc:mysql://localhost/facial_recognition?user=root&password=";
            Connection con= DriverManager.getConnection(URL);
            return con;
        }
        catch(ClassNotFoundException | SQLException ex){
            JOptionPane.showMessageDialog(null, ex.toString(),"Loi",JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    
}
