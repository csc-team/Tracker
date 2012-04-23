package grabber;

import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;


/**
 * @author stas
 *
 */
public final class Observation {	

	private final float xc;
	private final float yc;
	private final CvRect element;
	private final int kpts;
	private final CvMat descriptors;
	private CvRect mle;
	
	
	public CvRect getElement() {
		return element;
	}
	
	public CvRect getMle() {
		return mle;
	}
	
	public CvPoint getP1() {
		if (mle == null) {
			return null;
		}
		return new CvPoint(mle.x(), mle.y());
	}
	
	public CvPoint getP2() {
		if (mle == null) {
			return null;
		}
		return new CvPoint(mle.x() + mle.width(), mle.y() + mle.height());
	}
	
	public void setMle(CvRect mle) {
		this.mle = mle;
	}
	
	public float getX() {
		return xc;
	}
	
	public float getY() {
		return yc;
	}
	
	public float getTransX(Particle s) {
		float a = s.getAngle();
		return (float)(Math.cos(a) * xc - Math.sin(a) * yc + s.getX());
	}
	
	
	public float getTransY(Particle s) {
		float a = s.getAngle();
		return (float)(Math.cos(a) * yc + Math.sin(a) * xc + s.getY());
	}


	public CvMat getDescriptors() {
		return descriptors;
	}

	
	public Observation(CvRect element, CvMat descriptors, int kpts) {
		super();
		this.element = element;
		xc = (float)element.x() + element.width() / 2.0f;
		yc = (float)element.y() + element.height() / 2.0f;
		this.kpts = kpts;
		this.descriptors = descriptors;
	}

	public int getKpts() {
		return kpts;
	}
	
	
}
