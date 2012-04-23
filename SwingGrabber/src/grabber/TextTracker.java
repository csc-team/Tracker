package grabber;

import javax.swing.*;

import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import static com.googlecode.javacv.cpp.opencv_core.*;


/**
 * @author stas
 *
 */

@SuppressWarnings("serial")
public final class TextTracker extends JFrame {

	private final JFileChooser fileChooser = new JFileChooser();

	/**
     * Component for displaying the image
     */
    private final JLabel imageView = new JLabel();

    /**
     *  Tracker instance
     */  
    private MyParticleFilter tracker = null;

    
    /**
     * @throws HeadlessException
     */
    private TextTracker() throws HeadlessException {
        super("Text Detector");

        //
        // Define Mouse events
        //
        
        final MouseListener mouseAction = new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				super.mousePressed(e);
				tracker.setP1(new CvPoint(e.getX(), e.getY()));
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				super.mouseReleased(e);
				tracker.setP2(new CvPoint(e.getX(), e.getY()));
				IplImage tmp = tracker.getImage().clone();
			    cvRectangle(tmp, tracker.getP1(), tracker.getP2(), CV_RGB(200,0,125), 1, 8, 0 );
                imageView.setIcon(new ImageIcon(tmp.getBufferedImage()));
				System.out.println("mouseReleased " + e.getX() + " ; "+ e.getY());
			}        	
        };
        
        
        final MouseMotionListener mouseMotion = new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				// TODO Auto-generated method stub
				super.mouseDragged(e);
				IplImage tmp = tracker.getImage().clone();
			    cvRectangle(tmp, tracker.getP1(), cvPoint(e.getX(), e.getY()), CV_RGB(255,255,255), 1, 8, 0 );
                imageView.setIcon(new ImageIcon(tmp.getBufferedImage()));
			}
        	
        };
        
        
        /**
         * 		Define actions
         */ 

        // Action performed when "Open Video" button is pressed
        final Action openImageAction = new AbstractAction("Open Video") {

            @Override
            public void actionPerformed(final ActionEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    // Load video and update display. If video was not loaded do nothing.
                    tracker = instanceTracker();
                    tracker.start();
                    imageView.setIcon(new ImageIcon(tracker.getBufferedImage()));
                }
                finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        
        // Action performed when "Next Frame" button is pressed
        final Action nextFrameAction = new AbstractAction("Next Frame") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    // Load new frame and update display. 
                	tracker.nextFrame();
                    imageView.setIcon(new ImageIcon(tracker.getBufferedImage()));
                    System.out.println("Next!");
                }
                finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        
        
        // Action performed when "Create Tracker" button is pressed
        final Action createTrackerAction = new AbstractAction("Create Tracker") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    tracker.initDistribution();
                    System.out.println("Tracker created!");
                } 
                finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        
        
        // Action performed when "Track!" button is pressed
        final Action trackAction = new AbstractAction("Track!") {

            @Override
            public void actionPerformed(final ActionEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    // Load image and update display. If new image was not loaded do nothing.               	
                    System.out.println("Track!");
                    tracker.drawCurrentState();
                    imageView.setIcon(new ImageIcon(tracker.getBufferedImage()));
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
        buttonsPanel.add(new JButton(nextFrameAction));
        buttonsPanel.add(new JButton(createTrackerAction));
        buttonsPanel.add(new JButton(trackAction));

        // Layout frame contents

        // Action buttons on the left
        final JPanel leftPane = new JPanel();
        leftPane.add(buttonsPanel);
        add(leftPane, BorderLayout.WEST);

        // Image display in the center
        final JScrollPane imageScrollPane = new JScrollPane(imageView);
        imageScrollPane.addMouseListener(mouseAction);
        imageScrollPane.addMouseMotionListener(mouseMotion);
        imageScrollPane.setPreferredSize(new Dimension(320, 240));
        add(imageScrollPane, BorderLayout.CENTER); 

    }

	/**
     * Ask user for location and open new video.
     *
     * @return Opened video or {@code null} if video was not loaded.
     */
    private MyParticleFilter instanceTracker() {
        // Ask user for the location of the video file
        if (fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        // Load the video
        final String path = fileChooser.getSelectedFile().getAbsolutePath();
        final MyParticleFilter trk = new MyParticleFilter(path);
        return trk;
    }
    
    public static void main(final String[] args) {
    	
	    final TextTracker frame = new TextTracker();
	    frame.pack();
	    // Mark for display in the center of the screen
	    frame.setLocationRelativeTo(null);
	    // Exit application when frame is closed.
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setVisible(true);

    }
}
