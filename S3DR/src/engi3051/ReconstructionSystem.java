package engi3051;

import boofcv.abst.fiducial.calib.ConfigChessboard;
import boofcv.abst.geo.calibration.CalibrateStereoPlanar;
import boofcv.abst.geo.calibration.CalibrationDetector;
import boofcv.factory.calib.FactoryPlanarCalibrationTarget;
import boofcv.factory.calib.*;
import boofcv.gui.image.ShowImages;
import boofcv.io.UtilIO;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.calib.StereoParameters;
import boofcv.struct.image.ImageFloat32;
import org.opencv.calib3d.Calib3d;
import org.opencv.calib3d.StereoBM;
import org.opencv.calib3d.StereoSGBM;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.core.*;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.*;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Timer;

/**
 * Created by Slim on 11/17/2015.
 */



public class ReconstructionSystem {
    //initial values of left/ right
    private int left,right;
    private ArrayList<Camera> cams = new ArrayList<Camera>();

    //openCV
    //private FeatureDetector detector;
    //private DescriptorExtractor extractor;
    private StereoBM smatcher;
    private StereoSGBM sgbmmatcher;
    private BufferedImage camdisc;
    //boofcv
    final int calibnum = 15;
    int pictimer = 20;
    private boolean isCalib;//if cams change set to false
    ArrayList<BufferedImage> leftpics;
    ArrayList<BufferedImage> rightpics;
    private StereoParameters stereoCalib;

    //constructor
    public ReconstructionSystem(){
        Mat i = new Mat(400,600, CvType.CV_8UC3);
        Imgproc.putText(i,"Camera Offline", new Point(20,40),3,1,new Scalar(224,224,0),3);
        try {
            camdisc = mat2image(i);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //detector = FeatureDetector.create(FeatureDetector.FAST);
        //extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        //smatcher = StereoBM.create();
        //sgbmmatcher = StereoSGBM.create(2,2,1);

        initCams();
        leftpics = new ArrayList<BufferedImage>();
        rightpics = new ArrayList<BufferedImage>();
        left = 0;
        right = 1;
        isCalib = false;
    }

    public boolean frameread(int c, Mat f){
        return cams.get(c).getFrame(f);
    }

    public BufferedImage StereoCam() throws IOException {
        //act as a messenger to user
        //TODO create timer for overlayed text
        //coordinate calibration
        if (pictimer>0)
                pictimer--;
        Mat l = new Mat();
        Mat r = new Mat();
        boolean chessinleft = frameread(left, l);
        boolean chessinright = frameread(right, r);
        BufferedImage iml = mat2image(l);
        BufferedImage imr = mat2image(r);
        if (chessinleft && chessinright){
            //TODO add message with count
            if (!isCalib && (pictimer == 0) && (leftpics.size() < calibnum)){
                Mat lraw = new Mat();
                Mat rraw = new Mat();
                cams.get(left).rawFrame(lraw);
                cams.get(right).rawFrame(rraw);

                leftpics.add(mat2image(lraw));
                rightpics.add(mat2image(rraw));
                System.out.println("Calib pic:" + leftpics.size());
                pictimer=20;
                if (leftpics.size() == calibnum){
                    boofCalib();
                }
            }

        }

        return joinImages(iml,imr);
    }

    private void boofCalib(){

        CalibrationDetector detector = FactoryPlanarCalibrationTarget.detectorChessboard(new ConfigChessboard(5,7, 30));
        CalibrateStereoPlanar calibratorAlg = new CalibrateStereoPlanar(detector);
        calibratorAlg.configure(true, 2, false);

        for (int i =0; i < leftpics.size(); i++){
            ImageFloat32 imageLeft = ConvertBufferedImage.convertFrom(leftpics.get(i),(ImageFloat32)null);
            ImageFloat32 imageRight = ConvertBufferedImage.convertFrom(rightpics.get(i),(ImageFloat32)null);

            if( !calibratorAlg.addPair(imageLeft, imageRight) )
                System.out.println("Failed to detect target in pair");
        }
        stereoCalib = calibratorAlg.process();

        // TODO show accuracy
        // print out information on its accuracy and errors
        calibratorAlg.printStatistics();
        // save results to a file and print out
        UtilIO.saveXML(stereoCalib, "d:/stereo.xml");
        stereoCalib.print();

        isCalib = true;
        cams.get(left).setIsCalibrated(true);
        cams.get(right).setIsCalibrated(true);
    }

    private void boofDisp(){

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

    public BufferedImage returnFeed(int cam) throws IOException {
        if (cam>=cams.size()){
            return camdisc;
        }
        System.out.println(cams.size());
        Mat i = new Mat();
        try{
            frameread(cam,i);
        }
        catch (Exception e){
            //works to remove problematic false cameras
//            Iterator<Camera> c = cams.iterator();
//
//            while(c.hasNext()){
//                if (!c.next().getCamsource().isOpened()){
//                    cams.remove(c.next());
//                }
//            }
            //cams.get(cams.size()-1).getCamsource().release();
            //cams.remove(cams.size()-1);
            return camdisc;
        }


        return mat2image(i);
    }



    private void initCams(){

        // max 12 capture devices are connected -> this is opening more cameras than expected..
        for (int i = 0;i<12;i++){
            VideoCapture c = new VideoCapture(i);
            try {
                c.open(i);
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (c.isOpened()) {
                cams.add(new Camera(c, this));
                System.out.println("adding new cam" + c.hashCode());
            }
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

    public static BufferedImage joinImages(BufferedImage left,BufferedImage right) {
        int space = 25;
        int w = left.getWidth()+right.getWidth()+space;
        int h = Math.max(left.getHeight(),right.getHeight());
        BufferedImage newImage = new BufferedImage(w,h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = newImage.createGraphics();
        g2.setPaint(Color.WHITE);
        g2.fillRect(0, 0, w, h);
        g2.drawImage(left, null, 0, 0);
        g2.drawImage(right, null, left.getWidth()+space, 0);
        g2.dispose();
        return newImage;
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
