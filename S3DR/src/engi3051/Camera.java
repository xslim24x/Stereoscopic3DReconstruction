package engi3051;
import org.opencv.core.*;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.*;
import org.opencv.imgcodecs.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.CalibrateCRF;
import org.opencv.videoio.*;
import org.opencv.calib3d.*;
import sun.misc.JavaIOAccess;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Slim on 11/6/2015.
 */
public class Camera {

    //chess board parameters for calibration
    private final int chessc = 6;
    private final int chessr = 4;
    //max number of pics to take to calibrate
    private final int MaxCap = 12;
    //captures to wait between shots
    private int calibtimer = 0;
    private boolean isCalibrated = false;
    private MatOfPoint3f obj = new MatOfPoint3f();
    private ReconstructionSystem rs;



    //video source of cam
    private VideoCapture camsource;
    //rotation/translation vectors -> extrinsic factors
    private List<Mat> rvecs = new ArrayList<Mat>();
    private List<Mat> tvecs = new ArrayList<Mat>();
    //camera matrix and distortion coefficients -> intrinsic factors
    private Mat intrinsic = new Mat(3, 3, CvType.CV_32FC1);
    private Mat distCoeffs = new Mat();


    private ArrayList<Mat> imagePoints = new ArrayList<Mat>();
    private ArrayList<Mat> objectPoints = new ArrayList<Mat>();
    private Mat savedImage = new Mat();
    private int captures = 0;


    public VideoCapture getCamsource() {
        return camsource;
    }

    public List<Mat> getRvecs() {
        return rvecs;
    }

    public List<Mat> getTvecs() {
        return tvecs;
    }

    public Mat getIntrinsic() {
        return intrinsic;
    }

    public Mat getDistCoeffs() {
        return distCoeffs;
    }

    public ArrayList<Mat> getObjectPoints() {
        return objectPoints;
    }

    public ArrayList<Mat> getImagePoints() {
        return imagePoints;
    }

    public Size getSize(){
        return savedImage.size();
    }

    public void setIsCalibrated(boolean c){this.isCalibrated=c;}
    /*
    this.capture = new VideoCapture();
    this.cameraActive = false;
    this.obj = new MatOfPoint3f();
    this.imageCorners = new MatOfPoint2f();
    this.savedImage = new Mat();
    this.undistoredImage = null;
    this.imagePoints = new ArrayList<Mat>();
    this.objectPoints = new ArrayList<Mat>();
    this.distCoeffs = new Mat();
    this.successes = 0;
    this.isCalibrated = false;
    */

    public Camera(VideoCapture c, ReconstructionSystem r){
        //CaptureDeviceManager.getDeviceList(null);
        camsource = c;
        rs = r;
        // init needed variables according to OpenCV docs
        intrinsic.put(0, 0, 1);
        intrinsic.put(1, 1, 1);

        for (int j = 0; j < chessc*chessr; j++) {
            obj.push_back(new MatOfPoint3f(new Point3(j / chessc, j % chessr, 0.0f)));
        }
    }

    public void rawFrame(Mat f){
        camsource.read(f);
    }

    public boolean getFrame(Mat f){
        boolean found = false;
        MatOfPoint2f imageCorners = new MatOfPoint2f();
        boolean rdy = camsource.read(f);
        if (calibtimer > 0){
            calibtimer--;
        }
        //leaving here to show chess board
        if (!isCalibrated){
            Mat gray = new Mat();
            Imgproc.cvtColor(f, gray, Imgproc.COLOR_BGR2GRAY);
            Size boardSize = new Size(chessc,chessr);
            found = Calib3d.findChessboardCorners(gray, boardSize, imageCorners,
                    Calib3d.CALIB_CB_ADAPTIVE_THRESH + Calib3d.CALIB_CB_NORMALIZE_IMAGE + Calib3d.CALIB_CB_FAST_CHECK);
            if (found)
            {
                // optimization
                TermCriteria term = new TermCriteria(TermCriteria.EPS | TermCriteria.MAX_ITER, 30, 0.1);
                Imgproc.cornerSubPix(gray, imageCorners, new Size(11, 11), new Size(-1, -1), term);
                // save the current frame for further elaborations
                gray.copyTo(this.savedImage);
                // show the chessboard inner corners on screen
                Calib3d.drawChessboardCorners(f, boardSize, imageCorners, found);

                if (calibtimer == 0 && captures < MaxCap)
                {
                    //System.out.println("Calibrating");
                    // save all the needed values
                    imagePoints.add(imageCorners);
                    objectPoints.add(obj);
                    calibtimer = 20;
                    captures++;
                }

                // reach the correct number of images needed for the calibration
                if (captures == MaxCap && !isCalibrated)
                {
                    //System.out.println("Calibrated");
                    //calibrateCam();
                }

            }

        }
        return found;
    }

    public void calibrateCam()
    {
        // calibrate!
        Calib3d.calibrateCamera(objectPoints, imagePoints, savedImage.size(), intrinsic, distCoeffs, rvecs, tvecs);
        this.isCalibrated = true;
    }


}
