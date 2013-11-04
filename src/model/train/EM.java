package model.train;

import java.util.ArrayList;
import java.util.List;

import model.HMMBase;
import model.HMMType;
import model.param.HMMParamBase;
import model.param.HMMParamFinalState;
import model.param.HMMParamNoFinalState;
import model.param.HMMParamNoFinalStateLog;
import program.Main;
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

	// convergence criteria
	double precision = 1e-6;
	int maxConsecutiveDecreaseLimit = 10;

	HMMParamBase expectedCounts;

	int lowerCount = 0; // number of times LL could not increase from previous
						// best
	public int iterCount = 0;

	public static int sampleSentenceSize = Integer.MAX_VALUE;
	public double adaptiveWeight = 1.0;
	public double t0 = 2;
	public static double alpha = 1.0;
	public Object updateLock = new Object();

	public EM(int numIter, Corpus c, HMMBase model) {
		this.numIter = numIter;
		this.c = c;
		this.model = model;
	}
	
	public void setAdaptiveWeight() {
		//fraction of data
		double f = 1.0 * c.randomTrainingSampleInstanceList.size() / c.trainInstanceList.size();
		if(f == 1) {
			//System.out.println("fraction = 1, all dataset used");
			adaptiveWeight = 1.0;
		} else {
			//standard adaptiveWeight technique
			//adaptiveWeight = (t0 + iterCount)^(-alpha)
			adaptiveWeight = Math.pow((t0 + iterCount), -alpha);
		}	
	}
	
	public void eStep() {
		c.generateRandomTrainingSample(sampleSentenceSize, iterCount);
		if (model.hmmType == HMMType.WITH_NO_FINAL_STATE) {
			expectedCounts = new HMMParamNoFinalState(model);
		} else if (model.hmmType == HMMType.WITH_FINAL_STATE) {
			expectedCounts = new HMMParamFinalState(model);
		} else if (model.hmmType == HMMType.LOG_SCALE) {
			expectedCounts = new HMMParamNoFinalStateLog(model);
		}
		expectedCounts.initializeZeros();
		if(Main.USE_THREAD_COUNT <= 1) {
			eStepNoThread();
		} else {
			eStepThreaded();
		}
	}

	public void eStepNoThread() {
		for (int n = 0; n < c.randomTrainingSampleInstanceList.size(); n++) {
			Instance instance = c.randomTrainingSampleInstanceList.get(n);
			instance.doInference(model);
			instance.forwardBackward.addToCounts(expectedCounts);
			LL += instance.forwardBackward.logLikelihood;
			instance.clearInference();
		}
	}

	public void eStepThreaded() {
		/* multi-threading */
		int divideSize = c.randomTrainingSampleInstanceList.size()
				/ Main.USE_THREAD_COUNT;
		List<EstepWorker> threadList = new ArrayList<EstepWorker>();
		int startIndex = 0;
		int endIndex = divideSize;
		for (int i = 0; i < Main.USE_THREAD_COUNT; i++) {
			EstepWorker worker = new EstepWorker(startIndex, endIndex);
			threadList.add(worker);
			worker.start();
			startIndex = endIndex;
			endIndex = endIndex + divideSize;
		}
		// there might be few remainders
		EstepWorker finalWorker = new EstepWorker(startIndex,
				c.randomTrainingSampleInstanceList.size());
		finalWorker.start();
		threadList.add(finalWorker);
		// wait for the threads to complete
		for (EstepWorker worker : threadList) {
			try {
				worker.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// add to the final expected counts
			synchronized (updateLock) {
				expectedCounts.addFromOtherParam(worker.threadExpectedCounts);
				LL += worker.threadLL;
			}
		}
	}

	private class EstepWorker extends Thread {
		public HMMParamBase threadExpectedCounts;
		public double threadLL = 0;
		final int startIndex;
		final int endIndex;

		public EstepWorker(int startIndex, int endIndex) {
			this.startIndex = startIndex;
			this.endIndex = endIndex;
		}

		@Override
		public void run() {
			if (model.hmmType == HMMType.WITH_NO_FINAL_STATE) {
				threadExpectedCounts = new HMMParamNoFinalState(model);
			} else if (model.hmmType == HMMType.WITH_FINAL_STATE) {
				threadExpectedCounts = new HMMParamFinalState(model);
			} else if (model.hmmType == HMMType.LOG_SCALE) {
				threadExpectedCounts = new HMMParamNoFinalStateLog(model);
			}
			threadExpectedCounts.initializeZeros();
			for (int n = startIndex; n < endIndex; n++) {
				Instance instance = c.randomTrainingSampleInstanceList.get(n);
				instance.doInference(model);
				instance.forwardBackward.addToCounts(threadExpectedCounts);
				threadLL += instance.forwardBackward.logLikelihood;
				instance.clearInference();
			}
		}
	}

	public void mStep() {
		setAdaptiveWeight();
		if(adaptiveWeight == 1.0) {
			model.updateFromCounts(expectedCounts);
		} else {
			//System.out.println("adaptive weight = " + adaptiveWeight);
			model.updateFromCountsWeighted(expectedCounts, adaptiveWeight);
		}
	}

	public void start() {
		System.out.println("Starting EM");
		Timing totalEMTime = new Timing();
		totalEMTime.start();
		Timing eStepTime = new Timing();

		for (iterCount = 0; iterCount < numIter; iterCount++) {
			LL = 0;
			// e-step
			eStepTime.start();
			Stats.totalFixes = 0;
			eStep();
			// add more and more examples per iteration
			// sampleSentenceSize += 1000;
			StringBuffer sb = new StringBuffer();
			LL = LL / c.randomTrainingSampleInstanceList.numberOfTokens;
			double trainPreplex = Math.pow(2, -LL/Math.log(2));
			if (iterCount > 0) {
				sb.append(String.format("LL %.6f Diff %.6f perp %.2f \t Iter %d", LL,
						(LL - bestOldLL), trainPreplex, iterCount));
			}
			if (LL > bestOldLL) {
				bestOldLL = LL;
			}
			// m-step
			mStep();
			if (c.devInstanceList != null) {
				LLDev = c.devInstanceList.getLL(model);
				LLDev = LLDev/c.devInstanceList.numberOfTokens;
				double devPerplex = Math.pow(2, -LLDev/Math.log(2));
				if (iterCount > 0) {
					sb.append(String.format(" devLL %.6f \t dD %.6f dP %.2f",
							LLDev, (LLDev - bestOldLLDev), devPerplex));
				}
			}
            if(c.testInstanceList != null && iterCount % 20 == 0) {
			double testLL = c.testInstanceList.getLL(model);
			testLL = testLL / c.testInstanceList.numberOfTokens;
			double testPerplexity = Math.pow(2, -testLL/Math.log(2));
			System.out.println("Test data LL = " + testLL + " perplexity = " + testPerplexity);
		    }
			if (isConverged()) {
				break;
			}
			sb.append(String.format("\t Fix: %d \t time %s",
					Stats.totalFixes, eStepTime.stop()));
			System.out.println(sb.toString());
		}
		System.out.println("Total EM Time : " + totalEMTime.stop());
	}

	public boolean isConverged() {
		if (c.devInstanceList == null) { // use the training data itself for
											// checking convergence
			LLDev = LL;
		}

		double decreaseRatio = (LLDev - bestOldLLDev) / Math.abs(LLDev);
		// System.out.println("Decrease Ratio: %.5f " + decreaseRatio);
		if (iterCount % 50 == 0) {
			model.saveModel(iterCount);
		}
		if (precision > decreaseRatio && decreaseRatio > 0) {
			System.out.println("Converged. Saving the final model");
			model.saveModel(iterCount);
			model.saveModel(-1); // final
			return true;
		}

		if (LLDev < bestOldLLDev) {
			if (lowerCount == 0) {
				// save the best model so far
				System.out.println("Saving the best model so far");
				model.saveModel(iterCount);
			}
			lowerCount++;
			if (lowerCount == maxConsecutiveDecreaseLimit) {
				System.out.format(
						"Converged: LL could not increase for %d iterations\n",
						maxConsecutiveDecreaseLimit);
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
