package engi3051;

import boofcv.abst.distort.FDistort;
import boofcv.abst.feature.disparity.StereoDisparity;
import boofcv.abst.fiducial.calib.ConfigChessboard;
import boofcv.abst.geo.calibration.CalibrateStereoPlanar;
import boofcv.abst.geo.calibration.CalibrationDetector;
import boofcv.alg.depth.VisualDepthOps;
import boofcv.alg.distort.ImageDistort;
import boofcv.alg.geo.PerspectiveOps;
import boofcv.alg.geo.RectifyImageOps;
import boofcv.alg.geo.rectify.RectifyCalibrated;
import boofcv.core.image.border.BorderType;
import boofcv.factory.calib.FactoryPlanarCalibrationTarget;
import boofcv.factory.feature.disparity.DisparityAlgorithms;
import boofcv.factory.feature.disparity.FactoryStereoDisparity;
import boofcv.gui.d3.PointCloudViewer;
import boofcv.gui.image.ShowImages;
import boofcv.gui.image.VisualizeImageData;
import boofcv.io.UtilIO;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.calib.StereoParameters;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageUInt8;
import georegression.geometry.GeometryMath_F64;
import georegression.struct.point.Point3D_F64;
import georegression.struct.se.Se3_F64;
import org.ejml.data.DenseMatrix64F;
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

    //TODO Add calibration reading
    //StereoParameters param = UtilIO.loadXML(calibDir , "stereo.xml");

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
        try{
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
        }
        catch (Exception e){

        }
        finally {
            return joinImages(returnFeed(left),returnFeed(right));
        }
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

    public void boofDisp() throws IOException {
        double scale = 0.5;

        int minDisparity = 0;
        int maxDisparity = 240;
        int rangeDisparity = maxDisparity-minDisparity;

        Mat lraw = new Mat();
        Mat rraw = new Mat();
        cams.get(left).rawFrame(lraw);
        cams.get(right).rawFrame(rraw);

        //testing purposes
        StereoParameters stereoCalib = UtilIO.loadXML("d:/stereo.xml");

        ImageUInt8 distLeft = ConvertBufferedImage.convertFrom(mat2image(lraw), (ImageUInt8) null);
        ImageUInt8 distRight = ConvertBufferedImage.convertFrom(mat2image(rraw),(ImageUInt8)null);

        // re-scale input images
        ImageUInt8 scaledLeft = new ImageUInt8((int)(distLeft.width*scale),(int)(distLeft.height*scale));
        ImageUInt8 scaledRight = new ImageUInt8((int)(distRight.width*scale),(int)(distRight.height*scale));

        new FDistort(distLeft,scaledLeft).scaleExt().apply();
        new FDistort(distRight,scaledRight).scaleExt().apply();

        // Don't forget to adjust camera parameters for the change in scale!
        PerspectiveOps.scaleIntrinsic(stereoCalib.left, scale);
        PerspectiveOps.scaleIntrinsic(stereoCalib.right,scale);

        // rectify images and compute disparity
        ImageUInt8 rectLeft = new ImageUInt8(scaledLeft.width,scaledLeft.height);
        ImageUInt8 rectRight = new ImageUInt8(scaledRight.width,scaledRight.height);

        RectifyCalibrated rectAlg = rectify(scaledLeft,scaledRight,stereoCalib,rectLeft,rectRight);


        StereoDisparity<ImageUInt8,ImageFloat32> disparityAlg =
                FactoryStereoDisparity.regionSubpixelWta(DisparityAlgorithms.RECT,
                        minDisparity, maxDisparity, 3, 3, 25, 1, 0.2, ImageUInt8.class);

        // process and return the results
        disparityAlg.process(rectLeft,rectRight);

        ImageFloat32 disparity = disparityAlg.getDisparity();

        // ------------- Convert disparity image into a 3D point cloud

        // The point cloud will be in the left cameras reference frame
        DenseMatrix64F rectK = rectAlg.getCalibrationMatrix();
        DenseMatrix64F rectR = rectAlg.getRectifiedRotation();

        // used to display the point cloud
        PointCloudViewer viewer = new PointCloudViewer(rectK, 10);

        viewer.setPreferredSize(new Dimension(rectLeft.width,rectLeft.height));

        // extract intrinsic parameters from rectified camera
        double baseline = stereoCalib.getBaseline();
        double fx = rectK.get(0,0);
        double fy = rectK.get(1,1);
        double cx = rectK.get(0,2);
        double cy = rectK.get(1,2);

        // Iterate through each pixel in disparity image and compute its 3D coordinate
        Point3D_F64 pointRect = new Point3D_F64();
        Point3D_F64 pointLeft = new Point3D_F64();

        ArrayList<String>cloudexp = new ArrayList<String>();
        String line;

        for( int y = 0; y < disparity.height; y++ ) {
            for( int x = 0; x < disparity.width; x++ ) {
                double d = disparity.unsafe_get(x,y) + minDisparity;

                // skip over pixels were no correspondence was found
                if( d >= rangeDisparity )
                    continue;

                // Coordinate in rectified camera frame
                pointRect.z = baseline*fx/d;
                pointRect.x = pointRect.z*(x - cx)/fx;
                pointRect.y = pointRect.z*(y - cy)/fy;

                // rotate into the original left camera frame
                GeometryMath_F64.multTran(rectR, pointRect, pointLeft);

                // add pixel to the view for display purposes and sets its gray scale value
                int v = rectLeft.unsafe_get(x, y);
                viewer.addPoint(pointLeft.x, pointLeft.y, pointLeft.z, v << 16 | v << 8 | v);
                double grayv = ((double)v)/255;
                line = pointLeft.x + " " + pointLeft.y + " " + pointLeft.z + " " + grayv;
                cloudexp.add(line);
            }
        }

        // display the results.  Click and drag to change point cloud camera
        BufferedImage visualized = VisualizeImageData.disparity(disparity, null,minDisparity, maxDisparity,0);
        ShowImages.showWindow(viewer,"point");
        ShowImages.showWindow(rectLeft,"rect left");
        //saveCloud(cloudexp);

    }

    private void saveCloud(ArrayList<String> lines){

        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileOutputStream(new File("d:/pointcloud.obj"),false));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        /*
        pw.println("# .PCD v.7 - Point Cloud Data file format");
        pw.println("VERSION .7");
        pw.println("FIELDS x y z rgb");
        pw.println("SIZE 4 4 4 4");
        pw.println("TYPE F F F F");
        pw.println("COUNT 1 1 1 1");
        pw.println("WIDTH "+lines.size());
        pw.println("HEIGHT 1");
        pw.println("VIEWPOINT 0 0 0 1 0 0 0");
        pw.println("POINTS "+lines.size());
        pw.println("DATA ascii");*/

        for (String s:lines){
            pw.println("v "+ s);
        }
        pw.close();
    }



    public static RectifyCalibrated rectify( ImageUInt8 origLeft , ImageUInt8 origRight ,
                                             StereoParameters param ,
                                             ImageUInt8 rectLeft , ImageUInt8 rectRight )
    {
        // Compute rectification
        RectifyCalibrated rectifyAlg = RectifyImageOps.createCalibrated();
        Se3_F64 leftToRight = param.getRightToLeft().invert(null);

        // original camera calibration matrices
        DenseMatrix64F K1 = PerspectiveOps.calibrationMatrix(param.getLeft(), null);
        DenseMatrix64F K2 = PerspectiveOps.calibrationMatrix(param.getRight(), null);

        rectifyAlg.process(K1,new Se3_F64(),K2,leftToRight);

        // rectification matrix for each image
        DenseMatrix64F rect1 = rectifyAlg.getRect1();
        DenseMatrix64F rect2 = rectifyAlg.getRect2();
        // New calibration matrix,
        DenseMatrix64F rectK = rectifyAlg.getCalibrationMatrix();

        // Adjust the rectification to make the view area more useful
        RectifyImageOps.allInsideLeft(param.left, rect1, rect2, rectK);

        // undistorted and rectify images
        ImageDistort<ImageUInt8,ImageUInt8> imageDistortLeft =
                RectifyImageOps.rectifyImage(param.getLeft(), rect1, BorderType.SKIP, ImageUInt8.class);
        ImageDistort<ImageUInt8,ImageUInt8> imageDistortRight =
                RectifyImageOps.rectifyImage(param.getRight(), rect2, BorderType.SKIP, ImageUInt8.class);

        imageDistortLeft.apply(origLeft, rectLeft);
        imageDistortRight.apply(origRight, rectRight);

        return rectifyAlg;
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
