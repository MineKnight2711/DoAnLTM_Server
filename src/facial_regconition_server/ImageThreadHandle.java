/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package facial_regconition_server;

import com.google.gson.Gson;
import crud.AccountCRUD;
import crud.ImageCRUD;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
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

    public ImageThreadHandle() {
        this.imageCRUD = new ImageCRUD();
        gson=new Gson();
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
        byte[] faces = detctFace(imageCapture);
        OperationJson resultJson=new OperationJson();
        // Check if a face is detected
        if (faces != null) {  
            double min = 0;
            double max = 0;
            List<UserImages> allUserImages = imageCRUD.getAllUserImages();            
            // Compare the captured face with all user images
            for (UserImages userImage : allUserImages) {
                // Convert the user image to a matrix
                byte[] image = userImage.getImages();
                // Compare the similarity of the captured face and user image
                double similarity = compareImages(faces, image);
                resultJson.setData(sendDetectDisplayToClient(faces, image, similarity));
                // Định mức so sánh
                double threshold = 0.5; 
                if(min == 0)
                    min = similarity;
                else if( max == 0)
                    max = similarity;
                else if(similarity < min){
                    max = min;
                    min = similarity;
                }
                else if(similarity > max){
                    max = similarity;
                }
                // Check if the similarity is above the threshold
                if (max >= threshold) {
                    resultJson.setOperation("Detected");
                }
            }      
            resultJson.setOperation("NotDetected");
            return resultJson;
        }
        resultJson.setOperation("NoFace");
        return resultJson;
    }
    public byte[] detctFace(byte[] imageCapture) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        // Convert the byte[] imageData to a Mat object
        Mat frame = Imgcodecs.imdecode(new MatOfByte(imageCapture), Imgcodecs.IMREAD_COLOR);

        // Convert the frame to grayscale
        Mat grayFrame = new Mat();
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);

        // Load the face cascade classifier
        CascadeClassifier faceCascade = new CascadeClassifier("src\\PreTrainData\\haarcascade_frontalface_default.xml");
        // Detect faces in the grayscale frame
        MatOfRect faces = new MatOfRect();
        faceCascade.detectMultiScale(grayFrame, faces);     
        
        MatOfByte faceImageData = new MatOfByte();
        Imgcodecs.imencode(".jpg", grayFrame, faceImageData);
        imageCapture = faceImageData.toArray();
        // Check if a face is detected
        if (faces.toArray().length > 0) {

            // Encode the face image to JPEG
            Rect faceRect = faces.toArray()[0]; // Assuming only one face is detected

            // Crop the face region from the gray frame
            Mat faceImage = new Mat(grayFrame, faceRect); // Crop from the grayscale frame   
            Size resizedSize = new Size(256, 256); // Adjust the size as needed
            Imgproc.resize(faceImage, faceImage, resizedSize);
            // Encode the face image to JPEG
            Imgcodecs.imencode(".jpg", faceImage, faceImageData);
            imageCapture = faceImageData.toArray();            
            return imageCapture;
           
        }
        return null;
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
        String encodeImage1=gson.toJson(image1);
        String encodeImage2=gson.toJson(image2);
        String encodeSimularity=gson.toJson(simularity);
        return encodeImage1+"@"+encodeImage2+"@"+encodeSimularity;
    }
}
