package se.su.thesis;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import net.coobird.thumbnailator.Thumbnails;
import se.su.thesis.utils.ImageType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static com.sun.deploy.cache.Cache.copyFile;
import static se.su.thesis.utils.Constants.*;

public class TakePicture {

    public TakePicture(Image image) {
        takeTestPicture(image);
    }

    public TakePicture(String filePath, String currentPerson) {
        copyTestPictureToPerson(filePath, currentPerson);
    }

    public TakePicture (String filepath, String personName, String date){
        logLiveRecognitionPerson(filepath, personName, date);
    }

    public TakePicture(Image image, int pictureNumber, int labelNumber, ImageType type) throws IOException {
        takePicture(image, pictureNumber, labelNumber);
    }

    private void takeTestPicture(Image image) {
        File outputFile = new File(TEST_DIRECTORY + "test" + "." + PNG_FORMAT);
        try {
            writeFile(image, PNG_FORMAT, outputFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void takePicture(Image image, int pictureNumber, int labelNumber) {
        File outputFile = new File(PERSONS_DIRECTORY +
                Controller.currentPerson + "/" +
                Controller.personLabelMap.get(Controller.currentPerson) +
                "-" + Controller.currentPerson.toLowerCase() + "_" + pictureNumber + "." + PNG_FORMAT);
        nameFile(image, outputFile);
    }


    private void copyTestPictureToPerson(String filePath, String currentPerson) {
        File source = new File(filePath);
        File outputFile = new File(PERSONS_DIRECTORY +
                currentPerson + "/" +
                Controller.personLabelMap.get(currentPerson) +
                "-" + currentPerson.toLowerCase() + "_" + Controller.getPictureNumber
                (PERSONS_DIRECTORY + currentPerson + "/") + "." + PNG_FORMAT);
        try {
            copyFile(source, outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logLiveRecognitionPerson(String filePath, String currentPerson , String date) {
        String imageName = currentPerson + date;
        imageName = imageName.replaceAll("/", ".");
        imageName = imageName.replaceAll(" ", "_");
        imageName = imageName.replaceAll(":", "-");
        File source = new File(filePath);
        File outputFile = new File(LOGGER_DIRECTORY +
                imageName + "." + PNG_FORMAT);
        try {
            copyFile(source, outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void nameFile(Image image, File outputFile) {
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
