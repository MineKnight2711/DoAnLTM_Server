/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 *
 * @author WitherDragon
 */
public class EncodeDecode {
    public static String decodeBase64FromJson(String json){
        String base64EncodedData = json;
        byte[] decodedData = Base64.getDecoder().decode(base64EncodedData);
        return new String(decodedData, StandardCharsets.UTF_8);
    }
    public static String encodeToBase64(String data){
        return Base64.getEncoder().encodeToString(data.getBytes());
    }
}
