package model.inference;

import model.HMMNoFinalState;
import util.MyArray;
import corpus.Instance;

public class ForwardBackwardNoScaling extends ForwardBackward{
	public double likelihood;
	public ForwardBackwardNoScaling(HMMNoFinalState model, Instance instance) {
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
		//alpha = new double[T+1][nrStates]; //+1 for fake state
		alpha = new double[T][nrStates]; 
		//for t=0
		for(int i=0; i<nrStates; i++) {
			double pi = initial.get(i, 0);
			double obs = observation.get( instance.words[0], i);
			alpha[0][i] = pi * obs;  
		}
		likelihood = 0;
		logLikelihood = 0;
		//for(int t = 1; t < T+1; t++) {
		for(int t = 1; t < T; t++) {
			for(int j=0; j<nrStates; j++) {
				double transSum = 0;
				for(int i=0; i<nrStates; i++) {
					double trans;
					/*
					if(t == T) {
						trans = transition.get(nrStates, i);
					} else {
						trans = transition.get(j, i);
					}
					*/
					trans = transition.get(j, i);
					transSum += alpha[t-1][i] * trans;
				}
				double obs;
				/*
				if(t == T) {
					//fake state
					obs = 1.0;
				} else {
					obs = observation.get(instance.words[t], j);
				}
				*/
				obs = observation.get(instance.words[t], j);
				alpha[t][j] = transSum * obs;				
			}
		}
		for(int i=0; i<nrStates; i++) {
			likelihood += alpha[T-1][i];			
		}
		logLikelihood = Math.log(likelihood);
		//MyArray.printTable(alpha);		
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
					/*
					if(t == T-1) { 
						trans = transition.get(nrStates, i);
					} else {
						trans = transition.get(j, i);
					}
					*/
					trans = transition.get(j, i);
					double obs;
					/*
					if(t == T-1) {
						obs = 1.0; //taken for the fake state(at t+1)
					} else {
						obs = observation.get(instance.words[t+1], j);
					}
					*/
					obs = observation.get(instance.words[t+1], j);
					sum += trans * obs * beta[t+1][j];
				}
				beta[t][i] = sum;
			}
		}
		//MyArray.printTable(beta);
	}
	
	@Override
	public void computePosterior() {
		posterior = new double[T][nrStates];
		for(int t=0; t<T; t++) {
			for(int i=0; i<nrStates; i++) {
				posterior[t][i] = alpha[t][i] * beta[t][i] / likelihood;
			}
		}
		//MyArray.printTable(posterior);
		checkStatePosterior();
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
}
