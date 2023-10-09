/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package facial_regconition_server;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import crud.AccountCRUD;
import crud.ImageCRUD;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;
import model.Account;
import model.OperationJson;
import utils.EncodeDecode;


/**
 *
 * @author WitherDragon
 */
public class TCPServerThread extends Thread {
    private final Socket clientSocket;
    private final AccountCRUD accountCRUD;
    private final ImageCRUD imageCRUD;
    private final Gson gson;
    public TCPServerThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
        accountCRUD =new AccountCRUD();
        imageCRUD=new ImageCRUD();
        gson=new Gson();
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        ) {
            OperationJson receivedJson = gson.fromJson(in.readLine(), OperationJson.class);

            if (receivedJson != null && receivedJson.getOperation() != null) {
                String operation = receivedJson.getOperation();
                String data = receivedJson.getData().toString();
                switch (operation) {
                    case "create":
                        String responseCreate = createNewUser(data);
                        out.println(responseCreate);
                        break;
                    case "update":
                        String decodedDataUpdate = EncodeDecode.decodeBase64FromJson(data);
                        System.out.println("Dữ liệu mã hoá :" + data);
                        System.out.println(decodedDataUpdate);
                        String responseUpdate = updateAccount(decodedDataUpdate);
                        out.println(responseUpdate);
                        break;
                    default:
                        if (operation.startsWith("save-image/")) {
                            String[] operationParts = operation.split("/");
                            if (operationParts.length == 2) {
                                String accountID = operationParts[1];
                                byte[] receiveImage = Base64.getDecoder().decode(data);
                                String responseImage = saveImage(accountID, receiveImage);
                                out.println(responseImage);
                            } else {
                                out.println("Account not found");
                            }
                        } else if (operation.contains("login")) {
                            String[] operationParts = operation.split("/");
                            if (operationParts.length == 2) {
                                System.out.println("Tài khoản nhận :" + operationParts[1] + "\nMật khẩu nhận :" + data);
                                String account = operationParts[1];
                                String decodePassword = EncodeDecode.decodeBase64FromJson(data);
                                String responseLogin = login(account, decodePassword);
                                out.println(responseLogin);
                            } else {
                                out.println("Account not found");
                            }
                        } else {
                            //Sai operation
                            out.println("Unsupported operation");
                        }
                        break;
                }
            } else {
                // Dữ liệu JSOn không hợp lệ
                out.println("Invalid JSON data");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
  
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private String saveImage(String accountID,byte[] image){
        imageCRUD.saveImage(accountID, image);
        return "Success";
    }
    private String login(String account,String password){
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
    private String createNewUser(String data){
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

    private String updateAccount(String account) {
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
}