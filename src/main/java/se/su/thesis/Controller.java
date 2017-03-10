package se.su.thesis;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;
import se.su.thesis.utils.Constants;
import se.su.thesis.utils.ImageType;
import se.su.thesis.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static se.su.thesis.utils.Constants.*;

public class Controller {

    private VideoCapture capture = new VideoCapture();
    private CascadeClassifier faceCascade;
    private ScheduledExecutorService timer;
    private ScheduledExecutorService recognizeTimer;
    private boolean cameraActive = false;
    private boolean menuPopulated = false;
    private Image imageOfFace;
    private Rect roi;
    private Mat cropped;
    private Image imageToShow;
    private Recognizer faceRecognizer;
    private int faceSize;
    static String currentPerson = "";
    static HashMap<String, Integer> personLabelMap = new HashMap<>();
    static HashMap<String, Integer> testPersonMap = new HashMap<>();

    @FXML
    private Button startCameraButton;
    @FXML
    private Button pictureButton;
    @FXML
    private Button testPictureButton;
    @FXML
    private ImageView currentFrame;
    @FXML
    private BorderPane borderPane;
    @FXML
    private Label personLabel;
    @FXML
    private Menu existingPersonsMenu;
    @FXML
    private Menu topMenu;
    @FXML
    private MenuBar menuBar;
    @FXML
    private MenuItem trainMenuButton;

    @FXML
    protected void startCamera(ActionEvent event) {
        if (!this.cameraActive) {
            int cameraId = 0;
            this.capture.open(cameraId);
            if (this.capture.isOpened()) {
                this.cameraActive = true;
                currentFrame.fitWidthProperty().bind(borderPane.getScene().widthProperty());

                Runnable frameGrabber = () -> {
                    imageToShow = getImage();
                    currentFrame.setImage(imageToShow);
                };

                Runnable recognizer = () -> {
                  if (roi != null) {
                      new Recognizer().recognize(Utils.matToBufferedImage(cropped));
                      trainOnFaces("liverecog", faceRecognizer);
                  }
                };

                // Frame grabber only captures video output
                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

                // recognizer is run twice every second (can be changed to be run more often)
                this.recognizeTimer = Executors.newSingleThreadScheduledExecutor();
                this.recognizeTimer.scheduleAtFixedRate(recognizer, 0, 500, TimeUnit.MILLISECONDS);

                this.pictureButton.setDisable(false);
                this.testPictureButton.setDisable(false);
                this.startCameraButton.setText("Stop Camera");
            } else {
                System.err.println("Impossible to open the camera connection");
            }
        } else {
            this.pictureButton.setDisable(true);
            this.cameraActive = false;
            this.startCameraButton.setText("Start Camera");
            this.stopAcquisition();
        }
    }

    /**
     * Looks under persons folder to see if there are any folders
     * If any folders exist they will be put in the menu
     * This only works after you open the menu once and open it again
     * TODO: Fix above behaviour
     */
    @FXML
    public void setUpExistingPersonsMenu() {
        if (!menuPopulated) {
            borderPane.requestLayout();
            File[] directories = getExistingPersons();
            if (directories != null) {
                for (File f : directories) {
                    MenuItem menuItem = new MenuItem(f.getName());
                    menuItem.setOnAction((event -> setCurrentPerson(f.getName())));
                    existingPersonsMenu.getItems().add(menuItem);
                }
                menuPopulated = true;
            }
        }
    }

    private void setCurrentPerson(String name) {
        currentPerson = name;
        personLabel.setText(LABEL_TEXT + name);
    }

    /**
     * @return an array of directories in the persons folder
     */
    public static File[] getExistingPersons() {
        return new File(Constants.PERSONS_DIRECTORY).listFiles(File::isDirectory);
    }

    private void stopAcquisition() {
        if (this.timer != null && !this.timer.isShutdown()) {
            try {
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ie) {
                System.err.println("Exception in stopping the frame capture, trying to release the camera now");
            }
        }

        if (this.capture.isOpened())
            this.capture.release();
    }

    private void faceDetect(Mat frame) {
        MatOfRect faces = new MatOfRect();
        Mat grayFrame = new Mat();
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(grayFrame, grayFrame);

        if (faceSize == 0) {
            int height = grayFrame.rows();
            if (Math.round(height * 0.2f) > 0) {
                faceSize = Math.round(height * 0.2f);
            }
        }

        // detect faces
        if (faceCascade == null) {
            faceCascade = new CascadeClassifier(HAAR_CASCADE);
        }

        faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, Objdetect.CASCADE_SCALE_IMAGE,
                new Size(faceSize, faceSize), new Size());

        // each rectangle in faces is a face: draw them!
        Rect[] facesArray = faces.toArray();
        for (Rect facesRect : facesArray) {
            Imgproc.rectangle(frame, facesRect.tl(), facesRect.br(),
                    new Scalar(0, 255, 0), 3);
            roi = facesRect.clone();
        }
    }

    private Image getImage() {
        Image imageToShow = null;
        Mat frame = new Mat();

        if (this.capture.isOpened()) {
            try {
                this.capture.read(frame);

                if (!frame.empty())
                    faceDetect(frame);

                imageToShow = Utils.mat2Image(frame);
                if (roi != null) {
                    cropped = new Mat(frame, roi);
                    imageOfFace = Utils.mat2Image(cropped);
                    roi = null;
                }
            } catch (Exception e) {
                System.err.println("Exception during the image elaboration:" + e);
            }
        }
        return imageToShow;
    }

    @FXML
    public void takePicture(ActionEvent actionEvent) throws IOException {
        if (currentPerson.isEmpty()) {
            System.err.println("Need to select a person");
            return;
        }

        String pathToPersonFolder = PERSONS_DIRECTORY + currentPerson + "/";
        int pictureNumber = getPictureNumber(pathToPersonFolder);
        int labelNumber = 2;
        System.err.println("pictureNumber: " + getPictureNumber(pathToPersonFolder));
        if (imageOfFace != null) {
            Recognizer.dataChanged = true;
            new TakePicture(imageOfFace, pictureNumber, labelNumber, ImageType.Training);
        }
    }

    @FXML
    public void takeTestPicture(ActionEvent event) throws IOException {
        if (currentPerson.isEmpty()) {
            System.err.println("Need to select a person");
            return;
        }

        if (imageOfFace != null) {
            new TakePicture(imageOfFace);
        }
    }

    private int getPictureNumber(String path) {
        File[] files = new File(path).listFiles();
        System.err.println("Files length: " + files.length);
        if (files.length == 0) {
            return 1;
        } else {
            return files.length + 1;
        }
    }

    private static int getLabel(String path) {
        HashSet<Integer> labels = new HashSet<>();

        int label = 1;
        File[] files = new File(PERSONS_DIRECTORY).listFiles();
        if (files.length == 0) {
            return 1;
        } else {
            for (Integer intLabel : personLabelMap.values()) {
                labels.add(intLabel);
            }
            for (Integer i : labels) {
                if (labels.contains(label)) {
                    label++;
                }
            }
            labels.clear();
            return label;
        }
    }

    /**
     * Called when clicking close in the menu bar
     */
    @FXML
    public void closeProgram() {
        System.err.println("Program closed by user");
        System.exit(0);
    }


    @FXML
    public void createNewPerson() {
        TextInputDialog newPersonDialog = new TextInputDialog("New Person");
        newPersonDialog.setTitle("NewPerson");
        newPersonDialog.setHeaderText("Create a new person");
        newPersonDialog.setContentText("Enter the name for the new person:");

        Optional<String> result = newPersonDialog.showAndWait();
        result.ifPresent(this::createPersonFolder);
        existingPersonsMenu.getItems().clear();
        menuPopulated = false;
    }

    private void createPersonFolder(String name) {
        File files = new File(PERSONS_DIRECTORY + name + "/");
        if (!files.exists()) {
            if (files.mkdirs()) {
                System.err.println("Directory created");
                currentPerson = name;
                personLabelMap.put(name, getLabel(PERSONS_DIRECTORY));
                personLabel.setText(LABEL_TEXT + name);
                Recognizer.dataChanged = true;
            } else
                System.err.println("Failed to create directory");
        }
    }

    @FXML
    public void openFaceRecognitionDialog() {
        File[] testPersons = getExistingTestPersons();
        if (testPersons.length != 0) {
            ChoiceDialog dialog = new ChoiceDialog();

            for (File s : testPersons)
                if (s.getName().endsWith(".png"))
                    dialog.getItems().add(s.getName());


            dialog.setTitle("Face Recognition");
            dialog.setHeaderText("Try to recognize someone from the training sets");
            dialog.setContentText("Select the test image to use:");
            Optional<String> result = dialog.showAndWait();
            //result.ifPresent(this::trainOnFaces);
        } else
            System.err.println("No persons in default folder");
    }

    private File[] getExistingTestPersons() {
        return new File(TEST_DIRECTORY).listFiles();
    }

    private void trainOnFaces(String name, Recognizer recognizer) {
        System.err.println("Recognizing Face of: " + name);
//        recognizer.recognize(TEST_DIRECTORY + name);

        System.err.println("predicted label: " + recognizer.getPredictedLabel());
        System.err.println("The predicted person is: " + recognizer.getNameOfPredictedPerson());
    }
}
