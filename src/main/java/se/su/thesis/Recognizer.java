/*
 * Modified from https://github.com/bytedeco
 * to allow use without the args array
 */

package se.su.thesis;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.opencv_core.Mat;

import java.io.*;
import java.nio.IntBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_core.MatVector;
import static org.bytedeco.javacpp.opencv_face.FaceRecognizer;
import static org.bytedeco.javacpp.opencv_face.createFisherFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static se.su.thesis.utils.Constants.*;

public class Recognizer {
    private double confidence;
    private int predictedLabel;
    private FaceRecognizer faceRecognizer;
    public static boolean dataChanged = true;
    private double confidencePercent;
    private double confidenceAverage;
    private FilenameFilter imageFilter;

    public Recognizer() {
//        faceRecognizer = createEigenFaceRecognizer(0, THRESH_TRIANGLE);
//        faceRecognizer = createEigenFaceRecognizer(2, 5000);
        faceRecognizer = createFisherFaceRecognizer(0, RECOGNIZER_THRESHOLD);
//        faceRecognizer = createEigenFaceRecognizer();
        this.trainOnPictures();
    }

    public void recognize(String filepath) {
        Mat testImage = imread(filepath, CV_LOAD_IMAGE_GRAYSCALE);
        if (dataChanged) {
            trainOnPictures();
            System.out.println("IF DATACHANGE TRUE");
        } else {
            if (new File(PERSON_SAVE_DATA).exists()) {
                System.err.println("loading data");
                faceRecognizer.load(PERSON_SAVE_DATA);
            } else {
                trainOnPictures();
            }
        }

        IntPointer intPointer = new IntPointer(1);
        DoublePointer doublePointer = new DoublePointer(1);
        intPointer.put(-1);
        doublePointer.put(0.0);
        faceRecognizer.predict(testImage, intPointer, doublePointer);
        predictedLabel = intPointer.get();
        confidence = doublePointer.get();
        confidencePercent = Math.floor(((-confidence + 1) / 100) + 100);

        System.out.println("predictedLAbel : " + predictedLabel);
        System.out.println("confidence : " + confidence);
        System.out.println("confidencePercent : " + confidencePercent);
    }

    private void trainOnPictures() {
        File[] directories = Controller.getExistingPersons();
        imageFilter = (dir, name) -> {
            name = name.toLowerCase();
            return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
        };
        int result = calculateFileLength(directories, imageFilter);
        if (result != 0) {
            MatVector images = new MatVector(result);
            Mat labels = new Mat(result, 1, CV_32SC1);
            int counter = 0;

            for (File dirs : directories) {
                System.err.println("Going through " + dirs.getName() + " folder");
                File root = new File(dirs.getAbsolutePath());
                File[] imageFiles = root.listFiles(imageFilter);
                IntBuffer labelsBuffer = labels.createBuffer();

                for (File f : imageFiles) {
                    Mat image = imread(f.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
                    int label = Integer.parseInt(f.getName().split("\\-")[0]);
                    images.put(counter, image);
                    labelsBuffer.put(counter, label);
                    counter++;
                }
            }
            faceRecognizer.train(images, labels);
            faceRecognizer.save(PERSON_SAVE_DATA);
            dataChanged = false;
        }
    }

    public int getPredictedLabel() {
        return predictedLabel;
    }

    public String getNameOfPredictedPerson() {
        String recognizedPersonName = getKeyFromValue(Controller.personLabelMap,
                predictedLabel).toString();
        if (predictedLabel != 0 && confidencePercent >= 80) {
            int fileLength = new File(PERSONS_DIRECTORY + recognizedPersonName).listFiles().length;
            System.out.println(fileLength);
            logPeoplePassing(recognizedPersonName);
            if (fileLength <= 35) {
                new TakePicture(DEFAULT_TEST_PERSON, recognizedPersonName);
            }
            return recognizedPersonName + " " + confidencePercent + "%";
        }
        logPeoplePassing("Unknown");
        return "Unknown";
    }

    private int calculateFileLength(File[] directories, FilenameFilter imageFilter) {
        int size = 0;
        for (File dirs : directories) {
            File root = new File(dirs.getAbsolutePath());
            File[] imageFiles = root.listFiles(imageFilter);
            size += imageFiles.length;
        }
        return size;
    }

    private Object getKeyFromValue(Map hm, Object value) {
        for (Object o : hm.keySet()) {
            if (hm.get(o).equals(value)) {
                return o;
            }
        }
        return null;
    }

    public double getAverage(double average, double confidence) {
        return average == 0 ? confidence : (average + confidence) / 2;
    }

    private void logPeoplePassing(String currentPersonName) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String dateString = dateFormat.format(date);
        PrintWriter printWriter = null;
        confidenceAverage = getAverage(confidenceAverage, confidencePercent);
        try {
            printWriter = new PrintWriter(new BufferedWriter(new FileWriter
                    (LOGGER_DIRECTORY + "Logger.txt", true)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        printWriter.println("Person: " + currentPersonName + "{");
        printWriter.println("\t Date: " + dateString);
        printWriter.println("\t Confidence: " + confidencePercent);
        printWriter.println("\t Average: " + confidenceAverage);
        printWriter.println("}");
        printWriter.close();
    }
}
