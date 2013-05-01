package model;

import java.util.Random;

public class HMM {

	public int nrStates;
	public int nrObs;
	HMMParam param;
	
	public HMM(int nrStates, int nrObs) {
		this.nrStates = nrStates;
		this.nrObs = nrObs; 
	}
	
	public void initializeRandom(Random r) {
		param = new HMMParam(nrStates, nrObs);
		param.initialize(r);
	}
	
	public void checkModel() {
		param.check();
	}
	
}
