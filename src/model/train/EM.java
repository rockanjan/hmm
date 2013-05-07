package model.train;

import model.HMMBase;
import model.HMMNoFinalState;
import model.HMMType;
import model.param.HMMParamBase;
import model.param.HMMParamFinalState;
import model.param.HMMParamNoFinalState;
import model.param.HMMParamNoFinalStateLog;

import util.MyArray;
import util.Stats;
import util.Timing;
import corpus.Corpus;
import corpus.Instance;

public class EM {

	int numIter;
	Corpus c;
	HMMBase model;
	
	double bestOldLL = -Double.MAX_VALUE;
	double LL = 0;
	
	
	//convergence criteria
	double precision = 1e-6;
	int maxConsecutiveDecreaseLimit = 5;
	
	HMMParamBase expectedCounts;
	
	int lowerCount = 0; //number of times LL could not increase from previous best
	int iterCount = 0;
	public EM(int numIter, Corpus c, HMMBase model) {
		this.numIter = numIter;
		this.c = c;
		this.model = model;
	}
	
	public void eStep() {
		if(model.hmmType == HMMType.WITH_NO_FINAL_STATE) {
			expectedCounts = new HMMParamNoFinalState(model);
		} else if(model.hmmType == HMMType.WITH_FINAL_STATE) {
			expectedCounts = new HMMParamFinalState(model);
		} else if(model.hmmType == HMMType.LOG_SCALE) {
			expectedCounts = new HMMParamNoFinalStateLog(model);
		}
		expectedCounts.initializeZeros();
		for (int n=0; n<c.trainInstanceList.size(); n++) {
			Instance instance = c.trainInstanceList.get(n);
			instance.doInference(model);
			instance.forwardBackward.addToCounts(expectedCounts);
			LL += instance.forwardBackward.logLikelihood;
			instance.clearInference();
		}
	}
	
	public void mStep() {
//		MyArray.printTable(expectedCounts.initial.count);
//		MyArray.printTable(expectedCounts.transition.count);
//		MyArray.printTable(expectedCounts.observation.count);
		model.updateFromCounts(expectedCounts);
//		model.param.initial.printDistribution();
//		model.param.transition.printDistribution();
//		model.param.observation.printDistribution();
	}

	public void start() {
		System.out.println("Starting EM");
		Timing totalEMTime = new Timing();
		totalEMTime.start();
		Timing eStepTime = new Timing();
		
		for (iterCount=0; iterCount < numIter; iterCount++) {
			LL = 0;
			//e-step
			eStepTime.start();
			Stats.totalFixes = 0;
			eStep();
			if(iterCount>0) {
				System.out.format("LL %.2f Diff %.2f \t Iter %d \t Fixes: %d \t E-step time %s\n", LL, (LL - bestOldLL), iterCount, Stats.totalFixes, eStepTime.stop());
			}
			if(isConverged()) {
				break;
			}
			//m-step
			mStep();						
		}
		System.out.println("Total EM Time : " + totalEMTime.stop());
	}
	
	public boolean isConverged() {
		
		double decreaseRatio = (LL - bestOldLL)/Math.abs(bestOldLL);
		//System.out.println("Decrease Ratio: %.5f " + decreaseRatio);
		if(precision > decreaseRatio && decreaseRatio > 0) {
			System.out.println("Converged. Saving the final model");
			model.saveModel(iterCount);
			model.saveModel(-1); //final
			return true;
		}
		
		if(LL < bestOldLL) {
			if(lowerCount == 0) {
				//save the best model so far
				System.out.println("Saving the best model so far");
				model.saveModel(iterCount);
			}
			lowerCount++;
			if(lowerCount == maxConsecutiveDecreaseLimit) {
				System.out.format("Converged: LL could not increase for %d iterations\n", maxConsecutiveDecreaseLimit);
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
