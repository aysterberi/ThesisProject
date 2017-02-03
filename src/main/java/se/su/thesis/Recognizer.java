/*
 * Modified from https://github.com/bytedeco
 * to allow use without the args array
 */

package se.su.thesis;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_face.*;
import static org.bytedeco.javacpp.opencv_face.createFisherFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;

import org.bytedeco.javacpp.opencv_core.Mat;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.IntBuffer;

public class Recognizer {

    public Recognizer() {
    }

    public void recognize(String pathToTrainingDirectory, String pathToTestImage) {
        Mat testImage = imread(pathToTestImage, CV_LOAD_IMAGE_GRAYSCALE);
        File root = new File(pathToTrainingDirectory);

        FilenameFilter imageFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
            }
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

        FaceRecognizer faceRecognizer = createFisherFaceRecognizer();
//        FaceRecognizer faceRecognizer = createEigenFaceRecognizer();
//        FaceRecognizer faceRecognizer = createLBPHFaceRecognizer();

        faceRecognizer.train(images, labels);
        int predictedLabel = faceRecognizer.predict(testImage);
        System.err.println("Predicted label: " + predictedLabel);
    }
}