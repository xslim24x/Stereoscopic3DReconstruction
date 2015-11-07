package engi3051;
import org.opencv.core.*;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.*;
import org.opencv.imgcodecs.*;
import org.opencv.photo.CalibrateCRF;
import org.opencv.videoio.*;
import org.opencv.calib3d.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Slim on 11/6/2015.
 */
public class Camera {
    protected VideoCapture camsource;


    public Camera(VideoCapture c){
        camsource = c;

    }


}
