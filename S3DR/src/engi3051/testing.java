package engi3051;

import org.opencv.core.*;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.*;
import org.opencv.imgcodecs.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.*;
import org.opencv.calib3d.*;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
        JFrame f = new swingwindow();

    }

    public static class swingwindow extends JFrame implements ActionListener {
        private JButton startcam, stopcam, capture;
        private JLabel lblLeft, lblRight, lpic, rpic;
        private JTextField lcam, rcam;
        private JPanel lpanel, rpanel,bpanel;

        public swingwindow(){
            setTitle("S3DR Tester");
            setSize(1200,800);
            setLocation(400,200);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            lpanel = new JPanel();
            lpanel.setLayout(new GridLayout());
            lblLeft = new JLabel("Left");
            lpanel.add(lblLeft);
            lcam = new JTextField("0");
            lpanel.add(lcam);
            lpic = new JLabel(new ImageIcon());


            rpanel = new JPanel();
            rpanel.setLayout(new BoxLayout(rpanel,BoxLayout.PAGE_AXIS));
            lblRight = new JLabel("Right");
            //lcam =



            setVisible(true);
        }

        @Override
        public void actionPerformed(ActionEvent e) {

        }
    }

}
