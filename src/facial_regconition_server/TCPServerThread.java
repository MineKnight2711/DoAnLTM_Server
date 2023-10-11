/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package facial_regconition_server;
import com.google.gson.Gson;
import crud.AccountCRUD;
import crud.ImageCRUD;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;
import model.OperationJson;
import utils.EncodeDecode;


/**
 *
 * @author WitherDragon
 */
public class TCPServerThread extends Thread {
    private final Socket clientSocket;
    private final AccountThreadHandle accountThreadHandle;
    private final ImageThreadHandle imageThreadHandle;
    private final Gson gson;
    public TCPServerThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
        accountThreadHandle=new AccountThreadHandle();
        imageThreadHandle=new ImageThreadHandle();
        gson=new Gson();
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        ) {
            OperationJson receivedJson = gson.fromJson(in.readLine(), OperationJson.class);
            System.out.println("JSOn nhận được :"+receivedJson.getOperation());
            String operation=receivedJson.getOperation();
        if (!operation.isEmpty()) {
            String data="";
            if(receivedJson.getData()!=null)
            {
                data=receivedJson.getData().toString();
            }
            switch (operation) {
                    case "create":
                        handleCreate(data, out);
                        break;
                    case "update":
                        handleUpdate(data, out);
                        break;
                    default:
                        handleOtherOperations(operation, data, out);
                        break;
                }
        } else {
            out.println("InvalidJsonData");
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
    private void handleCreate(String data, PrintWriter out) {
        String responseCreate = accountThreadHandle.createNewUser(data);
        out.println(responseCreate);
    }

    private void handleUpdate(String data, PrintWriter out) {
        String decodedDataUpdate = EncodeDecode.decodeBase64FromJson(data);
        System.out.println("Dữ liệu mã hoá :" + data);
        System.out.println(decodedDataUpdate);
        String responseUpdate = accountThreadHandle.updateAccount(decodedDataUpdate);
        out.println(responseUpdate);
    }

    private void handleOtherOperations(String operation, String data, PrintWriter out) {
        String[] operationParts = operation.split("/");
        if (operationParts.length != 2) {
            out.println(EncodeDecode.encodeToBase64("Account not found"));
            return;
        }
        String pathVariables = operationParts[1];
        if (operation.startsWith("save-image/")) {
            handleSaveImage(pathVariables, data, out);
        } else if (operation.contains("login")) {
            handleLogin(pathVariables, data, out);
        } else if (operation.startsWith("change-password/")) {
            handleChangePassword(pathVariables, data, out);
        } else if (operation.startsWith("load-image/")) {
            handleLoadImage(pathVariables, out);
        } else if(operation.startsWith("delete-image/")){
            handleDeleteImage(pathVariables, out);
        }else {
            out.println(EncodeDecode.encodeToBase64("UnsupportedOperation"));
        }
    }

    private void handleSaveImage(String accountID, String data, PrintWriter out) {
        byte[] receiveImage = Base64.getDecoder().decode(data);
        String responseImage = imageThreadHandle.saveImage(accountID, receiveImage);
        out.println(responseImage);
    }

    private void handleLogin(String accountID, String data, PrintWriter out) {
        System.out.println("Tài khoản nhận :" + accountID + "\nMật khẩu nhận :" + data);
        String decodePassword = EncodeDecode.decodeBase64FromJson(data);
        String responseLogin = accountThreadHandle.login(accountID, decodePassword);
        out.println(responseLogin);
    }

    private void handleChangePassword(String accountID, String data, PrintWriter out) {
        System.out.println("Tài khoản nhận :" + accountID + "\nDữ liệu nhận :" + data);
        String result = accountThreadHandle.changePass(accountID, data);
        System.out.println("Ket qua tra ve :" + result);
        out.println(result);
    }

    private void handleLoadImage(String accountID, PrintWriter out) {
        System.out.println("Tài khoản nhận :" + accountID);
        OperationJson result = imageThreadHandle.loadImage(accountID);
        System.out.println("Ket qua tra ve :" + result.getData().toString());
        out.println(gson.toJson(result));
    }
    private void handleDeleteImage(String imageID, PrintWriter out) {
        System.out.println("ID ảnh :" + imageID);
        String result = imageThreadHandle.deleteImage(imageID);
        System.out.println("Ket qua tra ve :" + result);
        out.println(result);
    }
}