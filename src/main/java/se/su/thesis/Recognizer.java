/*
 * Modified from https://github.com/bytedeco
 * to allow use without the args array
 */

package se.su.thesis;

import org.bytedeco.javacpp.opencv_core.Mat;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.IntBuffer;

import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_core.MatVector;
import static org.bytedeco.javacpp.opencv_face.FaceRecognizer;
import static org.bytedeco.javacpp.opencv_face.createEigenFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;

public class Recognizer {
    private int predictedLabel;


    public Recognizer() {

    }

    public void recognize(String pathToTrainingDirectory, String pathToTestImage) {
//        String fname = null;
        Mat testImage = imread(pathToTestImage, CV_LOAD_IMAGE_GRAYSCALE);
//    public void recognize(String pathToTrainingDirectory, BufferedImage pathToTestImage) {
//        BufferedImage test = TakePicture.cropImage(pathToTestImage);
//        Mat testImage = new Mat(test.getHeight(),test.getWidth(), CV_LOAD_IMAGE_GRAYSCALE);
        File root = new File(pathToTrainingDirectory);

        FilenameFilter imageFilter = (dir, name) -> {
            name = name.toLowerCase();
            return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
        };

        File[] imageFiles = root.listFiles(imageFilter);
        MatVector images = new MatVector(imageFiles.length);
        Mat labels = new Mat(imageFiles.length, 1, CV_32SC1);
        IntBuffer labelsBuffer = labels.createBuffer();
        int counter = 0;

        for (File f : imageFiles) {
            Mat image = imread(f.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
            int label = Integer.parseInt(f.getName().split("\\-")[0]);
            images.put(counter, image);
//            fname = f.getName();
            labelsBuffer.put(counter, label);
            counter++;
        }

//        FaceRecognizer faceRecognizer = createFisherFaceRecognizer(0,0.0);
        FaceRecognizer faceRecognizer = createEigenFaceRecognizer(15, 5000.0);
//        FaceRecognizer faceRecognizer = createLBPHFaceRecognizer();
        faceRecognizer.train(images, labels);
        predictedLabel = faceRecognizer.predict(testImage);
//        if (predictedLabel != 1){
//            System.err.println("Predicted label: " + predictedLabel);
//        }else{
//            System.err.println("Predicted label: " + predictedLabel);
//        }
//        if (predictedLabel != 1){
//            testImage = imread("src/main/resources/test/Matt1.png", CV_LOAD_IMAGE_GRAYSCALE);
//            predictedLabel = faceRecognizer.predict(testImage);
//            System.err.println(fname + predictedLabel);          //        }
    }

    public int getPredictedLabel() {
        return predictedLabel;
    }
}
