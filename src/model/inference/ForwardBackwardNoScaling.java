package model.inference;

import model.HMMBase;
import model.HMMNoFinalState;
import model.HMMType;
import model.param.MultinomialBase;
import util.MyArray;
import corpus.Instance;

public class ForwardBackwardNoScaling extends ForwardBackward{
	public double likelihood;
	public ForwardBackwardNoScaling(HMMBase model, Instance instance) {
		super();
		this.model = model;
		this.instance = instance;
		this.nrStates = model.nrStates;
		T = instance.T; 
		initial = model.param.initial;
		transition = model.param.transition;
		observation = model.param.observation;		
	}
	
	@Override
	public void doInference() {
		forward();
		backward();
		computePosterior();
	}
	
	@Override
	public void forward() {
		alpha = new double[T][nrStates]; 
		//for t=0
		for(int i=0; i<nrStates; i++) {
			double pi = initial.get(i, 0);
			double obs = observation.get( instance.words[0], i);
			alpha[0][i] = pi * obs;  
		}
		likelihood = 0;
		logLikelihood = 0;
		for(int t = 1; t < T; t++) {
			for(int j=0; j<nrStates; j++) {
				double transSum = 0;
				for(int i=0; i<nrStates; i++) {
					double trans;
					trans = transition.get(j, i);
					transSum += alpha[t-1][i] * trans;
				}
				double obs;
				obs = observation.get(instance.words[t], j);
				alpha[t][j] = transSum * obs;				
			}
		}
		for(int i=0; i<nrStates; i++) {
			likelihood += alpha[T-1][i];			
		}
		//MyArray.printTable(alpha, "noscaling alpha");
		logLikelihood = Math.log(likelihood);		
	}
	
	public void backward() {
		beta = new double[T][nrStates];
		//initialization for t=T
		for(int i=0; i<nrStates; i++) {
			beta[T-1][i] = 1.0;
		}
		//induction
		for(int t=T-2; t>=0; t--) {
			for(int i=0; i<nrStates; i++) {			
				double sum = 0;
				for(int j=0; j<nrStates; j++) {
					double trans;
					trans = transition.get(j, i);
					double obs;
					obs = observation.get(instance.words[t+1], j);
					sum += trans * obs * beta[t+1][j];
				}
				beta[t][i] = sum;
			}
		}
		//MyArray.printTable(beta, "noscaling beta");		
	}
	
	@Override
	public void computePosterior() {
		posterior = new double[T][nrStates];
		for(int t=0; t<T; t++) {
			for(int i=0; i<nrStates; i++) {
				posterior[t][i] = alpha[t][i] * beta[t][i] / likelihood;
			}
		}
		//MyArray.printTable(posterior, "noscaling posterior");
		checkStatePosterior();
		//System.exit(-1);
	}
	
	@Override
	public void checkStatePosterior(){
		double tolerance = 1e-3;
		for(int t=0; t<T; t++) {
			double sum = 0;
			for(int i=0; i<nrStates; i++) {
				if(Double.isInfinite(posterior[t][i])){
					throw new RuntimeException("State posterior infinite while checking");
				}
				if(Double.isNaN(posterior[t][i])){
					throw new RuntimeException("State posterior NaN while checking");
				}
				sum += posterior[t][i];
			}
			if(Math.abs(sum - 1) > tolerance) {
				throw new RuntimeException("In checking state posterior, sum = " + sum);
			}
		}
	}

	@Override
	public void checkForwardBackward() {
		// TODO Auto-generated method stub
		
	}
	
	public void addToInitial(MultinomialBase initial) {
		for(int i=0; i<nrStates; i++) {
			initial.addToCounts(i, 0, getStatePosterior(0, i));
		}
	}
	
	public void addToObservation(MultinomialBase observation) {
		for(int t=0; t<T; t++) {
			for(int i=0; i<nrStates; i++) {
				observation.addToCounts(instance.words[t], i, getStatePosterior(t, i));
			}
		}
	}
	
	public void addToTransition(MultinomialBase transition) {
		for(int t=0; t<T-1; t++) {
			double normalizer = 0.0;
			for(int i=0; i<nrStates; i++) {
				for(int j=0; j<nrStates; j++) {
					normalizer += getTransitionPosterior(i, j, t);
				}
			}
			
			for(int i=0; i<nrStates; i++) {
				for(int j=0; j<nrStates; j++) {
					transition.addToCounts(j, i, getTransitionPosterior(i, j, t) / normalizer);
				}
			}
		}
		if(model.hmmType == HMMType.WITH_FINAL_STATE) {
			//transition to fake state
			for(int i=0; i<nrStates; i++) {
				//double value = getStatePosterior(T-1, i) * forwardBackward.model.param.transition.get(nrStates, i);
				//transition.addToCounts(nrStates, i, value);
			}
		}
	}
	
	/*
	 * Gives the transition posterior probability
	 */
	public double getTransitionPosterior(int currentState, int nextState, int position) {
		//xi in Rabiner Tutorial
		double alphaValue = alpha[position][currentState];
		double trans = model.param.transition.get(nextState, currentState); //transition to next given current
		double obs = model.param.observation.get(instance.words[position+1], nextState);
		double betaValue = beta[position+1][nextState];		
		double value = alphaValue * trans * obs * betaValue;
		return value;
	}

	@Override
	public double getStatePosterior(int t, int s) {
		return posterior[t][s];
	}

}
