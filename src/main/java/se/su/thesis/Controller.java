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
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static se.su.thesis.utils.Constants.LABEL_TEXT;

public class Controller {

    private VideoCapture capture = new VideoCapture();
    private CascadeClassifier faceCascade;
    private ScheduledExecutorService timer;
    private boolean cameraActive = false;
    private boolean menuPopulated = false;
    private Image imageOfFace;
    private Rect roi;
    private Mat cropped;
    private Image imageToShow;
    private int faceSize;
    static String currentPerson = "";
    static HashMap<String, Integer> personLabelMap = new HashMap<>();

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
//                    if (roi != null){
//                        new Recognizer().recognize(personPath, Utils.matToBufferedImage(cropped));
//                    }
//                    if (personPath != null){
//                        new Recognizer().recognize(personPath, "src/main/resources/test/Brad.png");
//                    }
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
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
    private File[] getExistingPersons() {
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
            faceCascade = new CascadeClassifier("src/main/resources/haarcascade_frontalface_alt.xml");
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

                if (!frame.empty()) {
                    faceDetect(frame);
                }
                imageToShow = Utils.mat2Image(frame);
                if (roi != null) {
                    cropped = new Mat(frame, roi);
                    imageOfFace = Utils.mat2Image(cropped);
                }
                // Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY); //TODO: Cna we remove this? Yes
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

        String pathToPersonFolder = "src/main/resources/persons/" + currentPerson + "/";
        int pictureNumber = checkNumber(pathToPersonFolder);
        /*
        Todo: fix ^ pictureNumber to work as intended.
         */

        pictureNumber++;
        if (imageOfFace != null) {
            new TakePicture(imageOfFace, pictureNumber, ImageType.Training);
        }
    }

    @FXML
    public void takeTestPicture(ActionEvent event) throws IOException {
        if (currentPerson.isEmpty()) {
            System.err.println("Need to select a person");
            return;
        }

        if (imageOfFace != null) {
            new TakePicture(imageOfFace, -1, ImageType.Test);
        }
    }

    private int checkNumber(String path) {
        File[] files = new File(path).listFiles();
        if (files != null)
            return files.length;
        return 0;
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
        File files = new File("src/main/resources/persons/" + name + "/");
        if (!files.exists()) {
            if (files.mkdirs()) {
                System.err.println("Directory created");
                currentPerson = name;
                personLabelMap.put(name, personLabelMap.size());
                personLabel.setText(LABEL_TEXT + name);
            } else
                System.err.println("Failed to create directory");
        }
    }

    @FXML
    public void openTrainDialog() {
        if (!personLabelMap.isEmpty()) {
            ChoiceDialog dialog = new ChoiceDialog();
            for (String s : personLabelMap.keySet())
                dialog.getItems().add(s);
            dialog.setTitle("Train");
            dialog.setHeaderText("Train the algorithm with some images");
            dialog.setContentText("Select the person you wish to train the algorithm on:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(this::trainOnFaces);
        } else
            System.err.println("No persons in default folder");
    }

    private void trainOnFaces(String name) {
        System.err.println("train on face: " + name);
        Recognizer recognizer = new Recognizer();
        for (int i = 0; i < 3; i++){
            if (i == 0){
                recognizer.recognize(Constants.PERSONS_DIRECTORY + name, Constants.TEST_DIRECTORY+"Brad.png");
            }else if (i == 1){
                recognizer.recognize(Constants.PERSONS_DIRECTORY + name, Constants.TEST_DIRECTORY+"Matt.png");
            }else {
                recognizer.recognize(Constants.PERSONS_DIRECTORY + name, Constants.TEST_DIRECTORY+"Jea.png");
            }
            if (recognizer.getPredictedLabel() == 1){
                name = "Jea";
                System.err.println("This person is : " + name);
            }else if (recognizer.getPredictedLabel() == 4){
                System.err.println("This person is: " + name);
            }else{
                System.err.println("This person is: " + "unkown");
            }
        }

    }
}
