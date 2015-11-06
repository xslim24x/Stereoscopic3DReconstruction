package engi3051;

import org.opencv.core.*;
import org.opencv.imgcodecs.*;
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

        Mat frame;
        for(VideoCapture c : cams) {
            frame = new Mat();
            while (true) {
                if (c.read(frame)) {
                    System.out.println("Frame Obtained");
                    System.out.println("Captured Frame Width " +
                            frame.width() + " Height " + frame.height());
                    Imgcodecs.imwrite("camera"+cams.indexOf(c)+".jpg", frame);
                    System.out.println("OK");
                    break;
                }
            }
        }



        Iterator<VideoCapture> c = cams.iterator();
        while(c.hasNext()){
            c.next().release();
            c.remove();
        }

    }
}
