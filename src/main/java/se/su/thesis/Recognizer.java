/*
 * Modified from https://github.com/bytedeco
 */

package se.su.thesis;

import org.bytedeco.javacpp.opencv_face;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Recognizer {

//    // NOTE: All uses of Mat are modified because cvMat is deprecated
//    // If all else fails, try using cvMat instead.
//
//    private int numberOfTrainingFaces = 0;
//    IplImage[] trainingFaces;
//    IplImage[] testFaces;
//    Mat personNumTruthMat;
//    int numberOfPersons;
//    final List<String> personNames = new ArrayList<>();
//    int numberOfEigenValues;
//    IplImage[] eigenVectArr;
//    Mat eigenValueMat;
//    IplImage pAverageTrainImage;
//    Mat projectedTrainFaceMat;
//
//    public Recognizer() {
//        opencv_face.FaceRecognizer faceRecognizer = new opencv_face.LBPHFaceRecognizer();
//    }
//
//    public void learn(final String trainingFileName) {
//        int i;
//
//        System.err.println("<---------------------------------->");
//        System.err.println("Loading training images from " + trainingFileName);
//        trainingFaces = loadFaceImageArray(trainingFileName);
//    }
//
//    private IplImage[] loadFaceImageArray(final String trainingFileName) {
//        IplImage[] faceImageArr;
//        BufferedReader imgListFile;
//        String filename;
//        int iFace = 0;
//        int numFaces = 0;
//        int i;
//
//        try {
//            imgListFile = new BufferedReader(new FileReader(trainingFileName));
//
//            // TODO: this could be adapted to get files in a different way
//            // Currently this requires us to create a csv file that list all the files
//            while (true) {
//                final String line = imgListFile.readLine();
//                if (line == null || line.isEmpty())
//                    break;
//                numFaces++;
//            }
//            System.err.println("numFaces:" + numFaces);
//            imgListFile = new BufferedReader(new FileReader(trainingFileName));
//
//            faceImageArr = new IplImage[numFaces];
//            personNumTruthMat = new Mat(1, numFaces, CV_32SC1);
//            for (int j = 0; j < numFaces; j++)
//                personNumTruthMat.put(new Mat(0, j, 0));
//
//            personNames.clear();
//            numberOfPersons = 0;
//
//            for (iFace = 0; iFace < numFaces; iFace++) {
//                String personName;
//                String sPersonName;
//                int personNumber;
//
//                final String line = imgListFile.readLine();
//                if (line.isEmpty())
//                    break;
//                final String[] tokens = line.split(" ");
//                personNumber = Integer.parseInt(tokens[0]);
//                personName = tokens[1];
//                filename = tokens[2];
//                sPersonName = personName;
//                System.err.println("Got " + iFace + " " + personNumber + " " + personName + " " + filename);
//
//                if (personNumber > numberOfPersons) {
//                    personNames.add(sPersonName);
//                    numberOfPersons = personNumber;
//                    System.err.println("Got new Person " + sPersonName + " -> numberOfPersons = " + numberOfPersons + " [" + personNames.size() + "]");
//                }
//                personNumTruthMat.put(new Mat(0, iFace, personNumber));
//                // TODO: Continue here
//                //faceImageArr[iFace] = load
//            }
//
//        } catch (IOException ioe) {
//
//        }
//    }
}