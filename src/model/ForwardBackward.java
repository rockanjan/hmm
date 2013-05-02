package model;

import corpus.Instance;

public abstract class ForwardBackward {
	
	public HMM model;
	Instance instance;
	public double logLikelihood;
	public double alpha[][];
	public double beta[][];
	public double posterior[][];
	
	//for easier reference
	int T;
	int nrStates;
	Multinomial initial;
	Multinomial transition;
	Multinomial observation;
	
	public abstract void doInference();
	public abstract void forward();
	public abstract void backward();
	public abstract void computePosterior();
	
	public abstract void checkForwardBackward();
	public abstract void checkStatePosterior();
	
	public void clear() {
		alpha = null;
		beta = null;
		posterior = null;
	}
	
}
