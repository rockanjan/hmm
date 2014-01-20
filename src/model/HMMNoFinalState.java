package model;

import java.util.Random;

import model.param.HMMParamBase;
import model.param.HMMParamRegular;

public class HMMNoFinalState extends HMMBase{
	public HMMNoFinalState() {
		super();
		this.hmmType = HMMType.REGULAR;
	}

	public HMMNoFinalState(int nrStates, int nrObs) {
		super();
		this.nrStates = nrStates;
		this.nrObs = nrObs;
		this.hmmType = HMMType.REGULAR;
	}

	public void initializeRandom(Random r) {
		this.param = new HMMParamRegular(this);
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
		HMMNoFinalState hmm = new HMMNoFinalState(nrStates, nrObs);
		hmm.initializeRandom(new Random());
		HMMParamBase beforeSaving = new HMMParamRegular(hmm);
		beforeSaving.initializeZeros();
		beforeSaving.cloneFrom(hmm.param);
		String fileSaved = hmm.saveModel();
		hmm.param.clear();
		hmm = null;
		
		HMMNoFinalState loaded = new HMMNoFinalState();
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
