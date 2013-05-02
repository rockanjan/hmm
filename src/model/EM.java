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
	
	HMMParam expectedCounts;
	
	public EM(int numIter, Corpus c, HMM model) {
		this.numIter = numIter;
		this.c = c;
		this.model = model;
	}
	
	public void eStep() {
		expectedCounts = new HMMParam(model.nrStates, model.nrObs);
		expectedCounts.initializeZeros();
		for (int n=0; n<c.trainInstanceList.size(); n++) {
			Instance instance = c.trainInstanceList.get(n);
			instance.doInference(model);
			instance.addToCounts(expectedCounts);
			LL += instance.forwardBackward.logLikelihood;
			instance.clearInference();
		}
	}
	
	public void mStep() {
		model.updateFromCounts(expectedCounts);
		model.param.transition.printDistribution();
		//model.param.initial.printDistribution();
	}

	public void start() {
		System.out.println("Starting EM");
		Timing totalEMTime = new Timing();
		totalEMTime.start();
		Timing eStepTime = new Timing();
		for (int iterCount=0 ; iterCount < numIter; iterCount++) {
			LL = 0;
			//e-step
			eStepTime.start();
			eStep();
			System.out.format("LL %2.10f \t Iter %d E-step time %s\n", LL, iterCount, eStepTime.stop());
			//m-step
			mStep();
			oldLL = LL;
		}
		System.out.println("Total EM Time : " + totalEMTime.stop());
	}
}
