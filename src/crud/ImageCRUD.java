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

/**
 *
 * @author WitherDragon
 */
public class ImageCRUD {
    private Connection con;
    private Statement stmt;
    public ImageCRUD(){
        try{
            MyConnection mycon=new MyConnection();
            con=mycon.getConection();
            stmt=con.createStatement();
        }
        catch(SQLException ex){
            JOptionPane.showMessageDialog(null, ex);
        }                    
    }
    public boolean saveImage(String userID,byte[] images){
        try{
            String query = "INSERT INTO user_image VALUES('', ? , ?)";
            try(PreparedStatement statement = con.prepareStatement(query)){                
                statement.setString(1, userID);
                statement.setBytes(2, images);
                statement.executeUpdate();
                return true;
            }
        }
        catch(SQLException ex ){
            JOptionPane.showMessageDialog(null, ex);
            return false;
        }
    }
}
