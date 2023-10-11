/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package crud;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import model.UserImages;

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
    public ResultSet Query(String srt){
         try{
             ResultSet rs=stmt.executeQuery(srt);
             return rs;
         }catch (SQLException ex) {
             JOptionPane.showMessageDialog(null, ex);
            return null ;
        }
    }
    public boolean deleteUserImage(String idImage){
        String query = "DELETE FROM user_image WHERE ID_Image = ?";
        try(PreparedStatement statement = con.prepareStatement(query)){
            statement.setString(1, idImage);
            statement.executeUpdate();
            return true;
        }
        catch(SQLException ex){
            JOptionPane.showConfirmDialog(null, ex);
            return false;
        }
    }
    
    public List<UserImages> getUserImage(String userID) {
        List<UserImages> userImages = new ArrayList<>();
        String query = String.format("SELECT * FROM user_image WHERE ID_USER = '%s'", userID);
        ResultSet rs = Query(query);
        try{
            while (rs.next()){
                UserImages user = new UserImages();
                user.setID_Image(rs.getString("ID_Image"));
                user.setID_User(rs.getString("ID_User"));
                user.setImages(rs.getBytes("Image"));
                userImages.add(user);
            }
            return userImages;
        }
        catch(SQLException ex){
            JOptionPane.showMessageDialog(null, ex);
            return null ;
        }    
    }
}
