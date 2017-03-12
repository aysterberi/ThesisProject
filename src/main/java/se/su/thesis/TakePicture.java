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

    public TakePicture(Image image) {
        takeTestPicture(image);
    }

    public TakePicture(Image image, int pictureNumber, int labelNumber, ImageType type) throws IOException {
            takePicture(image, pictureNumber, labelNumber);
    }

    private void takeTestPicture(Image image) {
//        File outputFile = new File(TEST_DIRECTORY + Controller.currentPerson + "."+PNG_FORMAT);
        File outputFile = new File(TEST_DIRECTORY + "test" + "."+PNG_FORMAT);
        try {
            writeFile(image, PNG_FORMAT, outputFile);
//            System.err.println(outputFile.getAbsolutePath());
//            System.err.println("Snapped test picture");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void takePicture(Image image, int pictureNumber, int labelNumber) {
        File outputFile = new File(PERSONS_DIRECTORY +
                Controller.currentPerson + "/" +
                Controller.personLabelMap.get(Controller.currentPerson) +
                "-" + Controller.currentPerson.toLowerCase() + "_" + pictureNumber + "." + PNG_FORMAT);
        try {
            writeFile(image, PNG_FORMAT, outputFile);
            System.err.println(outputFile.getAbsolutePath());
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

    public static BufferedImage cropImage(BufferedImage image) {
        try {
            return Thumbnails.of(image).size(IMAGE_SIZE, IMAGE_SIZE).asBufferedImage();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
