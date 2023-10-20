/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package facial_regconition_server;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.PublicKey;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.OperationJson;
import utils.EncodeDecode;
import utils.AES;


/**
 *
 * @author WitherDragon
 */
public class TCPServerThread extends Thread {
    private final Socket clientSocket;
    private final AccountThreadHandle accountThreadHandle;
    private final ImageThreadHandle imageThreadHandle;
    private final Gson gson;
    private final AES aes;
    public TCPServerThread(Socket clientSocket,AES aes) {
        this.clientSocket = clientSocket;
        accountThreadHandle=new AccountThreadHandle();
        imageThreadHandle=new ImageThreadHandle();
        gson=new Gson();
        this.aes=aes;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        ) {
            OperationJson receivedJson = gson.fromJson(in.readLine(), OperationJson.class);
            String operation=receivedJson.getOperation();
        if (!operation.isEmpty()) {
            String data="";
            if(receivedJson.getData()!=null)
            {
                data=receivedJson.getData().toString();
            }
            switch (operation) {
                    case "GET_PUBLIC_KEY":
                        String publicKeyString = aes.encodePublicKey(aes.getPublicKey());
                        out.println(publicKeyString);  
                        break;
                    case "create":
                        handleCreate(data, out);
                        break;
                    case "update":
                        handleUpdate(receivedJson, out);
                        break;
                    case "regconition":
                        handleRegconition(data, out);
                        break;    
                    case "get-account":
                        handleGetAccount(data, out);
                        break; 
                    default:
                        handleOtherOperations(receivedJson, out);
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

    private void handleUpdate(OperationJson data, PrintWriter out) {
        
        try {
            String decodeAccount = aes.decrypt(data.getData().toString(), aes.getPrivateKey());
            OperationJson responseUpdateJson = accountThreadHandle.updateAccount(decodeAccount);
            if(responseUpdateJson.getOperation().equals("Success")){
                String encryptUpdatedAccount=aes.encrypt(responseUpdateJson.getData().toString(), aes.getPublicKeyFromString(data.getPublicKey()));
                responseUpdateJson.setData(encryptUpdatedAccount);
                out.println(gson.toJson(responseUpdateJson));
            }
            else{
                out.println(gson.toJson(responseUpdateJson));
            }
        } catch (Exception ex) {
            System.out.println("Error"+ex.toString());
        }
    }

    private void handleOtherOperations(OperationJson request, PrintWriter out) {
        String[] operationParts = request.getOperation().split("/");
        if (operationParts.length != 2) {
            out.println(EncodeDecode.encodeToBase64("Account not found"));
            return;
        }
        String pathVariables = operationParts[1];
        if (request.getOperation().startsWith("save-image/")) {
            handleSaveImage(pathVariables, request, out);
        } else if (request.getOperation().startsWith("login/")) {
            handleLogin(pathVariables, request, out);
        } else if (request.getOperation().startsWith("change-password/")) {
            handleChangePassword(pathVariables, request, out);
        } else if (request.getOperation().startsWith("load-image/")) {
            handleLoadImage(pathVariables, out);
        } else if(request.getOperation().startsWith("delete-image/")){
            handleDeleteImage(pathVariables, out);
        }else {
            out.println(EncodeDecode.encodeToBase64("UnsupportedOperation"));
        }
    }

    private void handleSaveImage(String accountID, OperationJson data, PrintWriter out) {
        List<byte[]> receiveImages = gson.fromJson(data.getData().toString(),new TypeToken<List<byte[]>>() {}.getType());
        String responseImage = imageThreadHandle.saveImage(accountID, receiveImages);
        out.println(EncodeDecode.encodeToBase64(responseImage));
    }

    private void handleLogin(String accountID, OperationJson data, PrintWriter out) {
        try {
            
            String decodePassword = aes.decrypt(data.getData().toString(), aes.getPrivateKey());
            OperationJson responseLogin = accountThreadHandle.login(accountID, decodePassword);
            if(responseLogin.getOperation().equals("Success")){
                PublicKey publicKey = aes.getPublicKeyFromString(data.getPublicKey());
                String encryptAccount=aes.encrypt(responseLogin.getData().toString(), publicKey);
                responseLogin.setData(encryptAccount);
                out.println(gson.toJson(responseLogin));
            }
            else{
                out.println(gson.toJson(responseLogin));
            }
        } catch (Exception ex) {
            System.out.println("Error"+ex.toString());
        }
    }

    private void handleChangePassword(String accountID, OperationJson data, PrintWriter out) {
        try {
            String decodePassword = aes.decrypt(data.getData().toString(), aes.getPrivateKey());
            OperationJson resultChangePassJson = accountThreadHandle.changePass(accountID, decodePassword);
            out.println(gson.toJson(resultChangePassJson));
        } catch (Exception ex) {
            System.out.println("Error"+ex.toString());
        }
    }

    private void handleLoadImage(String accountID, PrintWriter out) {
        OperationJson result = imageThreadHandle.loadImage(accountID);
        out.println(gson.toJson(result));
    }
    private void handleDeleteImage(String imageID, PrintWriter out) {
        System.out.println("ID ảnh :" + imageID);
        String result = imageThreadHandle.deleteImage(imageID);
        System.out.println("Ket qua tra ve :" + result);
        out.println(result);
    }

    private void handleRegconition(String data, PrintWriter out) {
        byte[] imageReceived=gson.fromJson(data, byte[].class);
        OperationJson result=imageThreadHandle.facialRecognition(imageReceived);
        out.println(EncodeDecode.encodeToBase64(gson.toJson(result)));
    }

    private void handleGetAccount(String data, PrintWriter out) {
        String result = accountThreadHandle.getAccount(data);
        out.println(EncodeDecode.encodeToBase64(result));
    }
}