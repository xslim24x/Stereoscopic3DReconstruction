package engi3051;

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
    private int left =0,right=1;
    public ArrayList<Camera> cams = new ArrayList<Camera>();


    public ReconstructionSystem(){
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

}
