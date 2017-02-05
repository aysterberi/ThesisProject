/*
 * Modified from https://github.com/bytedeco
 * to allow use without the args array
 */

package se.su.thesis;

import org.bytedeco.javacpp.opencv_core.Mat;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.IntBuffer;
import java.util.HashMap;

import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_core.MatVector;
import static org.bytedeco.javacpp.opencv_face.FaceRecognizer;
import static org.bytedeco.javacpp.opencv_face.createEigenFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;

public class Recognizer {
    private int predictedLabel;
    private HashMap<Integer, String> personsMap;



    public Recognizer() {

    }

// This is code for liveStream recognition
//    public void recognize(String pathToTrainingDirectory, BufferedImage pathToTestImage) {
//        BufferedImage test = TakePicture.cropImage(pathToTestImage);
//        Mat testImage = new Mat(test.getHeight(),test.getWidth(), CV_LOAD_IMAGE_GRAYSCALE);


    public void recognize(String pathToTrainingDirectory, String pathToTestImage) {
        personsMap = new HashMap<>();
        File[] directories = Controller.getExistingPersons();
        Mat testImage = imread(pathToTestImage, CV_LOAD_IMAGE_GRAYSCALE);
        FilenameFilter imageFilter = (dir, name) -> {
            name = name.toLowerCase();
            return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
        };
        int result = calculateFileLenght(directories, imageFilter);
        MatVector images = new MatVector(result);
        Mat labels = new Mat(result, 1, CV_32SC1);
        int counter = 0;
        for (File dirs : directories) {
            System.out.println("Going through " + dirs.getName() + " folder");
            File root = new File(dirs.getAbsolutePath());
//            File root = new File(pathToTrainingDirectory);
            File[] imageFiles = root.listFiles(imageFilter);
            IntBuffer labelsBuffer = labels.createBuffer();

            for (File f : imageFiles) {
                Mat image = imread(f.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
                int label = Integer.parseInt(f.getName().split("\\-")[0]);
                images.put(counter, image);
                labelsBuffer.put(counter, label);
                counter++;
                personsMap.put(label, f.getName());
            }

        }
            FaceRecognizer faceRecognizer = createEigenFaceRecognizer(10,500.0);
            faceRecognizer.train(images, labels);
            predictedLabel = faceRecognizer.predict(testImage);
    }

    public int getPredictedLabel() {
        return predictedLabel;
    }

    public String getNameOfPredictedPerson(){
        if (predictedLabel == 0){
            return "unknown";
        }
        String personName = personsMap.get(predictedLabel);
        String[] personNameSplit = personName.split("\\-");
        return personNameSplit[1].substring(0,personNameSplit[1].length() - 6);
    }

    public int calculateFileLenght (File[] directories, FilenameFilter imageFilter) {
        int size = 0;
        for (File dirs : directories) {
            File root = new File(dirs.getAbsolutePath());
            File[] imageFiles = root.listFiles(imageFilter);
            size += imageFiles.length;
        }
        return size;
    }
}
