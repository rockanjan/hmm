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
	
	double bestOldLLDev = -Double.MAX_VALUE;
	double LLDev = 0;
	
	
	//convergence criteria
	double precision = 1e-6;
	int maxConsecutiveDecreaseLimit = 3;
	
	HMMParamBase expectedCounts;
	
	int lowerCount = 0; //number of times LL could not increase from previous best
	int iterCount = 0;
	
	public static int sampleSentenceSize = 50000;
	
	public EM(int numIter, Corpus c, HMMBase model) {
		this.numIter = numIter;
		this.c = c;
		this.model = model;
	}
	
	public void eStep() {
		c.generateRandomTrainingSample(sampleSentenceSize);
		if(model.hmmType == HMMType.WITH_NO_FINAL_STATE) {
			expectedCounts = new HMMParamNoFinalState(model);
		} else if(model.hmmType == HMMType.WITH_FINAL_STATE) {
			expectedCounts = new HMMParamFinalState(model);
		} else if(model.hmmType == HMMType.LOG_SCALE) {
			expectedCounts = new HMMParamNoFinalStateLog(model);
		}
		expectedCounts.initializeZeros();
		for (int n=0; n<c.randomTrainingSampleInstanceList.size(); n++) {
			Instance instance = c.randomTrainingSampleInstanceList.get(n);
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
			//add more and more examples per iteration
			//sampleSentenceSize += 1000;
			StringBuffer sb = new StringBuffer();
			if(iterCount>0) {
				sb.append(String.format("LL %.2f Diff %.2f \t Iter %d", LL, (LL - bestOldLL), iterCount));
			}
			if(LL > bestOldLL) {
				bestOldLL = LL;
			}
			//m-step
			mStep();
			if(c.devInstanceList != null) {
				LLDev = c.devInstanceList.getLL(model);
				if(iterCount > 0) {
					sb.append(String.format(" DevLL %.2f \t devDiff %.2f ", LLDev, (LLDev - bestOldLLDev)));
				}
			}
			if(isConverged()) {
				break;
			}
			sb.append(String.format("\t Fixes: %d \t E-step time %s", Stats.totalFixes, eStepTime.stop()));
			System.out.println(sb.toString());
		}
		System.out.println("Total EM Time : " + totalEMTime.stop());
	}
	
	public boolean isConverged() {
		if(c.devInstanceList == null) { //use the training data itself for checking convergence
			LLDev = LL;
		}
		
		double decreaseRatio = (LLDev - bestOldLLDev)/Math.abs(LLDev);
		//System.out.println("Decrease Ratio: %.5f " + decreaseRatio);
		if(iterCount % 50 == 0) {
			model.saveModel(iterCount);
		}
		if(precision > decreaseRatio && decreaseRatio > 0) {
			System.out.println("Converged. Saving the final model");
			model.saveModel(iterCount);
			model.saveModel(-1); //final
			return true;
		}
		
		if(LLDev < bestOldLLDev) {
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
			bestOldLLDev = LLDev;
			return false;
		}
	}
}
