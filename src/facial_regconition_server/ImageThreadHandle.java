/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package facial_regconition_server;

import com.google.gson.Gson;
import crud.AccountCRUD;
import crud.ImageCRUD;
import java.util.List;
import model.Account;
import model.OperationJson;
import model.UserImages;
import utils.EncodeDecode;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

/**
 *
 * @author Administrator
 */
public class ImageThreadHandle {
    private final ImageCRUD imageCRUD;
    private final Gson gson;
    private AccountCRUD acc;

    public ImageThreadHandle() {
        this.imageCRUD = new ImageCRUD();
        gson = new Gson();
        acc = new AccountCRUD();
    }
    
    
    public String saveImage(String accountID,List<byte[]> images){
        if(!accountID.isEmpty()){
            if(!images.isEmpty()){
                for(byte[] image : images)
                {
                    imageCRUD.saveImage(accountID, image);
                }
                return "Success";
            }
            return "EmptyList";
        }
        return "Fail";
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
    public OperationJson facialRecognition(byte[] imageCapture) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        byte[] dataFace = null;
        OperationJson resultJson=new OperationJson();
        // Check if a face is detected
        if (imageCapture != null) {  
            double max = 0;
            List<UserImages> allUserImages = imageCRUD.getAllUserImages();            
            // Compare the captured face with all user images
            for (UserImages userImage : allUserImages) {                
                // Convert the user image to a matrix
                byte[] image = userImage.getImages();
                // Compare the similarity of the captured face and user image
                double similarity = compareImages(imageCapture, image);                
                // Định mức so sánh
                double threshold = 0.88;
                if( max == 0){
                    max = similarity;
                    dataFace = userImage.getImages();
                }                    
                else if(similarity > max){
                    max = similarity;
                    Account account = acc.getUser(userImage.getID_User());
                    dataFace = userImage.getImages();
                    
                }                    
                // Check if the similarity is above the threshold
                if (similarity >= threshold) {
                    resultJson.setData(sendDetectDisplayToClient(imageCapture, image, similarity));
                    resultJson.setOperation("Detected");
                    return resultJson;
                }
            }      
            resultJson.setData(sendDetectDisplayToClient(imageCapture, dataFace, max));
            resultJson.setOperation("NotDetected");
            return resultJson;
        }
        resultJson.setOperation("NoFace");
        return resultJson;
    }
   
    public double compareImages(byte[] image1, byte[] image2) {
        // Load OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Convert image byte arrays to OpenCV Mat objects
        Mat mat1 = Imgcodecs.imdecode(new MatOfByte(image1), Imgcodecs.IMREAD_UNCHANGED);
        Mat mat2 = Imgcodecs.imdecode(new MatOfByte(image2), Imgcodecs.IMREAD_UNCHANGED);
       
        // Calculate the Mean Squared Error (MSE) as a similarity measure
        double mse = Core.norm(mat1, mat2, Core.NORM_L2) / (mat1.rows() * mat1.cols());

        // Convert the MSE to a similarity score (1 - MSE)
        double similarity = 1.0 - mse;

        return similarity;
    }
    public String sendDetectDisplayToClient(byte[] image1, byte[] image2, double simularity) {         
        String encodeImage1 = gson.toJson(image1);
        String encodeImage2 = gson.toJson(image2);
        String encodeSimularity=gson.toJson(simularity);
        return encodeImage1+"@"+encodeImage2+"@"+encodeSimularity;
    }
}
