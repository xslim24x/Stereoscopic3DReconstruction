package engi3051;

import org.opencv.calib3d.Calib3d;
import org.opencv.calib3d.StereoBM;
import org.opencv.calib3d.StereoSGBM;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.core.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;

/**
 * Created by Slim on 11/17/2015.
 */
public class ReconstructionSystem {

    private int left =0,right=1;
    public ArrayList<Camera> cams = new ArrayList<Camera>();

    private FeatureDetector detector;
    private DescriptorExtractor extractor;
    private StereoBM smatcher;
    private StereoSGBM sgbmmatcher;

    public ReconstructionSystem(){
        detector = FeatureDetector.create(FeatureDetector.FAST);
        extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        //smatcher = StereoBM.create();
        //sgbmmatcher = StereoSGBM.create(2,2,1);

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

        Mat lframe = new Mat();
        Mat rframe = new Mat();

        cams.get(0).getFrame(lframe);
        cams.get(1).getFrame(rframe);

        //get grayscale left/right frames
        Imgproc.cvtColor(lframe, lframe, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(rframe, rframe, Imgproc.COLOR_BGR2GRAY);

        Mat disparity = new Mat(lframe.size(), lframe.type());
        int numDisparity = (int)(lframe.size().width/8);

        //smatcher = StereoBM.create(96,15);
        //smatcher.compute(lframe,rframe,disparity);
        sgbmmatcher = StereoSGBM.create(12,96,15);
        sgbmmatcher.compute(lframe,rframe,disparity);

        //Mat fundMat = Calib3d.findFundamentalMat();
        //Mat output = new Mat(Diffs.size(),Diffs.type());
        //Mat Q = Calib3d.stereoRectifyUncalibrated();
        //Calib3d.reprojectImageTo3D(Diffs,output,);

        //normalize to increase contrast
        Core.normalize(disparity, disparity, 0, 256, Core.NORM_MINMAX);

        return disparity;
    }

    private Mat disparityMap(Mat mLeft, Mat mRight){
        // Converts the images to a proper type for stereoMatching

        Mat lold = new Mat();
        Mat rold = new Mat();
        cams.get(0).getFrame(lold);
        cams.get(1).getFrame(rold);
        Mat leftfr = new Mat();
        Mat rightfr = new Mat();

        Imgproc.cvtColor(lold, leftfr, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(rold, rightfr, Imgproc.COLOR_BGR2GRAY);

        // Create a new image using the size and type of the left image
        Mat disparity = new Mat(leftfr.size(), leftfr.type());

        int numDisparity = (int)(leftfr.size().width/8);

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

    public Mat reconstruct(){

        Mat lframe = new Mat();
        Mat lgray = new Mat();
        Mat leftDesc = new Mat();
        MatOfKeyPoint leftKeypt = new MatOfKeyPoint();
        Mat rframe = new Mat();
        Mat rgray = new Mat();
        Mat rightDesc = new Mat();
        MatOfKeyPoint rightKeypt = new MatOfKeyPoint();

        cams.get(0).getFrame(lframe);
        cams.get(1).getFrame(rframe);


        Imgproc.cvtColor(lframe, lgray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(rframe, rgray, Imgproc.COLOR_BGR2GRAY);

        detector.detect(lgray,leftKeypt);
        extractor.compute(lgray,leftKeypt,leftDesc);

        detector.detect(rgray,rightKeypt);
        extractor.compute(rgray,rightKeypt,rightDesc);

        Mat disparity = new Mat();//frames.get(1).size(), frames.get(1).type());
        int numDisparity = (int)(lframe.size().width/8);

        System.out.println("Left\nSize:" + leftDesc.size() + " Type:" + leftDesc.type() + " Depth:" + leftDesc.depth());
        System.out.println("Right\nSize:" + rightDesc.size() + " Type:" + rightDesc.type());

        Mat l8bit = new Mat();
        Mat r8bit = new Mat();
        lgray.convertTo(l8bit,0);
        rgray.convertTo(r8bit,0);
        smatcher = StereoBM.create(96,15);
        smatcher.compute(l8bit,r8bit,disparity);
        //sgbmmatcher = StereoSGBM.create(2,90,15);
        //sgbmmatcher.compute(lgray,rgray,disparity);




        //Mat fundMat = Calib3d.findFundamentalMat();
        //Mat output = new Mat(Diffs.size(),Diffs.type());
        //Mat Q = Calib3d.stereoRectifyUncalibrated();
        //Calib3d.reprojectImageTo3D(Diffs,output,);

        Core.normalize(disparity, disparity, 0, 256, Core.NORM_MINMAX);

        return disparity;
    }

}
