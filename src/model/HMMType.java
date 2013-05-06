package model;

public enum HMMType {
	WITH_NO_FINAL_STATE("HMM with no final state"), 
	WITH_FINAL_STATE("HMM with final state"), //with fake final state
	LOG_SCALE("HMM working with log scale"); //log
	 
	private String description;
 
	private HMMType(String d) {
		description = d;
	}
 
	public String getDescription() {
		return description;
	}
}
