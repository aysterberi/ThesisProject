package se.su.thesis;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TakePicture {
    public TakePicture(Image image, int i) throws IOException {
        takePicture(image, i);
    }

    private void takePicture (Image image, int i) {
        File outputFile = new File("src/main/resources/persons/" +
                Controller.currentPerson + "/" +
                Controller.personLabelMap.get(Controller.currentPerson) +
                "-" + Controller.currentPerson.toLowerCase() + "_" + i + ".png");
        BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
        try {
            ImageIO.write(bImage, "png", outputFile);
            System.err.println("Snapped picture");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
