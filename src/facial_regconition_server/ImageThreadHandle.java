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
    
    
    public OperationJson saveImage(String accountID,List<byte[]> images){
        OperationJson resultJson=new OperationJson();
        if(!accountID.isEmpty()){
            if(!images.isEmpty()){
                for(byte[] image : images)
                {
                    imageCRUD.saveImage(accountID, image);
                }
                resultJson.setOperation("Success");
                return resultJson;
            }
            resultJson.setOperation("EmptyList");
            return resultJson;
        }
        resultJson.setOperation("AccountNotFound");
        return resultJson;
    }
    

    public OperationJson loadImage(String accountID) {
        List<UserImages> imagesList=imageCRUD.getUserImage(accountID);
        OperationJson sendListToClientJson=new OperationJson();
        if(!imagesList.isEmpty())
        {
            sendListToClientJson.setOperation("Success");
            String encodeListToJson=gson.toJson(imagesList);
            sendListToClientJson.setData(encodeListToJson);
            return sendListToClientJson;
        }
        sendListToClientJson.setOperation("NoImage");
        return sendListToClientJson;
    }
    public OperationJson deleteImage(String imageID) {
        OperationJson resultJson=new OperationJson();
        if(imageCRUD.deleteUserImage(imageID))
        {
            resultJson.setOperation("Success");
            return resultJson;
        }
        resultJson.setOperation("CannotDeleteImage");
        return resultJson;
    }
    public OperationJson facialRecognition(byte[] imageCapture) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        byte[] faceDetected = null;
        OperationJson resultJson=new OperationJson();
        UserImages accountImage=new UserImages();
        // Kiểm tra khuôn mặt có phát hiện hay không
        if (imageCapture != null) {  
            double maxSimilarity = 0;
            // Định mức so sánh
            double threshold = 0.88;
            List<UserImages> allUserImages = imageCRUD.getAllUserImages();            
            // Tạo vòng lặp để so sánh với tất cả khuôn mặt hiên có 
            for (UserImages userImage : allUserImages) {
                double similarity = compareImages(imageCapture, userImage.getImages());               
                if(maxSimilarity == 0){
                    maxSimilarity = similarity;
                    faceDetected = userImage.getImages();
                    if (similarity >= threshold) {                        
                        accountImage=userImage;
                    }
                    continue;                    
                }
                // kiểm tra liên tục xem similarity hiện tiện có lớn hơn maxSimilarity hay không
                if (similarity > maxSimilarity) {
                    // Update maxSimilarity and set the corresponding face
                    maxSimilarity = similarity;
                    faceDetected = userImage.getImages();
                    if (similarity >= threshold) {
                        //Gán khuôn mặt giống nhất                        
                        accountImage=userImage;
                    } 
                }
            }
            if(maxSimilarity >= threshold){
                resultJson.setData(sendDetectDisplayToClient(imageCapture, faceDetected, maxSimilarity,accountImage.getID_User()));
                resultJson.setOperation("Detected");
            }
            if(maxSimilarity < threshold){
                resultJson.setData(sendDetectDisplayToClient(imageCapture, faceDetected, maxSimilarity,"NotFound"));
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
