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
//servlet to organize and orchastrate communication between server and client webapp
@WebServlet(name = "S3DRServlet", urlPatterns = {"/a"})
public class S3DRServlet extends HttpServlet {
    public ReconstructionSystem rs;

    //initilization method
    public void init() throws ServletException
    {
        // Do required initialization; in case maven dependeces arent set up
        String opencvpath = "D:\\opencv\\build\\java\\x64\\";
        System.load(opencvpath + Core.NATIVE_LIBRARY_NAME + ".dll");
        rs = new ReconstructionSystem();
        try {
            //wait 1 sec to ensure cameras/everything else is ok
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        java.io.OutputStream outputStream = response.getOutputStream();
        BufferedImage image;
        String getcam = request.getParameter("cam");
        String getdisp = request.getParameter("disp");
        String getcap = request.getParameter("cap");
        String getaction = request.getParameter("action");
        //todo refactor this all to one parameter:action
        if (getcam!=null){
            // left and right feed
            if (getcam.contains("stereo")){
                image = rs.StereoCam();
                response.setContentType("image/jpeg");
                ImageIO.write(image,"jpeg",outputStream);
            }
            //show boofcv disparity/pointcloud
            else if (getcam.contains("disp")){
                rs.boofDisp();
            }
            //get individual feeds
            else{
                try{
                    int camreq = Integer.parseInt(getcam);
                    image = rs.returnFeed(camreq);
                    response.setContentType("image/jpeg");
                    ImageIO.write(image,"jpeg",outputStream);
                }
                catch (NumberFormatException e){

                }
            }
            outputStream.flush();

        }
        //opencv disparity
        if (getdisp!=null){
            int disptype = Integer.parseInt(getdisp);
            image = rs.mat2image(rs.normalizedDisp(disptype));
            response.setContentType("image/jpeg");
            ImageIO.write(image,"jpeg",outputStream);
        }
        //opencv pointcloud
        if (getcap!=null){
            int disptype = Integer.parseInt(getcap);
            rs.reconstruct();
            image = rs.mat2image(rs.normalizedDisp(disptype));
            response.setContentType("image/jpeg");
            ImageIO.write(image,"jpeg",outputStream);
        }
        if (getaction!=null){
            if (getaction.contains("pointcloud")){
                rs.boofDisp();
            }

        }
        outputStream.flush();
        outputStream.close();
    }
}
