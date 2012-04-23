package grabber;

import java.util.Random;


/**
 * @author stas
 *
 */
public final class Particle implements Comparable<Particle> {
	private static final Random rng = new Random();
    private static final float X_STD = 0.5f;
    private static final float Y_STD = 0.5f;
    private static final float A_STD = 0.05f;
	
	private float x;
	private float y;
	private float angle;
	private float weight;
	
    public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}


	public float getAngle() {
		return angle;
	}

	public float getWeight() {
		return weight;
	}

	public void move(int w, int h) {
		x += (float)rng.nextGaussian() * X_STD;
		y += (float)rng.nextGaussian() * Y_STD;
		x = Math.max(0.0f, Math.min((float) w - 1.0f, x));
		y = Math.max(0.0f, Math.min((float) h - 1.0f, y));
		angle += (float)rng.nextGaussian() * A_STD;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	public void normalizeWeight(float factor) {
		weight /= factor;
	}

	@Override
	public int compareTo(Particle o) {
		if (weight > o.weight) {
			return -1;
		}
		if (weight < o.weight) {
			return 1;
		}
		return 0;
	}
	
	public Particle(float x, float y, float angle) {
		this.x = x;
		this.y = y;
		this.angle = angle;
		weight = 0.0f;
	}

}
