package engi3051;

import org.opencv.calib3d.StereoBM;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.core.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Slim on 11/17/2015.
 */
public class ReconstructionSystem {
    ArrayList<Mat> frames = new ArrayList<Mat>();
    ArrayList<Mat> desc = new ArrayList<Mat>();
    ArrayList<MatOfKeyPoint> keypts = new ArrayList<MatOfKeyPoint>();

    private int left =0,right=1;
    public ArrayList<Camera> cams = new ArrayList<Camera>();

    private FeatureDetector detector;
    private DescriptorExtractor extractor;
    private StereoBM smatcher;

    public ReconstructionSystem(){
        detector = FeatureDetector.create(FeatureDetector.AKAZE);
        extractor = DescriptorExtractor.create(DescriptorExtractor.AKAZE);
        smatcher = StereoBM.create();

        initCams();
    }
    public boolean frameread(int c, Mat f){
        return cams.get(c).getFrame(f);
    }

    public BufferedImage mat2image(Mat f) throws IOException {
        BufferedImage i;
        MatOfByte byteMat = new MatOfByte();
        Imgcodecs.imencode(".jpg",f,byteMat);
        //System.setProperty("java.io.tmpdir", "C:/temp");
        ImageIO.setUseCache(false);
        return ImageIO.read(new ByteArrayInputStream(byteMat.toArray()));
        //Saving File doesnt work..
        //File savefile = new File("camera"+cams.indexOf(c)+".jpg");
        //ImageIO.write(image,"jpeg",savefile);
        //Imgcodecs.imwrite("camera"+cams.indexOf(c)+".jpg",f);
    }


    public void initCams(){

        // max 8 capture devices are connected
        for (int i = 0;i<8;i++){
            VideoCapture c = new VideoCapture(i);
            if (c.isOpened())
                cams.add(new Camera(c));
        }

    }

    public void calibrateCam(int c){
        cams.get(c).calibrateCam();
        System.out.println("Cams "+cams.size());
    }


    public Mat capture(){

        frames.clear();
        keypts.clear();
        desc.clear();

        frames.add(new Mat());
        frames.add(new Mat());

        System.out.println("frames: " + frames.size());
        System.out.println("keypts: " + keypts.size());
        System.out.println("desc: " + desc.size());

        cams.get(0).getFrame(frames.get(0));
        cams.get(1).getFrame(frames.get(1));

        detector.detect(frames,keypts);
        extractor.compute(frames,keypts,desc);

        Mat Diffs = new Mat();

        System.out.println("desc 0: " + desc.get(0).size());
        System.out.println("desc 1: " + desc.get(1).size());

        smatcher.compute(desc.get(0),desc.get(1),Diffs);

        //Mat fundMat = Calib3d.findFundamentalMat();
        //Mat output = new Mat(Diffs.size(),Diffs.type());
        //Mat Q = Calib3d.stereoRectifyUncalibrated();
        //Calib3d.reprojectImageTo3D(Diffs,output,);

        for (MatOfKeyPoint mk : keypts){
            System.out.println(mk.size());
        }
        return Diffs;
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
