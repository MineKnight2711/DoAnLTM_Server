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
                        handleCreate(receivedJson, out);
                        break;
                    case "update":
                        handleUpdate(receivedJson, out);
                        break;
                    case "regconition":
                        handleRegconition(data, out);
                        break;    
                    case "get-account":
                        handleGetAccount(receivedJson, out);
                        break; 
                    case "load-image":
                        handleLoadImage(receivedJson, out);
                        break;    
                    case "delete-image":
                        handleDeleteImage(receivedJson, out);
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
    private void handleCreate(OperationJson data, PrintWriter out) {
        try {
            System.out.println("Account nhan"+data.getData().toString());
            String decryptAccount=aes.decrypt(data.getData().toString(), aes.getPrivateKey());
            OperationJson responseCreate = accountThreadHandle.createNewUser(decryptAccount);
            if(responseCreate.getOperation().equals("Success")){
                String encryptUpdatedAccount=aes.encrypt(responseCreate.getData().toString(), aes.getPublicKeyFromString(data.getPublicKey()));
                 System.out.println("Account ma hoa"+encryptUpdatedAccount);
                responseCreate.setData(encryptUpdatedAccount);
                out.println(gson.toJson(responseCreate));
            }
            else{
                out.println(gson.toJson(responseCreate));
            }
        } catch (Exception ex) {
            System.out.println("Error"+ex.toString());
        }
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
        } else {
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

    private void handleLoadImage(OperationJson request, PrintWriter out) {
        
        try {
            String accountIDDecrypt=aes.decrypt(request.getData().toString(), aes.getPrivateKey());
            OperationJson resultJson = imageThreadHandle.loadImage(accountIDDecrypt);
            if(resultJson.getOperation().equals("Success"))
            {
                PublicKey publicKey = aes.getPublicKeyFromString(request.getPublicKey());
                String encryptListImage=aes.encrypt(resultJson.getData().toString(), publicKey);
                System.out.println("List anh ma hoa::::"+encryptListImage);
                resultJson.setData(encryptListImage);
                out.println(gson.toJson(resultJson));
            }
            else{
                out.println(gson.toJson(resultJson));
            }
            
        } catch (Exception ex) {
            System.out.println("Error"+ex.toString());
        }
    }
    private void handleDeleteImage(OperationJson request, PrintWriter out) {
        try {
            String decryptImageId=aes.decrypt(request.getData().toString(), aes.getPrivateKey());
            OperationJson resultDeleteJson = imageThreadHandle.deleteImage(decryptImageId);
            out.println(gson.toJson(resultDeleteJson));
        } catch (Exception ex) {
            System.out.println("Error"+ex.toString());
        }
    }

    private void handleRegconition(String data, PrintWriter out) {
        byte[] imageReceived=gson.fromJson(data, byte[].class);
        OperationJson result=imageThreadHandle.facialRecognition(imageReceived);
        out.println(EncodeDecode.encodeToBase64(gson.toJson(result)));
    }

    private void handleGetAccount(OperationJson data, PrintWriter out) {
        try {
            String decodePassword = aes.decrypt(data.getData().toString(), aes.getPrivateKey());
            OperationJson responseGetAccount = accountThreadHandle.getAccount(decodePassword);
            if(responseGetAccount.getOperation().equals("Success")){
                PublicKey publicKey = aes.getPublicKeyFromString(data.getPublicKey());
                String encryptAccount=aes.encrypt(responseGetAccount.getData().toString(), publicKey);
                responseGetAccount.setData(encryptAccount);
                out.println(gson.toJson(responseGetAccount));
            }
            else{
                out.println(gson.toJson(responseGetAccount));
            }
        } catch (Exception ex) {
            System.out.println("Error"+ex.toString());
        }
    }
}