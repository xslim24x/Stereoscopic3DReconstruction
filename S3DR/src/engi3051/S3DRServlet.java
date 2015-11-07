package engi3051;


import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import javax.imageio.*;

/**
 * Created by Slim on 11/5/2015.
 */
@WebServlet(name = "S3DRServlet", urlPatterns = {"/a"})
public class S3DRServlet extends HttpServlet {
    public ArrayList<VideoCapture> cams = new ArrayList<VideoCapture>();
    private String message;

    public void init() throws ServletException
    {
        // Do required initialization
        String opencvpath = "D:\\opencv\\build\\java\\x64\\";
        System.load(opencvpath + Core.NATIVE_LIBRARY_NAME + ".dll");
        initCams();

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


        System.out.println("Servlet running");
        //PrintWriter out = response.getWriter();
        //response.setContentType("text/html");
        response.setContentType("image/jpeg");
        java.io.OutputStream outputStream = response.getOutputStream();


        BufferedImage image;
        int camreq = Integer.parseInt(request.getParameter("cam"));

        if(camreq <= cams.size()) {
            Mat f = new Mat();
            while (true) {
                if (cams.get(camreq).read(f)) {

                    MatOfByte byteMat = new MatOfByte();
                    Imgcodecs.imencode(".jpg",f,byteMat);
                    //System.setProperty("java.io.tmpdir", "C:/temp");
                    ImageIO.setUseCache(false);
                    image = ImageIO.read(new ByteArrayInputStream(byteMat.toArray()));
                    Graphics2D graphics2D = image.createGraphics();
                    graphics2D.dispose();
                    response.setContentType("image/jpeg");
                    ImageIO.write(image,"jpeg",outputStream);
                    outputStream.flush();
                    //Saving File doesnt work..
                    //File savefile = new File("camera"+cams.indexOf(c)+".jpg");
                    //ImageIO.write(image,"jpeg",savefile);
                    //Imgcodecs.imwrite("camera"+cams.indexOf(c)+".jpg",f);
                    System.out.println("OK");
                    break;
                }
            }
        }
        outputStream.close();
        //out.flush();
    }

    public void initCams(){

        for (int i = 0;i<10;i++){
            VideoCapture c = new VideoCapture(i);
            if (c.isOpened())
                cams.add(c);
        }

    }
}
