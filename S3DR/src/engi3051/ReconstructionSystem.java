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
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.*;
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


    public Mat disparity(int type){

        Mat lframe = new Mat();
        Mat rframe = new Mat();

        cams.get(0).getFrame(lframe);
        cams.get(1).getFrame(rframe);

        //get grayscale left/right frames
        Imgproc.cvtColor(lframe, lframe, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(rframe, rframe, Imgproc.COLOR_BGR2GRAY);

        Mat disparity = new Mat(lframe.size(), lframe.type());
        int numDisparity = (int)(lframe.size().width/8);

        if (type == 1){
            sgbmmatcher = StereoSGBM.create(12,96,15);
            sgbmmatcher.compute(lframe,rframe,disparity);
        }
        else{
            smatcher = StereoBM.create(96,15);
            smatcher.compute(lframe,rframe,disparity);
        }

        return disparity;
    }

    public Mat normalizedDisp(int type){
        Mat disp = disparity(type);
        Core.normalize(disp, disp, 0, 256, Core.NORM_MINMAX);
        return disparity(type);
    }

    private Mat disparityMap(Mat mLeft, Mat mRight){
        // Converts the images to a proper type for stereoMatching

        Mat lold = new Mat();
        Mat rold = new Mat();
        cams.get(0).getFrame(lold);
        cams.get(1).getFrame(rold);
        Mat leftfr = new Mat();
        Mat rightfr = new Mat();

        Imgproc.cvtColor(lold, leftfr, Imgproc.COLOR_RGB2GRAY);
        Imgproc.cvtColor(rold, rightfr, Imgproc.COLOR_RGB2GRAY);

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

    public void reconstruct(){
        Camera left = cams.get(0);
        Camera right = cams.get(1);

        Mat R1 = new Mat();
        Mat R2 = new Mat();
        Mat P1 = new Mat();
        Mat P2 = new Mat();

        Mat R = new Mat();
        Mat T = new Mat();
        Mat E = new Mat();
        Mat F = new Mat();
        Mat Q = new Mat();

        //stupidly long so expanded func
        Calib3d.stereoCalibrate(
                left.getObjectPoints(),
                left.getImagePoints(),
                right.getImagePoints(),
                left.getIntrinsic(),
                left.getDistCoeffs(),
                right.getIntrinsic(),
                right.getDistCoeffs(),
                left.getSize(),
                R,T,E,F);

        Calib3d.stereoRectify(
                left.getIntrinsic(),
                left.getDistCoeffs(),
                right.getIntrinsic(),
                right.getDistCoeffs(),
                left.getSize(),
                R,T,R1,R2,P1,P2,Q);

        Mat disp = new Mat();
        disp =  disparity(1);

        Mat points4D = new Mat();

//        Calib3d.triangulatePoints(
//                P1,
//                P2,
//                left.getObjectPoints(),
//                right.getObjectPoints(),
//                points4D);

        Calib3d.reprojectImageTo3D(disp, points4D,Q);

        Imgproc.cvtColor(disp, disp, Imgproc.COLOR_BGR2RGB);
        CreatePointcloud(disp, points4D);

        return;
    }


    public void CreatePointcloud(Mat disp, Mat pc){

        String filename= "d:/output.txt";
        PrintStream outstream = null; //the true will append the new data
        try {
            outstream = new PrintStream(new File(filename));


            for (int i = 0; i < pc.rows(); i++){
                for (int j = 0; j < pc.cols(); j++) {
                    double[] pnt = new double[3];
                    pnt = pc.get(j, i);
//                    pnt[0] = -pnt[0];
//                    pnt[2] = -pnt[2];
                    String line = pnt[0] + " " + pnt[1] + " " + pnt[2] + " " + disp.get(i,j)[0] + " " + disp.get(i,j)[1] + " " + disp.get(i,j)[2];
                    outstream.println(line);
                    ;
                }
            }
            outstream.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
