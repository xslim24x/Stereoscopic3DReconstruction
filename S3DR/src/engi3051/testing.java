package engi3051;

import org.opencv.core.*;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.*;
import org.opencv.imgcodecs.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.*;
import org.opencv.calib3d.*;


import java.util.ArrayList;
import java.util.Iterator;


/**
 * Created by Slim on 11/5/2015.
 *
 * capture testing, this application captures images from all available cameras
 * then saves them in the root directory of the project
 */
public class testing {

    public static void main(String args[]){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        ArrayList<VideoCapture> cams = new ArrayList<VideoCapture>();
        for (int i = 0;i<10;i++){
            VideoCapture c = new VideoCapture(i);
            if (c.isOpened())
                cams.add(c);
        }

        System.out.println("Cameras found: " + cams.size());

        ArrayList<Mat> frames = new ArrayList<Mat>();
        ArrayList<Mat> desc = new ArrayList<Mat>();
        ArrayList<MatOfKeyPoint> keypts = new ArrayList<MatOfKeyPoint>();

        for(VideoCapture c : cams) {
            Mat f = new Mat();
            while (true) {
                if (c.read(f)) {
                    System.out.println("Frame Obtained");
                    System.out.println("Captured Frame Width " + f.width() + " Height " + f.height());
                    Imgcodecs.imwrite("camera"+cams.indexOf(c)+".jpg", f);
                    System.out.println("OK");
                    frames.add(f);
                    desc.add(new Mat(f.rows(),f.cols(),f.type()));
                    break;
                }
            }
        }

        FeatureDetector detector = FeatureDetector.create(FeatureDetector.FAST);
        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.FREAK);

        detector.detect(frames,keypts);
        extractor.compute(frames,keypts,desc);

        StereoBM smatcher = StereoBM.create();

        Mat Diffs = new Mat();
        smatcher.compute(desc.get(0),desc.get(1),Diffs);

        //Mat fundMat = Calib3d.findFundamentalMat();
        //Mat output = new Mat(Diffs.size(),Diffs.type());
        //Mat Q = Calib3d.stereoRectifyUncalibrated();
        //Calib3d.reprojectImageTo3D(Diffs,output,);

        for (MatOfKeyPoint mk : keypts){
            System.out.println(mk.size());
        }

        //use iterator to avoid concurrent modification
        Iterator<VideoCapture> c = cams.iterator();
        while(c.hasNext()){
            c.next().release();
            c.remove();
        }

    }

    private Mat disparityMap(Mat mLeft, Mat mRight){
        // Converts the images to a proper type for stereoMatching
        Mat left = new Mat();
        Mat right = new Mat();

        //Imgproc.cvtColor(rectLeft, left, Imgproc.COLOR_BGR2GRAY);
       // Imgproc.cvtColor(rectRight, right, Imgproc.COLOR_BGR2GRAY);

        // Create a new image using the size and type of the left image
        Mat disparity = new Mat(left.size(), left.type());

        int numDisparity = (int)(left.size().width/8);

        /*
        StereoSGBM stereoAlgo = new StereoSGBM(
                0,    // min DIsparities
                numDisparity, // numDisparities
                11,   // SADWindowSize
                2*11*11,   // 8*number_of_image_channels*SADWindowSize*SADWindowSize   // p1
                5*11*11,  // 8*number_of_image_channels*SADWindowSize*SADWindowSize  // p2

                -1,   // disp12MaxDiff
                63,   // prefilterCap
                10,   // uniqueness ratio
                0, // sreckleWindowSize
                32, // spreckle Range
                false); // full DP
        // create the DisparityMap - SLOW: O(Width*height*numDisparity)
        stereoAlgo.compute(left, right, disparity);

        Core.normalize(disparity, disparity, 0, 256, Core.NORM_MINMAX);
        */
        return disparity;
    }

}
