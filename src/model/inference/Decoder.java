package model.inference;

import java.util.Arrays;

import model.HMMBase;
import model.HMMNoFinalState;
import model.HMMType;
import util.MyArray;
import corpus.Instance;

public class Decoder {
	HMMBase model;
	public Decoder(HMMBase model) {
		this.model = model;
	}
	
	public int[] viterbi(Instance instance) {
		if(model.hmmType == HMMType.LOG_SCALE) {
			return viterbiLog(instance);
		} else {
			return viterbiRegular(instance);
		}
		
	}
	
	private int[] viterbiRegular(Instance instance) {
		int[] decoded = new int[instance.T];
		instance.doInference(model);
		
		double[][] probLattice = new double[instance.T][model.nrStates];
		int[][] stateLattice = new int[instance.T][model.nrStates];
		
		for(int i=0; i<model.nrStates; i++) {
			double init = model.param.initial.get(i, 0);
			double obs = model.param.observation.get(instance.words[0], i);
			probLattice[0][i] = Math.log(init) + Math.log(obs);			
		}
		
		double maxValue = -Double.MAX_VALUE;
		int maxIndex = -1;
		for(int t=1; t<instance.T; t++) {
			for(int j=0; j<model.nrStates; j++) {
				double obs = model.param.observation.get(instance.words[t], j);
				
				maxValue = -Double.MAX_VALUE;
				maxIndex = -1;
				for(int i=0; i<model.nrStates; i++) {
					double value = probLattice[t-1][i] + Math.log(model.param.transition.get(j, i)) + Math.log(obs);
					if(model.hmmType == HMMType.WITH_FINAL_STATE && t == instance.T-1) {
						//also include the transition to the final state
						value += Math.log(model.param.transition.get(model.nrStates, j));
					}
					if(value > maxValue) {
						maxValue = value;
						maxIndex = i;
					}
				}
				probLattice[t][j] = maxValue;
				stateLattice[t][j] = maxIndex;
			}
		}
		maxValue = -Double.MAX_VALUE;
		maxIndex = -1;
		for(int i=0; i<model.nrStates; i++) {
			if(probLattice[instance.T-1][i] > maxValue) {
				maxValue = probLattice[instance.T-1][i];
				decoded[instance.T-1] = i;
			}
		}
		//backtrack
		for(int t=instance.T-2; t>=0; t--) {
			decoded[t] = stateLattice[t+1][decoded[t+1]];			
		}
		//MyArray.printTable(probLattice);
		instance.clearInference();
		return decoded;
	}
	
	private int[] viterbiLog(Instance instance) {
		int[] decoded = new int[instance.T];
		Arrays.fill(decoded, -1);
		instance.doInference(model);
		
		double[][] probLattice = new double[instance.T][model.nrStates];
		int[][] stateLattice = new int[instance.T][model.nrStates];
		
		for(int i=0; i<model.nrStates; i++) {
			double init = model.param.initial.get(i, 0);
			double obs = model.param.observation.get(instance.words[0], i);
			probLattice[0][i] = init + obs;			
		}
		
		double maxValue = -Double.MAX_VALUE;
		int maxIndex = -1;
		for(int t=1; t<instance.T; t++) {
			for(int j=0; j<model.nrStates; j++) {
				double obs = model.param.observation.get(instance.words[t], j);
				
				maxValue = -Double.MAX_VALUE;
				maxIndex = -1;
				for(int i=0; i<model.nrStates; i++) {
					double value = probLattice[t-1][i] + model.param.transition.get(j, i) + obs;
					if(value > maxValue) {
						maxValue = value;
						maxIndex = i;
					}
				}
				probLattice[t][j] = maxValue;
				stateLattice[t][j] = maxIndex;
			}
		}
		maxValue = -Double.MAX_VALUE;
		maxIndex = -1;
		for(int i=0; i<model.nrStates; i++) {
			if(probLattice[instance.T-1][i] > maxValue) {
				maxValue = probLattice[instance.T-1][i];
				decoded[instance.T-1] = i;
			}
		}
		//backtrack
		for(int t=instance.T-2; t>=0; t--) {
			decoded[t] = stateLattice[t+1][decoded[t+1]];			
		}
		//MyArray.printTable(probLattice);
		instance.clearInference();
		return decoded;
	}
	
	public int[] posterior(Instance instance) {
		int[] decoded = new int[instance.T];
		instance.doInference(model);
		
		double maxValue = -Double.MAX_VALUE;
		int maxIndex = -1;
		
		for(int t=0; t<instance.T; t++) {
			maxValue = -Double.MAX_VALUE;
			maxIndex = -1;
			for(int i=0; i<model.nrStates; i++) {
				if(instance.forwardBackward.posterior[t][i] > maxValue) {
					maxValue = instance.forwardBackward.posterior[t][i];
					maxIndex = i;					
				}
			}
			decoded[t] = maxIndex;
		}
		instance.clearInference();
		return decoded;
	}
	
	public double[][] posteriorDistribution(Instance instance) {
		double[][] decoded = new double[instance.T][model.nrStates];
		instance.doInference(model);
		for(int t=0; t<instance.T; t++) {
			for(int i=0; i<model.nrStates; i++) {
				decoded[t][i] = instance.forwardBackward.posterior[t][i];
			}
		}
		instance.clearInference();
		return decoded;
	}
}
