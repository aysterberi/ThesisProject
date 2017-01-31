package se.su.thesis;

import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.indexer.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_calib3d.*;
import static org.bytedeco.javacpp.opencv_objdetect.*;

public class Recognizer {

    // TODO: Set this, in the example they use args
    private String classifierName = null;

    public void recognize() {
        Loader.load(opencv_objdetect.class);

        CvHaarClassifierCascade classifier = new CvHaarClassifierCascade(cvLoad(classifierName));
        if(classifier.isNull()) {
            System.err.println("Error loading classifier file:" + classifierName);
            System.exit(1);
        }
    }
}