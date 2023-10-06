/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package crud;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;
import model.Account;

/**
 *
 * @author WitherDragon
 */
public class AccountCRUD {
    private Connection con;
    private Statement stmt;
    public AccountCRUD(){
        try{
            MyConnection mycon=new MyConnection();
            con=mycon.getConection();
            stmt=con.createStatement();
        }
        catch(SQLException ex){
            JOptionPane.showMessageDialog(null, ex);
        }                    
    }
    public boolean createNewAccount(Account acc){
        try{
            String query = String.format("INSERT INTO user VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')",
                                    acc.getID_User(), acc.getAccount(), acc.getPassword(), acc.getFrist_Name(), acc.getLast_Name(),
                                    acc.getBrithday(), acc.getGender(), acc.getPhone(), acc.getAddress(), acc.getEmail());
            stmt.executeUpdate(query);
            return true;
        }catch(SQLException ex){
            JOptionPane.showMessageDialog(null,ex);
            System.out.println(ex.toString());
            return false;
        }
    }
    
}
