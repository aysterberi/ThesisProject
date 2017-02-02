package se.su.thesis;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.opencv.core.Core;

import java.io.File;

public class Main extends Application {

    public static final String PERSONS_DIRECTORY = "src/main/resources/persons/";

    private void populateMap() {
        File[] persons = new File(PERSONS_DIRECTORY).listFiles(File::isDirectory);
        if (persons != null) {
            for (File f : persons) {
                Controller.personLabelMap.putIfAbsent(f.getName(), getPersonLabel(f.getName()));
            }
        }
    }

    private int getPersonLabel(String name) {
        String path = PERSONS_DIRECTORY + name;
        File[] files = new File(path).listFiles();
        if (files != null)
            return Integer.parseInt(files[0].getName().substring(0, 1));
        return 0;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/gui.fxml"));
        primaryStage.setTitle("Facial recognition");
        primaryStage.setScene(new Scene(root, 1024, 768));
        populateMap();
        primaryStage.show();
    }


    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }
}
