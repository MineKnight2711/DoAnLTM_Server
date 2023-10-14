/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package facial_regconition_server;

import com.google.gson.Gson;
import crud.AccountCRUD;
import crud.ImageCRUD;
import java.util.List;
import model.OperationJson;
import model.UserImages;
import utils.EncodeDecode;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

/**
 *
 * @author Administrator
 */
public class ImageThreadHandle {
    private final ImageCRUD imageCRUD;
    private final Gson gson;

    public ImageThreadHandle() {
        this.imageCRUD = new ImageCRUD();
        gson = new Gson();
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
        byte[] faceUndetected = null;
        byte[] faceDetected = null;
        OperationJson resultJson=new OperationJson();
        UserImages accountImage=new UserImages();
        // Check if a face is detected
        if (imageCapture != null) {  
            double maxSimilarity = 0;
            // Định mức so sánh
            double threshold = 0.88;
            List<UserImages> allUserImages = imageCRUD.getAllUserImages();            
            // Tạo vòng lặp để so sánh với tất cả khuôn mặt hiên có 
            for (UserImages userImage : allUserImages) {
//                // Convert the user image to a matrix
//                byte[] image = userImage.getImages();

                // Compare the similarity of the captured face and user image
                double similarity = compareImages(imageCapture, userImage.getImages());

                // Check if similarity is greater than the current maxSimilarity
                if (similarity > maxSimilarity) {
                    // Update maxSimilarity and set the corresponding face
                    maxSimilarity = similarity;
                    if (similarity >= threshold) {
                        //Gán khuôn mặt giống nhất
                        faceDetected = userImage.getImages();
                        accountImage=userImage;
                    } else {
                        //Gán mặt giống nhất nhưng chưa phải mặt của người dùng
                        faceUndetected = userImage.getImages();
                    }
                }

                // Check if the similarity is above the threshold
                if (similarity >= threshold) {
                    resultJson.setData(sendDetectDisplayToClient(imageCapture, userImage.getImages(), similarity,userImage.getID_User()));
                    resultJson.setOperation("Detected");
                    return resultJson;
                }
            }
            // Set the appropriate face based on maxSimilarity
            if (maxSimilarity >= threshold) {
                resultJson.setData(sendDetectDisplayToClient(imageCapture, faceDetected, maxSimilarity,accountImage.getID_User()));
                resultJson.setOperation("Detected");
            } else {
                resultJson.setData(sendDetectDisplayToClient(imageCapture, faceUndetected, maxSimilarity,"NotFound"));
                resultJson.setOperation("NotDetected");
            }
            return resultJson;
        }
        resultJson.setOperation("NoFace");
        return resultJson;
    }
   
    public double compareImages(byte[] image1, byte[] image2) {
        // Load thư viện OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        //Chuyển đổi hình ảnh dạng byte[] sang đối tượng OpenCV Mat
        Mat mat1 = Imgcodecs.imdecode(new MatOfByte(image1), Imgcodecs.IMREAD_UNCHANGED);
        Mat mat2 = Imgcodecs.imdecode(new MatOfByte(image2), Imgcodecs.IMREAD_UNCHANGED);
       
        // Tính toán the sai số bình phương trung bình (MSE) dưới dạng tỉ lệ giống nhau
        //Trong OpenCV, Sai số bình phương trung bình (MSE) là một thước đo thường được sử dụng để đo chất lượng 
        //của việc nén hình ảnh hoặc video hoặc độ chính xác của một thuật toán xử lý hình ảnh.
        double mse = Core.norm(mat1, mat2, Core.NORM_L2) / (mat1.rows() * mat1.cols());

        //Chuyển đổi MSE thành tỉ lệ giống nhau
        double similarity = 1.0 - mse;

        return similarity;
    }
    public String sendDetectDisplayToClient(byte[] image1, byte[] image2, double simularity,String accountId) {         
        if(!accountId.equals("NotFound")){
            String encodeImage1=gson.toJson(image1);
            String encodeImage2=gson.toJson(image2);
            String encodeSimularity=gson.toJson(simularity);
            String accountID=accountId;
            return encodeImage1+"@"+encodeImage2+"@"+encodeSimularity+"@"+accountID;
        }
        String encodeImage1=gson.toJson(image1);
        String encodeImage2=gson.toJson(image2);
        String encodeSimularity=gson.toJson(simularity);
        return encodeImage1+"@"+encodeImage2+"@"+encodeSimularity;
    }
}
