package se.su.thesis;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import net.coobird.thumbnailator.Thumbnails;

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
        bImage = cropImage(bImage);
        if (bImage == null)
            System.err.println("Image crop failed");
        System.err.println("Image width: " + bImage.getWidth() + " Image height: " + bImage.getHeight());
        try {
            ImageIO.write(bImage, "png", outputFile);
            System.err.println("Snapped picture");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private BufferedImage cropImage(BufferedImage image) {
        try {
            return Thumbnails.of(image).size(150, 150).asBufferedImage();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
