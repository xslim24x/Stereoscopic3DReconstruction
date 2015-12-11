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
    private String message;
    public ReconstructionSystem rs;

    public void init() throws ServletException
    {
        // Do required initialization
        String opencvpath = "D:\\opencv\\build\\java\\x64\\";
        System.load(opencvpath + Core.NATIVE_LIBRARY_NAME + ".dll");
        rs = new ReconstructionSystem();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


        //PrintWriter out = response.getWriter();
        //response.setContentType("text/html");
        response.setContentType("image/jpeg");
        java.io.OutputStream outputStream = response.getOutputStream();
        BufferedImage image;
        String getcam = request.getParameter("cam");
        String getdisp = request.getParameter("disp");
        String getcap = request.getParameter("cap");
        if (getcam!=null){
            if (getcam.contains("stereo")){
                image = rs.StereoCam();
                response.setContentType("image/jpeg");
                ImageIO.write(image,"jpeg",outputStream);
            }
            else{
                try{
                    int camreq = Integer.parseInt(getcam);
                    image = rs.returnFeed(camreq);
                    ImageIO.write(image,"jpeg",outputStream);
                }
                catch (NumberFormatException e){

                }
            }
            outputStream.flush();

        }
        if (getdisp!=null){
            int disptype = Integer.parseInt(getdisp);
            image = rs.mat2image(rs.normalizedDisp(disptype));
            ImageIO.write(image,"jpeg",outputStream);
            outputStream.flush();
        }
        if (getcap!=null){
            int disptype = Integer.parseInt(getcap);
            rs.reconstruct();
            image = rs.mat2image(rs.normalizedDisp(disptype));
            ImageIO.write(image,"jpeg",outputStream);
            outputStream.flush();
        }
        outputStream.close();
    }
}
