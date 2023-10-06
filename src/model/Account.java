/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

/**
 *
 * @author dell
 */
public class Account {
    @SerializedName("ID_User") 
    private String ID_User;
    
    private String Account;
    
    private String Password;
    
    private String First_Name;
    
    private String Last_Name;
    
    private Date Brithday;
    
    private String Gender;
    
    private String Phone;
    
    private String Address;
    
    private String Email;

    public String getID_User() {
        return ID_User;
    }
    
    public void setID_User(String ID_User) {
        this.ID_User = ID_User;
    }

    public String getAccount() {
        return Account;
    }

    public void setAccount(String Account) {
        this.Account = Account;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String Password) {
        this.Password = Password;
    }

    public String getFrist_Name() {
        return First_Name;
    }

    public void setFrist_Name(String First_Name) {
        this.First_Name = First_Name;
    }

    public String getLast_Name() {
        return Last_Name;
    }

    public void setLast_Name(String Last_Name) {
        this.Last_Name = Last_Name;
    }

    public Date getBrithday() {
        return Brithday;
    }

    public void setBrithday(Date Brithday) {
        this.Brithday = Brithday;
    }

    public String getGender() {
        return Gender;
    }

    public void setGender(String Gender) {
        this.Gender = Gender;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String Phone) {
        this.Phone = Phone;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String Address) {
        this.Address = Address;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String Email) {
        this.Email = Email;
    }
}
