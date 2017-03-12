/*
 * Modified from https://github.com/bytedeco
 * to allow use without the args array
 */

package se.su.thesis;

import org.bytedeco.javacpp.opencv_core.Mat;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.IntBuffer;
import java.util.Map;

import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_core.MatVector;
import static org.bytedeco.javacpp.opencv_face.FaceRecognizer;
import static org.bytedeco.javacpp.opencv_face.createEigenFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static se.su.thesis.utils.Constants.PERSON_SAVE_DATA;

public class Recognizer {
    private int predictedLabel;
    private FaceRecognizer faceRecognizer;
    public static boolean dataChanged = true;
    // I used these to try to find best parameters
//    private double threshhold;
//    private int firstParameter = 1;

    public Recognizer() {
//        faceRecognizer = createEigenFaceRecognizer(0, THRESH_TRIANGLE);
        faceRecognizer = createEigenFaceRecognizer(2, 4000);
//        faceRecognizer = createEigenFaceRecognizer();
        this.trainOnPictures();
    }

// This is code for liveStream recognition
//    public void recognize(BufferedImage pathToTestImage) {
//        BufferedImage test = TakePicture.cropImage(pathToTestImage);

//    public void recognize(String pathToTestImage) {
//        Mat testImage = imread(pathToTestImage, CV_LOAD_IMAGE_GRAYSCALE);

    public void recognize(){
        // I used these to try to find best parameters
//        faceRecognizer = createEigenFaceRecognizer(firstParameter, threshhold);
        Mat testImage = imread("src/main/resources/test/test.png", CV_LOAD_IMAGE_GRAYSCALE);
        if (dataChanged){
            trainOnPictures();
            System.out.println("IF DATACHANGE TRUE");
        } else {
            if (new File(PERSON_SAVE_DATA).exists()){
                System.err.println("loading data");
                faceRecognizer.load(PERSON_SAVE_DATA);
            }else {
                trainOnPictures();
            }
        }

        predictedLabel = faceRecognizer.predict(testImage);

        // I used these to try to find best parameters

//            if (predictedLabel == 0){
//                threshhold += 500.0;
//                if (threshhold > 9999.0){
//                    firstParameter += 1;
//                    threshhold = 500;
//                }
//            } else if (predictedLabel == 2){
//                System.out.println("Predicted Label " + predictedLabel);
////                System.out.println(threshhold);
//                System.out.println(faceRecognizer.getThreshold());
//                System.out.println(firstParameter);
//            }
    }

    private void trainOnPictures() {
        File[] directories = Controller.getExistingPersons();
        FilenameFilter imageFilter = (dir, name) -> {
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
            System.out.println(faceRecognizer.getThreshold());
            System.out.println(faceRecognizer.getLabelInfo(predictedLabel));
            dataChanged = false;
        }
    }

    public int getPredictedLabel() {
        return predictedLabel;
    }

    public String getNameOfPredictedPerson(){
        if (predictedLabel == 0){
            return "Unknown";
        }
        return getKeyFromValue(Controller.personLabelMap, predictedLabel).toString();
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
}
