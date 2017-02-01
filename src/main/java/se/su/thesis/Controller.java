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
import se.su.thesis.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Controller {

    VideoCapture capture = new VideoCapture();
    private CascadeClassifier faceCascade;
    private ScheduledExecutorService timer;
    private boolean cameraActive = false;
    private static int cameraId = 0;
    static String currentPerson = "";
    private static final String LABEL_TEXT = "Current Person: ";
    private Image imageToShow;
    private int faceSize;
    private HashMap<String, Integer> personLabelMap = new HashMap<>();

    @FXML
    private Button startCameraButton;
    @FXML
    private Button pictureButton;
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
    protected void startCamera(ActionEvent event) {
        if (!this.cameraActive) {
            this.capture.open(cameraId);
            if (this.capture.isOpened()) {
                this.cameraActive = true;
                currentFrame.fitWidthProperty().bind(borderPane.getScene().widthProperty());

                Runnable frameGrabber = () -> {
                    imageToShow = getImage();
                    currentFrame.setImage(imageToShow);
                    // TODO: Scale to the window ^
                    // updateImageView(currentFrame, imageToShow);
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
                this.pictureButton.setDisable(false);
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
     * TODO: Some code need to be written so pictures are scanned from the right folder
     * TODO: At the time only the label is set to the name selected in the menu
     */
    @FXML
    public void setUpExistingPersonsMenu() {
        borderPane.requestLayout();
        File[] directories = getExistingPersons();
        if (directories != null) {
            for (File f : directories) {
                MenuItem menuItem = new MenuItem(f.getName());
                if (!existingPersonsMenu.getItems().contains(menuItem))
                    menuItem.setOnAction((event -> setCurrentPerson(f.getName())));
                existingPersonsMenu.getItems().add(menuItem);
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
        return new File("src/main/resources/persons/").listFiles(File::isDirectory);
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
        for (Rect aFacesArray : facesArray) {
            Imgproc.rectangle(frame, aFacesArray.tl(), aFacesArray.br(),
                    new Scalar(0, 255, 0), 3);
        }
    }

    private Image getImage() {
        Image imageToShow = null;
        Mat frame = new Mat();

        if (this.capture.isOpened()) {
            try {
                this.capture.read(frame);

                if (!frame.empty()){
                    faceDetect(frame);
                }
                imageToShow = Utils.mat2Image(frame);
                // Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
            } catch (Exception e) {
                System.err.println("Exception during the image elaboration:" + e);
            }
        }
        return imageToShow;
    }

    @FXML
    public void takePicture(ActionEvent actionEvent) throws IOException {
        if (currentPerson.equals("")) {
            System.err.println("Need to select a person");
            return;
        }
        String pathToPersonFolder = "src/main/resources/persons/" + currentPerson + "/";
        int pictureNumber = checkNumber(pathToPersonFolder);
        new TakePicture(imageToShow, pictureNumber);
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
    }

    private void createPersonFolder(String name) {
        File files = new File("src/main/resources/persons/" + name + "/");
        if (!files.exists()) {
            if (files.mkdirs()) {
                System.err.println("Directory created");
                currentPerson = name;
                personLabelMap.put(name, personLabelMap.size());
                personLabel.setText(LABEL_TEXT + name);
            }
            else
                System.err.println("Failed to create directory");
        }
    }
}
