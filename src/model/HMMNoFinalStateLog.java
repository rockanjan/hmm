package model;

import java.util.Random;

import model.param.HMMParamBase;
import model.param.HMMParamLog;
import model.param.HMMParamRegular;

public class HMMNoFinalStateLog extends HMMBase{
	public HMMNoFinalStateLog() {
		super();
		this.hmmType = HMMType.LOG_SCALE;
	}

	public HMMNoFinalStateLog(int nrStates, int nrObs) {
		super();
		this.nrStates = nrStates;
		this.nrObs = nrObs;
		this.hmmType = HMMType.LOG_SCALE;
	}

	public void initializeRandom(Random r) {
		this.param = new HMMParamLog(this);
		this.param.initialize(r);
	}
	
	public void initializeZeros() {
		param = new HMMParamRegular(this);
		param.initializeZeros();
	}
	
	public static void main(String[] args) {
		//check saving and loading model
		int nrStates = 20;
		int nrObs = 50;
		HMMNoFinalStateLog hmm = new HMMNoFinalStateLog(nrStates, nrObs);
		hmm.initializeRandom(new Random());
		HMMParamBase beforeSaving = new HMMParamRegular(hmm);
		beforeSaving.initializeZeros();
		beforeSaving.cloneFrom(hmm.param);
		String fileSaved = hmm.saveModel();
		hmm.param.clear();
		hmm = null;
		
		HMMNoFinalStateLog loaded = new HMMNoFinalStateLog();
		loaded.loadModel(fileSaved);
		if(beforeSaving.equalsExact(loaded.param)) {
			System.out.println("Saved and Loaded models match exactly");
		} else if(beforeSaving.equalsApprox(loaded.param)) {
			System.out.println("Saved and Loaded models match approx");
		} else {
			System.out.println("Saved and Loaded models do not match");
		}
		
	}
}
