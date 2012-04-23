package grabber;

import static com.googlecode.javacv.cpp.opencv_core.CV_RGB;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvCircle;
import static com.googlecode.javacv.cpp.opencv_core.cvLine;
import static com.googlecode.javacv.cpp.opencv_core.cvPoint;
import static com.googlecode.javacv.cpp.opencv_core.cvRectangle;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
//import static com.googlecode.javacv.cpp.opencv_core.cvReleaseImage;
import static com.googlecode.javacv.cpp.opencv_core.cvResetImageROI;
import static com.googlecode.javacv.cpp.opencv_core.cvSetImageROI;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RGB2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;

import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_features2d.DMatch;
import com.googlecode.javacv.cpp.opencv_features2d.FlannBasedMatcher;
import com.googlecode.javacv.cpp.opencv_features2d.KeyPoint;
import com.googlecode.javacv.cpp.opencv_features2d.SiftDescriptorExtractor;
import com.googlecode.javacv.cpp.opencv_features2d.SiftFeatureDetector;

/**
 * @author stas
 *
 */
public final class MyParticleFilter {
	/**
     * System Constants
     */
	public final static int radius = 5;
	public final static int PART_NUM = 200;
	public final static int FEATURE_NUM = 2;
	public final static double THRESHOLD = 5.0;
	
	public MyParticleFilter(String path) {
		grabber = new OpenCVFrameGrabber(path);
		states = new ArrayList<Particle>(PART_NUM);
		features = new ArrayList<Observation>(FEATURE_NUM);
	}
	
	public void setP1(CvPoint p1) {
		this.p1 = p1;
	}
	
	public void setP2(CvPoint p2) {
		this.p2 = p2;
		myROI = buildRect();
	}
	
	public IplImage getImage() {
		return image;
	}
	
	public Image getBufferedImage() {
		return image.getBufferedImage();
	}
	
	public CvPoint getP1() {
		return p1;
	}
	
	public CvPoint getP2() {
		return p2;
	}
	
	public void start() {
		try {
			grabber.start();
            image = grabber.grab();
            grey = cvCreateImage(image.cvSize(), IPL_DEPTH_8U, 1);
    		cvCvtColor(image, grey, CV_RGB2GRAY);
		} catch (Exception e) {
			System.out.println("Inside tracker start!");
			e.printStackTrace();
		}
	}
	public void nextFrame() {
        try {
//        	cvReleaseImage(image);
//        	cvReleaseImage(grey);
			image = grabber.grab();
	        grey = cvCreateImage(image.cvSize(), IPL_DEPTH_8U, 1);
			cvCvtColor(image, grey, CV_RGB2GRAY);
		} catch (Exception e) {
			System.out.println("Inside tracker next frame!");
			e.printStackTrace();
		}
	}

	public void initDistribution() {
		// TODO Auto-generated method stub
		CvRect[] symb = getSymbRect();
		CvRect area = getRegion(symb);
		
		for(int i = 0; i < symb.length; i++) {
			CvRect roi = new CvRect(symb[i].x() - area.x(), symb[i].y() - area.y(), symb[i].width(), symb[i].height());
			CvMat descr = new CvMat(null);
			KeyPoint kp = findKeyPoints(roi);
			extractor.compute(grey, kp, descr);
			features.add(new Observation(roi, descr, kp.capacity()));
		}
		
		for(int i = 0; i < PART_NUM; i++) {
			states.add(new Particle(area.x(), area.y(), 0));
		}
	}
	
	public void drawCurrentState() {
		transition();
		likelihood();
		normalize();
		resample();
		drawMostLikelyParticle();
		// TODO Auto-generated method stub		
	}
	
    
	/**
     * Variable for holding current loaded image and video
     */
    private OpenCVFrameGrabber grabber;
    private IplImage image;
    private IplImage grey;
      
    /**
     * Tools for SIFT matching
     */    
	private static final SiftFeatureDetector detector = new SiftFeatureDetector(0.05, 10, 4, 3, -1, 0);
    private static final SiftDescriptorExtractor extractor = new SiftDescriptorExtractor();
    private static final FlannBasedMatcher matcher = new FlannBasedMatcher();

    
    /**
     * Observe variables
     */
    private CvRect myROI;
    private CvPoint p1;
    private CvPoint p2;
    
    /**
     * Tracker parameters
     */
    private ArrayList<Particle> states;
    private ArrayList<Observation> features;
    private ArrayList<Observation> observations;
    
    

	private CvRect buildRect() {
    	int w = Math.abs(p1.x() - p2.x());
    	int h = Math.abs(p1.y() - p2.y());
    	int x = Math.min(p1.x(), p2.x());
    	int y = Math.min(p1.y(), p2.y());
		return new CvRect(x, y, w, h);
	}
	
	private KeyPoint findKeyPoints(CvRect roi) {
		cvSetImageROI(grey, roi);
		KeyPoint kpts = new KeyPoint();
		detector.detect(grey, kpts, null);
		cvResetImageROI(grey);
		return kpts;
	}


	
	private void drawMostLikelyParticle() {
		doMostLikelyEstimate();
		for (Observation f : features) {
			target(f);
			targetCenter(Math.round(f.getTransX(states.get(0))), Math.round(f.getTransY(states.get(0))));
		}
	}

    private void targetCenter(int x, int y) {
    	if(image != null) {
            cvCircle(image, cvPoint(x, y), radius, CV_RGB(250,45,70),1 ,8, 0);
            cvLine(image, cvPoint(x-radius/2, y-radius/2), cvPoint(x+radius/2, y+radius/2), CV_RGB(250,0,0), 1, 8, 0);
            cvLine(image, cvPoint(x-radius/2, y+radius/2), cvPoint(x+radius/2, y-radius/2), CV_RGB(250,0,0), 1, 8, 0);
    	}
    }
    
    private void target(Observation ob) {
    	if(image != null && ob.getMle() != null) {
            cvRectangle(image, ob.getP1(), ob.getP2(), CV_RGB(250,0,0),1 ,8, 0);
    	}
    }
	private void doMostLikelyEstimate() {
		Particle s = states.get(0);
		double dist = 0.0;
		double min = 9000.0;
		for(Observation f : features) {
			f.setMle(null);
			min = 9000.0f;
			for(Observation o : observations) {
				dist = Math.hypot(f.getTransX(s) - o.getX(), f.getTransY(s) - o.getY());
				if(dist < THRESHOLD) {
					if (dist < min) {
						f.setMle(o.getElement());
						min = dist;
					}
				}
				
			}
		}
	}

	private void resample() {
		ArrayList<Particle> newStates = new ArrayList<Particle>(PART_NUM);
		Collections.sort(states);
		int k = 0;
		for(int i = 0; i < PART_NUM; i++) {
			int np = (int) Math.round(states.get(i).getWeight() * PART_NUM);
			for(int j = 0; j < np; j++) {
				newStates.add(states.get(i));
				k++;
			}
			if(k == PART_NUM) {
				break;
			}
		}
		while(k < PART_NUM) {
			newStates.add(states.get(0));
			k++;
		}
		states = newStates;
	}
	
	
	private void normalize() {
		float sum = 0.0f;
		for (Particle s : states) {
			sum += s.getWeight();
		}
		for (Particle s : states) {
			s.normalizeWeight(sum);
		}
	}
	
	
	private void likelihood() {
		CvRect[] symbols = getSymbRectFast();
		observations = new ArrayList<Observation>(symbols.length);
		for(CvRect r : symbols) {
			KeyPoint kp = findKeyPoints(r);
			CvMat descr = new CvMat(null);
			extractor.compute(grey, kp, descr);
			observations.add(new Observation(r, descr, kp.capacity()));
		}
		int count = 0;
		double dist = 0.0f;
		for(Particle s : states) {
			count = 0;
			for(Observation f : features) {
				for(Observation o : observations) {
					dist = Math.hypot(f.getTransX(s) - o.getX(), f.getTransY(s) - o.getY());
					if(dist < THRESHOLD && match(f, o)) {
						count++;
					}
				}
			}
			s.setWeight((float)Math.exp(count));
		}
	}
	

	private boolean match(Observation f, Observation o) {
		DMatch dm = new DMatch();
		matcher.match(f.getDescriptors(), o.getDescriptors(), dm, null);
	    int n = dm.capacity();
	    DMatch[] matches = new DMatch[n];
	    double min_dist = 1000;
	    double max_dist = 0;
	    for (int i = 0; i < n; i++) {
	        matches[i] = new DMatch(dm.position(i));
	        double dist = matches[i].distance();
	        if( dist < min_dist ) {
	        	min_dist = dist;
	        }
	        if( dist > max_dist ) {
	        	max_dist = dist;
	        }
	    }
	    return min_dist < 100.0;
	}

	private void transition() {
		for(Particle p : states) {
			p.move(image.width(), image.height());
		}
	}

	private CvRect getRegion(CvRect[] symb) {
		return myROI;
	}

	private CvRect[] getSymbRect() {
		CvRect[] rects = new CvRect[FEATURE_NUM];
		int diff =(int)Math.round(((double) myROI.width()) / FEATURE_NUM);
		for(int i = 0; i < FEATURE_NUM; i++) {
			CvRect roi = new CvRect(myROI.x() + diff * i, myROI.y(), diff, myROI.height());
			rects[i] = roi;
		}
		return rects;
	}
	
	private CvRect[] getSymbRectFast() {
		return getSymbRect();
	}
}
