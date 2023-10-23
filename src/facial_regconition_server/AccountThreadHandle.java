/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package facial_regconition_server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
        this.gson = new GsonBuilder().setDateFormat("MMM d, yyyy").create();
        this.accountCRUD = new AccountCRUD();
    }
    
    public OperationJson login(String account,String password){
        String loginResult = accountCRUD.login(account, password);
        OperationJson sendJson = new OperationJson();

        switch (loginResult) {
            case "Success":
                Account acc=accountCRUD.getAccount(account);
                sendJson.setOperation("Success");
                sendJson.setData(gson.toJson(acc));
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

        return sendJson;   
    }
    public String createNewUser(String data){
        OperationJson sendJson=new OperationJson();
        try {
            System.out.println(data);
            String decodeRequest=EncodeDecode.decodeBase64FromJson(data);
            Account receivedAccount=gson.fromJson(decodeRequest, Account.class);
            java.sql.Date birthday = new java.sql.Date(receivedAccount.getBrithday().getTime());
            receivedAccount.setBrithday(birthday);
            if(accountCRUD.createNewAccount(receivedAccount))
            {
                Account getAccount=accountCRUD.getAccount(receivedAccount.getAccount());
                if(getAccount!=null){
                    sendJson.setOperation("Success");
                    sendJson.setData(gson.toJson(getAccount));
                    return EncodeDecode.encodeToBase64(gson.toJson(sendJson));
                }
                sendJson.setOperation("AccountNotFound");
                return EncodeDecode.encodeToBase64(gson.toJson(sendJson));
            }
            else
            {
                sendJson.setOperation("CreateAccountFail");
                return EncodeDecode.encodeToBase64(gson.toJson(sendJson));
            }
        } catch (JsonSyntaxException e) {
            System.out.println("Lỗi định dạng ngày"+e.toString());
            sendJson.setOperation("DateTimeFormat");
            return EncodeDecode.encodeToBase64(gson.toJson(sendJson));
        }
    }

    public OperationJson updateAccount(String account) {
        OperationJson resultUpdateJson=new OperationJson();
        try {
            
            Account receivedAccount=gson.fromJson(account, Account.class);
            java.sql.Date birthday = new java.sql.Date(receivedAccount.getBrithday().getTime());
            receivedAccount.setBrithday(birthday);
            if(accountCRUD.updateInfo(receivedAccount))
            {
                Account updatedAccount=accountCRUD.getUser(receivedAccount.getID_User());
                resultUpdateJson.setOperation("Success");
                resultUpdateJson.setData(gson.toJson(updatedAccount));
            }
            else
            {
                resultUpdateJson.setOperation("UpdateFail");
                
            }
        } catch (JsonSyntaxException e) {
            resultUpdateJson.setOperation("DateTimeFormat");
        }
        return resultUpdateJson;
    }

    public OperationJson changePass(String account, String encodedPassword) {
        OperationJson resultJson=new OperationJson();
        String decodePassword = encodedPassword;
        String[] oldAndNewpass = decodePassword.split("-");
        System.out.println("Mật khẩu cũ :"+oldAndNewpass[0]+"Mật khẩu mới :"+oldAndNewpass[1]);
        if(oldAndNewpass.length==2){
            String result =accountCRUD.changePassword(account, oldAndNewpass[0], oldAndNewpass[1]);
            if(result.equals("Success"))
            {
                resultJson.setOperation("Success");
            }
            else
            {
                resultJson.setOperation(result);
            }
        }
        else{
            resultJson.setOperation("MissingField");
        }
        return resultJson;
    }

    public String getAccount(String idUser) {
        Account acc=accountCRUD.getUser(idUser);
        OperationJson sendJson=new OperationJson();
        if(acc!=null){
            
            sendJson.setOperation("Success");
            sendJson.setData(EncodeDecode.encodeToBase64(gson.toJson(acc)));
            return gson.toJson(sendJson);
        }
        sendJson.setOperation("NotFound");
        return gson.toJson(sendJson);
    }
}
