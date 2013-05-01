package model;

import java.util.Random;

public class HMM {

	int nrStates;
	int nrObs;
	HMMParam param;
	
	public HMM(int nrStates, int nrObs) {
		this.nrStates = nrStates;
		this.nrObs = nrObs; 
	}
	
	public void initializeRandom(Random r) {
		param = new HMMParam(nrStates, nrObs);
		param.initialize(r);
	}
	
}
