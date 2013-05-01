package model;

import util.MyArray;
import corpus.Instance;

public class ForwardBackward {
	
	HMM model;
	Instance instance;
	double likelihood;
	double logLikelihood;
	double alpha[][];
	double beta[][];
	double posterior[][];
	
	//for easier reference
	int T;
	int nrStates;
	Multinomial initial;
	Multinomial transition;
	Multinomial observation;
	
	public ForwardBackward(HMM model, Instance instance) {
		this.model = model;
		this.instance = instance;
		nrStates = model.nrStates;
		T = instance.T; 
		initial = model.param.initial;
		transition = model.param.transition;
		observation = model.param.observation;
	}
	
	public void doInference() {
		forward();
		backward();
		computePosterior();
	}
	
	public void forward() {
		alpha = new double[T+1][nrStates]; //+1 for fake state
		//for t=0
		for(int i=0; i<nrStates; i++) {
			double pi = initial.get(i, 0);
			double obs = observation.get( instance.words[0], i);
			alpha[0][i] = pi * obs;  
		}
		likelihood = 0;
		for(int t = 1; t < T+1; t++) {
			for(int j=0; j<nrStates; j++) {
				double transSum = 0;
				for(int i=0; i<nrStates; i++) {
					transSum += alpha[t-1][i] * transition.get(j, i);
				}
				double obs;
				if(t == T) {
					//fake state
					obs = 1.0;
				} else {
					obs = observation.get(instance.words[t], j);
				}
				alpha[t][j] = transSum * obs;
				if(t == T) {
					likelihood += alpha[t][j];
				}
			}
		}
		//MyArray.printTable(alpha);
		System.out.println("Likelihood : " + likelihood);
	}
	
	public void backward() {
		beta = new double[T+1][nrStates];
		//initialization for t=T
		for(int i=0; i<nrStates; i++) {
			beta[T][i] = 1.0;
		}
		//induction
		for(int t=T-1; t>=0; t--) {
			for(int i=0; i<nrStates; i++) {			
				double sum = 0;
				for(int j=0; j<nrStates; j++) {
					double trans = transition.get(j, i);
					double obs;
					if(t == T-1) {
						obs = 1.0; //taken for the fake state(at t+1)
					} else {
						obs = observation.get(instance.words[t+1], j);
					}
					sum += trans * obs * beta[t+1][j];
				}
				beta[t][i] = sum;
			}
		}
		//MyArray.printTable(beta);
	}
	
	public void computePosterior() {
		posterior = new double[T][nrStates];
		for(int t=0; t<T; t++) {
			for(int i=0; i<nrStates; i++) {
				//System.out.format("alpha %f, beta %f\n", alpha[t][i], beta[t][i]);
				posterior[t][i] = alpha[t][i] * beta[t][i] / likelihood;
			}
		}
		MyArray.printTable(posterior);
		//TODO: for t=T, it should be deterministic, should we define it?
		//posterior[T][nrStates] = 1.0;
	}
}
