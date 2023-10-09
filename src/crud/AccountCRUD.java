/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package crud;


import at.favre.lib.crypto.bcrypt.BCrypt;
import java.awt.HeadlessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;
import model.Account;
import model.OperationJson;

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
    public String login(String account,String password){
        String query = "SELECT Password FROM user WHERE Account = ?";
        try (PreparedStatement statement = con.prepareStatement(query)) {
            statement.setString(1, account);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String hashedPassword = resultSet.getString("Password");
                    BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), hashedPassword);
                    if(result.verified) {
                        return "Success";
                    } else {
                        return "WrongPass";
                    }
                } else {
                    return  "AccountNotFound";
                }
            }
        } catch (SQLException ex) {
            return "Unknown";
        }
    }
    public Account getAccount(String account) {
        String query = "SELECT * FROM user WHERE Account = ?";
        try (PreparedStatement statement = con.prepareStatement(query)) {
            statement.setString(1, account);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Account user = new Account();
                    user.setID_User(resultSet.getString("ID_User"));
                    user.setAccount(resultSet.getString("Account"));
                    user.setFrist_Name(resultSet.getString("First_Name"));
                    user.setLast_Name(resultSet.getString("Last_Name"));
                    user.setBrithday(resultSet.getDate("Brithday"));
                    user.setGender(resultSet.getString("Gender"));
                    user.setPhone(resultSet.getString("Phone"));
                    user.setAddress(resultSet.getString("Address"));
                    user.setEmail(resultSet.getString("Email"));

                    return user;
                }
            }
        } catch (SQLException ex) {
            return null;
        }
        return null; 
    }
    public boolean updateInfo(Account acc){
        try{
            String query = String.format("UPDATE user SET First_Name = '%s', Last_Name = '%s', Brithday = '%s', Gender = '%s', Phone = '%s', Address = '%s', Email = '%s' WHERE Account = '%s'",
                            acc.getFrist_Name(), acc.getLast_Name(), acc.getBrithday(), acc.getGender(), acc.getPhone(), acc.getAddress(),acc.getEmail(), acc.getAccount());
            stmt.executeUpdate(query);
//            JOptionPane.showMessageDialog(null, "Cập nhật thông tin thành công");
            return true;
        }catch (HeadlessException | SQLException ex) {
            JOptionPane.showMessageDialog(null, ex);
            return false;
        }
    }
}
