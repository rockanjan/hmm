package model.inference;

import model.HMMBase;
import model.HMMNoFinalState;
import model.HMMType;
import model.param.HMMParamBase;
import model.param.MultinomialBase;
import model.param.MultinomialRegular;
import corpus.Instance;

public abstract class ForwardBackward {
	public HMMBase model;
	Instance instance;
	public double logLikelihood;
	public double alpha[][];
	public double beta[][];
	public double posterior[][];
	
	//for easier reference
	int T;
	int nrStates;
	MultinomialBase initial;
	MultinomialBase transition;
	MultinomialBase observation;
	
	public abstract void doInference();
	public abstract void forward();
	public abstract void backward();
	public abstract void computePosterior();
	public abstract void checkForwardBackward();
	public abstract void checkStatePosterior();
	
	public void addToCounts(HMMParamBase param) { 
		addToInitial(param.initial);
		addToObservation(param.observation);
		addToTransition(param.transition);
	}
	
	public abstract void addToInitial(MultinomialBase initial);	
	public abstract void addToObservation(MultinomialBase observation);	
	public abstract void addToTransition(MultinomialBase transition);	
	
	/*
	 * Gives the transition posterior probability
	 */
	public abstract double getTransitionPosterior(int currentState, int nextState, int position);	
	public abstract double getStatePosterior(int t, int s);
	
	public void clear() {
		alpha = null;
		beta = null;
		posterior = null;
	}
	
}
