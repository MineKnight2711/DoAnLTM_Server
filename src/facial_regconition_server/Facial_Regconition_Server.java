/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package facial_regconition_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import utils.AES;
import utils.BaseURL;

/**
 *
 * @author WitherDragon
 */
public class Facial_Regconition_Server {

   public static void main(String[] args) {
        AES aes=new AES();
        try {
            ServerSocket serverSocket = new ServerSocket(BaseURL.PORT);

            while (true) {
                System.out.println("Server dang chay tren cong "+BaseURL.PORT+" ...");
                Socket clientSocket = serverSocket.accept();

                System.out.println("Client connected :"+clientSocket.getInetAddress().getHostName());

                Thread clientHandler = new TCPServerThread(clientSocket,aes);
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
