package grabber;

import javax.swing.*;



import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_features2d.DMatch;
import com.googlecode.javacv.cpp.opencv_features2d.FlannBasedMatcher;
import com.googlecode.javacv.cpp.opencv_features2d.KeyPoint;
import com.googlecode.javacv.cpp.opencv_features2d.SiftDescriptorExtractor;
import com.googlecode.javacv.cpp.opencv_features2d.SiftFeatureDetector;
//import com.googlecode.javacv.cpp.opencv_features2d.SurfDescriptorExtractor;
//import com.googlecode.javacv.cpp.opencv_features2d.SurfFeatureDetector;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
//import javax.swing.event.MouseInputAdapter;

import static com.googlecode.javacv.cpp.opencv_core.*;
//import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
//import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2RGB;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

/**
 * @author stas
 *
 */

@SuppressWarnings("serial")
public final class VideoGrabber extends JFrame {

	private final JFileChooser fileChooser = new JFileChooser();

    /**
     * Component for displaying the image
     */
    private final JLabel imageView = new JLabel();
    /**
     * Variable for holding loaded image and video
     */
    private OpenCVFrameGrabber grabber = null;
    private IplImage image = null;
    private IplImage gray = null;
    
    
    /**
     * Some stuff
     */    
	private final SiftFeatureDetector detector = new SiftFeatureDetector(0.05, 10, 4, 3, -1, 0);
    private final SiftDescriptorExtractor extractor = new SiftDescriptorExtractor();
//	private final SurfFeatureDetector detector = new SurfFeatureDetector(1000.0, 3, 4, true);
//    private final SurfDescriptorExtractor extractor = new SurfDescriptorExtractor();
    private final FlannBasedMatcher matcher = new FlannBasedMatcher();

    /**
     *  Some stuff for Candidate Region definition
     */  
    private CvMat descriptors = null;
    private CvRect myROI = null;
    private CvPoint p1;
    private CvPoint p2;
    
    /**
     *  Some constants
     */

    private final int radius = 5;

    private void target(int x, int y) {
    	if(image != null) {
            cvCircle(image, cvPoint(x, y), radius, CV_RGB(250,0,0),1 ,8, 0);
            cvLine(image, cvPoint(x-radius/2, y-radius/2), cvPoint(x+radius/2, y+radius/2), CV_RGB(250,0,0), 1, 8, 0);
            cvLine(image, cvPoint(x-radius/2, y+radius/2), cvPoint(x+radius/2, y-radius/2), CV_RGB(250,0,0), 1, 8, 0);
    	}
    }
    
    
    private VideoGrabber() throws HeadlessException {
        super("SIFT Matcher");


        //
        // Define actions
        //
        
        final MouseListener mouseAction = new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				super.mousePressed(e);
				p1 = new CvPoint(e.getX(), e.getY());
				
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				super.mouseReleased(e);
				p2 = new CvPoint(e.getX(), e.getY());
				IplImage tmp = image.clone();
				myROI = buildRect(p1 , p2);
			    cvRectangle( tmp, p1, p2, CV_RGB(44,255,44), 1, 8, 0 );
                imageView.setIcon(new ImageIcon(tmp.getBufferedImage()));
				System.out.println("mouseReleased " + e.getX() + " ; "+ e.getY());
			}


			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				super.mouseClicked(e);
				target(e.getX(), e.getY());
                imageView.setIcon(new ImageIcon(image.getBufferedImage()));
				System.out.println("Click" + e.getX() + " ; "+ e.getY());
			}
        	
        };
        
        
        final MouseMotionListener mouseMotion = new MouseMotionAdapter() {


			@Override
			public void mouseDragged(MouseEvent e) {
				// TODO Auto-generated method stub
				super.mouseDragged(e);
				IplImage tmp = image.clone();
			    cvRectangle(tmp, p1, cvPoint(e.getX(), e.getY()), CV_RGB(255,255,255), 1, 8, 0 );
                imageView.setIcon(new ImageIcon(tmp.getBufferedImage()));
			}
        	
        };
        
        


        // Action performed when "Open Video" button is pressed
        final Action openImageAction = new AbstractAction("Open Video") {

            @Override
            public void actionPerformed(final ActionEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    // Load image and update display. If new image was not loaded do nothing.
                    grabber = openGrabber();
                    grabber.start();
                    image = grabber.grab();
                    gray = cvCreateImage(image.cvSize(), IPL_DEPTH_8U, 1);
            		cvCvtColor(image, gray, CV_RGB2GRAY);
                    imageView.setIcon(new ImageIcon(image.getBufferedImage()));

                } 
                catch(Exception ex) {
                	
                }
                finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        
        // Action performed when "Next Frame" button is pressed
        final Action nextImageAction = new AbstractAction("Next Frame") {

            @Override
            public void actionPerformed(final ActionEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    // Load image and update display. If new image was not loaded do nothing.
                    image = grabber.grab();
                    gray = cvCreateImage(image.cvSize(), IPL_DEPTH_8U, 1);
            		cvCvtColor(image, gray, CV_RGB2GRAY);
                    imageView.setIcon(new ImageIcon(image.getBufferedImage()));
                    System.out.println("Next!");

                } 
                catch(Exception ex) {
                	
                }
                finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        
        
        // Action performed when "SIFT detection" button is pressed
        final Action detectSIFTAction = new AbstractAction("SIFT Detection") {

            @Override
            public void actionPerformed(final ActionEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    // Load image and update display. If new image was not loaded do nothing.
                	
                    detectSift();
                    imageView.setIcon(new ImageIcon(image.getBufferedImage()));
                } 
                finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        
        
        // Action performed when "SIFT matching" button is pressed
        final Action matchingSIFTAction = new AbstractAction("SIFT Matching") {

            @Override
            public void actionPerformed(final ActionEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    // Load image and update display. If new image was not loaded do nothing.
                	
                    System.out.println("Match!");
                    matchSift();
                    imageView.setIcon(new ImageIcon(image.getBufferedImage()));
                } 
                finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };


        //
        // Create UI
        //

        // Create button panel
        final JPanel buttonsPanel = new JPanel(new GridLayout(0, 1, 0, 5));
        buttonsPanel.add(new JButton(openImageAction));
        buttonsPanel.add(new JButton(nextImageAction));
        buttonsPanel.add(new JButton(detectSIFTAction));
        buttonsPanel.add(new JButton(matchingSIFTAction));

        // Layout frame contents

        // Action buttons on the left
        final JPanel leftPane = new JPanel();
        leftPane.add(buttonsPanel);
        add(leftPane, BorderLayout.WEST);

        // Image display in the center
        final JScrollPane imageScrollPane = new JScrollPane(imageView);
        imageScrollPane.addMouseListener(mouseAction);
        imageScrollPane.addMouseMotionListener(mouseMotion);
        imageScrollPane.setPreferredSize(new Dimension(200, 200));
        add(imageScrollPane, BorderLayout.CENTER); 

    }


    private void matchSift() {
    	if(myROI == null) {
    		return;
    	}
		CvMat descriptors2 = new CvMat(null);
		KeyPoint kp = findKeyPoints();
		extractor.compute(gray, kp, descriptors2);
		DMatch matches = new DMatch();
		matcher.match(descriptors, descriptors2, matches, null);
//		System.out.println("Descriptors1: " + descriptors.length());
		System.out.println("KeyPoints2: " + kp.capacity());
		System.out.println("Descriptors2: " + descriptors2.capacity());
		System.out.println("Matches: " + matches.capacity());
//		System.out.println("Procent: " + matches.capacity() / descriptors.capacity());
	    // Convert keyPoints to an array and draw
	    int n = matches.capacity();
	    DMatch[] dm = new DMatch[n];
	    double min_dist = 1000;
	    double max_dist = 0;
	    for (int i = 0; i < n; i++) {
	        dm[i] = new DMatch(matches.position(i));
	        double dist = dm[i].distance();
	        if( dist < min_dist ) {
	        	min_dist = dist;
	        }
	        if( dist > max_dist ) {
	        	max_dist = dist;
	        }
	    }
	    System.out.println("Min distance:  " + min_dist);
	    System.out.println("Max distance:  " + max_dist);
	    int count = 0;
	    for(int i = 0; i < n; i++) {
	    	if(dm[i].distance() < 2 * min_dist) {
	    		count++;
	    	}
	    }
	    System.out.println("Amount of Good Feature:  " + count);

    }


	private KeyPoint findKeyPoints() {
		// TODO Auto-generated method stub
		cvSetImageROI(gray, myROI);
		KeyPoint kpts = new KeyPoint();
		detector.detect(gray, kpts, null);
		cvResetImageROI(gray);
		return kpts;
	}


	private CvRect buildRect(CvPoint p1, CvPoint p2) {
		// TODO Auto-generated method stub
    	int w = Math.abs(p1.x() - p2.x());
    	int h = Math.abs(p1.y() - p2.y());
    	int x = Math.min(p1.x(), p2.x());
    	int y = Math.min(p1.y(), p2.y());
		return new CvRect(x, y, w, h);
	}


	/**
     * Ask user for location and open new video.
     *
     * @return Opened video or {@code null} if video was not loaded.
     */
    private OpenCVFrameGrabber openGrabber() {

        // Ask user for the location of the image file
        if (fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        // Load the image
        final String path = fileChooser.getSelectedFile().getAbsolutePath();
        final OpenCVFrameGrabber newGrabber = new OpenCVFrameGrabber(path);
        return newGrabber;
    }

	private void detectSift() {
		if(myROI == null) {
			return;
		}
		KeyPoint kp = findKeyPoints();
		descriptors = new CvMat(null);
		extractor.compute(gray, kp, descriptors);
		System.out.println("KeyPoints: " + kp.capacity());
/*	    System.out.println("keyPoints: " + kpts.capacity());
	    // Convert keyPoints to an array and draw
	    int n = kpts.capacity();
	    KeyPoint[] points = new KeyPoint[n];
	    for (int i = 0; i < n; i++) {
	        points[i] = new KeyPoint(kpts.position(i));
	        int x = (int)Math.round(points[i].pt_x());
	        int y = (int)Math.round(points[i].pt_y());
	        target(x, y);
	    }*/

	}
    
    
    
    public static void main(final String[] args) {
    	
	    final VideoGrabber frame = new VideoGrabber();
	    frame.pack();
	    // Mark for display in the center of the screen
	    frame.setLocationRelativeTo(null);
	    // Exit application when frame is closed.
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setVisible(true);

    }
}

