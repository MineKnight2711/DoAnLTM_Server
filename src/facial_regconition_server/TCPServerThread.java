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
import java.util.List;
import java.util.Scanner;
import model.Account;
import model.OperationJson;


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
                if (receivedJson.getOperation().equals("create")) {
                    String receiveAccount = receivedJson.getData().toString();
                    String response = createNewUser(receiveAccount);
                    out.println(response);
                } else if (receivedJson.getOperation().startsWith("save-image/")) {
                    String[] operationParts = receivedJson.getOperation().split("/");
                    if (operationParts.length == 2) {
                        String imageType = operationParts[1]; 
                        byte[] receiveImage = Base64.getDecoder().decode(receivedJson.getData().toString());
                        String response = saveImage(imageType, receiveImage);
                        out.println(response);
                    } else {
                        // Handle invalid save-image operation
                        out.println("Invalid save-image operation");
                    }
                } else {
                    // Handle unsupported operation
                    out.println("Unsupported operation");
                }
            } else {
                // Handle invalid or incomplete JSON data
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
}