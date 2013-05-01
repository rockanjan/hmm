package util;

public class Timing {
	private long startT;
	private long stopT;
	
	public void start() {
		startT = System.currentTimeMillis();
	}
	
	/*
	 * return time elapsed since start() called
	 */
	public String stop() {
		stopT = System.currentTimeMillis();
		String elapsed = (1.0 * (stopT - startT) / 1000 / 60) + " minutes";
		stopT = 0; startT = 0;
		return elapsed;
		
	}
	
}
