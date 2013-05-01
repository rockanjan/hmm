package model;

import corpus.Instance;

public abstract class ForwardBackward {
	
	public HMM model;
	Instance instance;
	public double likelihood;
	public double logLikelihood;
	double alpha[][];
	double beta[][];
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
	
}
