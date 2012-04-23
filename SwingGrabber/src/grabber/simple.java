package grabber;

public class simple {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		double count = 0.0;
		for(int i = 0; i < 1000; i++) {
			for(int j = 0; j < 1000; j++) {
				for (int l = 0; l < 1000; l++) {
					count = 0;
					double u = Math.cos(0.6) + count;
					count += u;
				}
			}
		}
		System.out.println(count);
		// TODO Auto-generated method stub

	}

}
