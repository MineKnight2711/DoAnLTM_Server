/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package facial_regconition_server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import crud.AccountCRUD;
import model.Account;
import model.OperationJson;
import utils.EncodeDecode;

/**
 *
 * @author Administrator
 */
public class AccountThreadHandle {
    private final Gson gson;
    private final AccountCRUD accountCRUD;

    public AccountThreadHandle() {
        this.gson = new Gson();
        this.accountCRUD = new AccountCRUD();
    }
    
    public String login(String account,String password){
        String loginResult = accountCRUD.login(account, password);
        OperationJson sendJson=new OperationJson();

        switch (loginResult) {
            case "Success":
                Account acc=accountCRUD.getAccount(account);
                sendJson.setOperation("Success");
                String encodedAccount=EncodeDecode.encodeToBase64(gson.toJson(acc));
                sendJson.setData(encodedAccount);
                break;
            case "WrongPass":
                sendJson.setOperation("WrongPass");
//                encodedResult = EncodeDecode.encodeToBase64("WrongPass");
                break;
            case "AccountNotFound":
                sendJson.setOperation("AccountNotFound");
//                encodedResult = EncodeDecode.encodeToBase64("AccountNotFound");
                break;
            default:
                sendJson.setOperation("Unknown");
//                encodedResult = EncodeDecode.encodeToBase64("Unknown");
                break;
        }

        return gson.toJson(sendJson);   
    }
    public String createNewUser(String data){
        try {
            System.out.println(data);
            Account receivedAccount=gson.fromJson(data, Account.class);
            java.sql.Date birthday = new java.sql.Date(receivedAccount.getBrithday().getTime());
            receivedAccount.setBrithday(birthday);
            if(accountCRUD.createNewAccount(receivedAccount))
            {
                return "Success";
            }
            else
            {
                return "CreateAccountFail";
            }
        } catch (JsonSyntaxException e) {
            System.out.println("Lỗi định dạng ngày"+e.toString());
            return "DateTimeFormat";
        }
    }

    public String updateAccount(String account) {
        try {
//            System.out.println(data);
            Account receivedAccount=gson.fromJson(account, Account.class);
            java.sql.Date birthday = new java.sql.Date(receivedAccount.getBrithday().getTime());
            receivedAccount.setBrithday(birthday);
            if(accountCRUD.updateInfo(receivedAccount))
            {
                return EncodeDecode.encodeToBase64("Success");
            }
            else
            {
                return EncodeDecode.encodeToBase64("UpdateFail");
            }
        } catch (JsonSyntaxException e) {
            return EncodeDecode.encodeToBase64("DateTimeFormat");
        }
    }

    public String changePass(String account, String encodedPassword) {
        String decodePassword = EncodeDecode.decodeBase64FromJson(encodedPassword);
        String[] oldAndNewpass = decodePassword.split("-");
        System.out.println("Mật khẩu cũ :"+oldAndNewpass[0]+"Mật khẩu mới :"+oldAndNewpass[1]);
        String result;
        if(oldAndNewpass.length==2){
            result=EncodeDecode.encodeToBase64(accountCRUD.changePassword(account, oldAndNewpass[0], oldAndNewpass[1]));
            return result;
        }
        else{
            return EncodeDecode.encodeToBase64("WrongOldOrNewPass"); 
        }
    }
}
