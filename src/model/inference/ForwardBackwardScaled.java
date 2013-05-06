package model.inference;

import model.HMMBase;
import model.HMMNoFinalState;
import model.HMMType;
import model.param.HMMParamBase;
import model.param.MultinomialBase;
import util.MyArray;
import util.Stats;
import corpus.Instance;

public class ForwardBackwardScaled extends ForwardBackward{
	double[] scale;
	public ForwardBackwardScaled(HMMBase model, Instance instance) {
		super();
		this.model = model;
		this.instance = instance;
		this.nrStates = model.nrStates;
		T = instance.T; 
		initial = model.param.initial;
		transition = model.param.transition;
		observation = model.param.observation;		
		scale = new double[T];		
	}
	
	@Override
	public void doInference() {
		forward();
		backward();
		computePosterior();
	}
	
	@Override
	public void forward() {
		alpha = new double[T][nrStates]; //+1 for fake state
		logLikelihood = 0;
		//initialization: for t=0
		for(int i=0; i<nrStates; i++) {
			double pi = initial.get(i, 0);
			double obs = observation.get( instance.words[0], i);
			alpha[0][i] = pi * obs;
			
			if(alpha[0][i] == 0) {
				Stats.totalFixes++;
				alpha[0][i] = Double.MIN_VALUE; //fix
				//System.err.format("ZERO alpha at initial. init = %f, obs=%f\n", pi, obs);
			}
			
			scale[0] += alpha[0][i];
			if(scale[0] == 0) {
				throw new RuntimeException("Scale at initial position zero in forward");
			}
		}
		for(int i=0; i<nrStates; i++) {
			alpha[0][i] = alpha[0][i] / scale[0];
		}
		logLikelihood += Math.log(scale[0]);
		if(Double.isNaN(Math.log(scale[0]))) {
			System.out.println("Scale = " + scale[0]);
			MyArray.printTable(alpha);
			throw new RuntimeException("logLikelihood at initial position in forward is NaN");
		}
		//induction
		for(int t = 1; t < T; t++) {
			for(int j=0; j<nrStates; j++) {
				double transSum = 0;
				for(int i=0; i<nrStates; i++) {
					transSum += alpha[t-1][i] * transition.get(j, i);
				}
				double obs;
				obs = observation.get(instance.words[t], j);
				alpha[t][j] = transSum * obs;
				
				if(alpha[t][j] == 0) {
					Stats.totalFixes++;
					alpha[t][j] = Double.MIN_VALUE;//fix
				}				
				scale[t] += alpha[t][j];
				if(scale[t] == 0) {
					throw new RuntimeException("scale zero in forward");
				}
			}
			//scale
			for(int j=0; j<nrStates; j++) {
				if(scale[t] <= 0) {
					System.err.println("Scale is not positive");
					System.exit(-1);
				}
				alpha[t][j] = alpha[t][j] / scale[t];				
			}
			logLikelihood += Math.log(scale[t]);
			if(Double.isNaN(logLikelihood)) {
				System.out.println(scale[t]);
				MyArray.printTable(alpha);
				throw new RuntimeException("logLikelihood is NaN");
			}
		}
		//MyArray.printTable(alpha);
		//System.out.println("LogLikelihood: " + logLikelihood);
	}
	
	@Override
	public void backward() {
		beta = new double[T][nrStates];
		//initialization for t=T
		for(int i=0; i<nrStates; i++) {
			beta[T-1][i] = scale[T-1] * 1.0;
		}
		//induction
		for(int t=T-2; t>=0; t--) {
			for(int i=0; i<nrStates; i++) {			
				double sum = 0;
				for(int j=0; j<nrStates; j++) {
					double trans = transition.get(j, i);
					double obs;
					obs = observation.get(instance.words[t+1], j);
					sum += trans * obs * beta[t+1][j];
				}
				beta[t][i] = sum / scale[t];				
			}
		}
		//MyArray.printTable(beta);
	}
	
	@Override
	public void computePosterior() {
		posterior = new double[T][nrStates];
		for(int t=0; t<T; t++) {
			double denom = 0;
			
			for(int i=0; i<nrStates; i++) {
				denom += alpha[t][i] * beta[t][i];
			}
			if(denom == 0) {
				throw new RuntimeException("Denominator zero while computing posterior"); 
			}
			for(int i=0; i<nrStates; i++) {
				posterior[t][i] = alpha[t][i] * beta[t][i] / denom;
			}
		}
		//MyArray.printTable(posterior);
		checkStatePosterior();
	}
	
	public void checkStatePosterior(){
		double tolerance = 1e-5;
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
	
	public void addToCounts(HMMParamBase param) { 
		addToInitial(param.initial);
		addToObservation(param.observation);
		addToTransition(param.transition);
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
					transition.addToCounts(i, j, getTransitionPosterior(i, j, t) / normalizer);
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
	
	public double getStatePosterior(int t, int s) {
		return posterior[t][s];
	}
	
	@Override
	public void checkForwardBackward() {
				
	}
}
