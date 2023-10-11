/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package facial_regconition_server;

import com.google.gson.Gson;
import crud.ImageCRUD;
import java.util.List;
import model.OperationJson;
import model.UserImages;
import utils.EncodeDecode;

/**
 *
 * @author Administrator
 */
public class ImageThreadHandle {
    private final ImageCRUD imageCRUD;
    private final Gson gson;

    public ImageThreadHandle() {
        this.imageCRUD = new ImageCRUD();
        gson=new Gson();
    }
    
    
    public String saveImage(String accountID,byte[] image){
        imageCRUD.saveImage(accountID, image);
        return "Success";
    }
    

    public OperationJson loadImage(String accountID) {
        List<UserImages> imagesList=imageCRUD.getUserImage(accountID);
        OperationJson sendListToClientJson=new OperationJson();
        if(!imagesList.isEmpty())
        {
            sendListToClientJson.setOperation("Success");
            String encodeListToJson=gson.toJson(imagesList);
            String encodeListToBase64=EncodeDecode.encodeToBase64(encodeListToJson);
            
            sendListToClientJson.setData(encodeListToBase64);
            return sendListToClientJson;
        }
        sendListToClientJson.setOperation("NoImage");
        return sendListToClientJson;
    }
    public String deleteImage(String imageID) {
        if(imageCRUD.deleteUserImage(imageID))
        {
            return EncodeDecode.encodeToBase64("Success");
        }
        return EncodeDecode.encodeToBase64("Fail");
    }
}
