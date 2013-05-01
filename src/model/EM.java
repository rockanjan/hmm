package model;

import java.text.DecimalFormat;

import util.Timing;

import corpus.Corpus;
import corpus.Instance;

public class EM {

	int numIter;
	Corpus c;
	HMM model;
	
	double oldLL = 0;
	double LL = 0;
	
	public EM(int numIter, Corpus c, HMM model) {
		this.numIter = numIter;
		this.c = c;
		this.model = model;
	}
	
	
	
	public void eStep() {
		HMMParam expectedCounts = new HMMParam(model.nrStates, model.nrObs);
		
		for(int n=0; n<c.trainInstanceList.size(); n++) {
			Instance instance = c.trainInstanceList.get(n);
			instance.doInference(model);
			
		}
	}
	
	public void mStep() {
		
	}



	public void start() {
		System.out.println("Starting EM");
		DecimalFormat df = new DecimalFormat("#.###");
		Timing totalEMTime = new Timing();
		totalEMTime.start();
		Timing eStepTime = new Timing();
		for (int iterCount=0 ; iterCount < numIter; iterCount++) {
			LL = 0;
			//e-step
			eStepTime.start();
			eStep();
			System.out.format("LL %2.10f \t E-step time %s\n", LL, eStepTime.stop());
			//m-step
			mStep();
			oldLL = LL;
		}
		System.out.println("Total EM Time : " + totalEMTime.stop());
		
	}
}
