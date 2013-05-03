package model;

import java.text.DecimalFormat;

import util.Stats;
import util.Timing;
import corpus.Corpus;
import corpus.Instance;

public class EM {

	int numIter;
	Corpus c;
	HMM model;
	
	double bestOldLL = -Double.MAX_VALUE;
	double LL = 0;
	
	HMMParam expectedCounts;
	
	int lowerCount = 0; //number of times LL could not increase from previous best
	
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
		//model.param.transition.printDistribution();
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
			Stats.totalFixes = 0;
			eStep();
			if(iterCount>0) {
				System.out.format("LL %.2f Diff %.2f \t Iter %d \t Fixes: %d \t E-step time %s\n", LL, (LL - bestOldLL), iterCount, Stats.totalFixes, eStepTime.stop());
			}
			Stats.totalFixes = 0;
			//m-step
			mStep();
			if(isConverged()) {
				break;
			}			
		}
		System.out.println("Total EM Time : " + totalEMTime.stop());
	}
	
	public boolean isConverged() {
		double precision = 1e-6;
		double decreaseRatio = (LL - bestOldLL)/Math.abs(bestOldLL);
		System.out.println("Decrease Ratio: " + decreaseRatio);
		if(precision > decreaseRatio && decreaseRatio > 0) {
			return true;
		}
		
		if(LL < bestOldLL) {
			lowerCount++;
			if(lowerCount == 3) {
				System.out.println("Converged: LL could not increase for three iterations");
				return true;
			}
			return false;
		} else {
			lowerCount = 0;
			bestOldLL = LL;
			return false;
		}
	}
}
