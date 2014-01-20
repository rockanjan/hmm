package model;

public enum HMMType {
	REGULAR("HMM working in regular scale, no log"), 
	LOG_SCALE("HMM working with log scale"); //log
	 
	private String description;
 
	private HMMType(String d) {
		description = d;
	}
 
	public String getDescription() {
		return description;
	}
}
