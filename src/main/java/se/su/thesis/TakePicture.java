package se.su.thesis;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import net.coobird.thumbnailator.Thumbnails;
import se.su.thesis.utils.ImageType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static se.su.thesis.utils.Constants.*;

public class TakePicture {
    public TakePicture(Image image, int i, ImageType type) throws IOException {
        if (type == ImageType.Training)
            takePicture(image, i);
        else
            takeTestPicture(image);
    }

    private void takeTestPicture(Image image) {
        File outputFile = new File(TEST_DIRECTORY + Controller.currentPerson + PNG_FORMAT);
        try {
            writeFile(image, PNG_FORMAT, outputFile);
            System.err.println("Snapped test picture");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void takePicture(Image image, int i) {
        File outputFile = new File(PERSONS_DIRECTORY +
                Controller.currentPerson + "/" +
                Controller.personLabelMap.get(Controller.currentPerson) +
                "-" + Controller.currentPerson.toLowerCase() + "_" + i + PNG_FORMAT);
        try {
            writeFile(image, PNG_FORMAT, outputFile);
            System.err.println("Snapped training picture");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeFile(Image image, String imageFormat, File path) throws IOException {
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        bufferedImage = cropImage(bufferedImage);
        if (bufferedImage != null)
            ImageIO.write(bufferedImage, imageFormat, path);
        else
            System.err.println("Image crop failed");
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
