/*
 * Modified from https://github.com/bytedeco
 * to allow use without the args array
 */

package se.su.thesis;

import org.bytedeco.javacpp.opencv_core.Mat;

import java.awt.image.BufferedImage;
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

    public Recognizer() {
    }

    public void recognize(String pathToTrainingDirectory, BufferedImage pathToTestImage) {
//        Mat testImage = imread(pathToTestImage., CV_LOAD_IMAGE_GRAYSCALE);
        BufferedImage test = TakePicture.cropImage(pathToTestImage);
        Mat testImage = new Mat(test.getHeight(),test.getWidth(), CV_LOAD_IMAGE_GRAYSCALE);
//        pathToTestImage.
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
            labelsBuffer.put(counter, label);
            counter++;
        }

//        FaceRecognizer faceRecognizer = createFisherFaceRecognizer();
        FaceRecognizer faceRecognizer = createEigenFaceRecognizer();
//        FaceRecognizer faceRecognizer = createLBPHFaceRecognizer();

        faceRecognizer.train(images, labels);
        int predictedLabel = faceRecognizer.predict(testImage);

        System.err.println("Predicted label: " + predictedLabel);
    }
}