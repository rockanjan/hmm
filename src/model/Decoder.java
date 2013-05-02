package model;

import util.MyArray;
import corpus.Instance;

public class Decoder {
	HMM model;
	public Decoder(HMM model) {
		this.model = model;
	}
	
	public int[] viterbi(Instance instance) {
		int[] decoded = new int[instance.T];
		instance.doInference(model);
		
		double[][] probLattice = new double[instance.T+1][model.nrStates];
		int[][] stateLattice = new int[instance.T+1][model.nrStates];
		
		for(int i=0; i<model.nrStates; i++) {
			double init = model.param.initial.get(i, 0);
			double obs = model.param.observation.get(instance.words[0], i);
			probLattice[0][i] = Math.log(init) + Math.log(obs);			
		}
		
		for(int t=1; t<instance.T; t++) {
			for(int j=0; j<model.nrStates; j++) {
				double obs = model.param.observation.get(instance.words[t], j);
				
				double maxValue = -Double.MAX_VALUE;
				int maxIndex = -1;
				for(int i=0; i<model.nrStates; i++) {
					double value = probLattice[t-1][i] + Math.log(model.param.transition.get(j, i)) + Math.log(obs);
					if(value > maxValue) {
						maxValue = value;
						maxIndex = i;
					}
				}
				probLattice[t][j] = maxValue;
				stateLattice[t][j] = maxIndex;
			}
		}
		
		//fake state : to consider which state at the last index of the sentence has higher transition prob to STOP fake state
		double maxValue = -Double.MAX_VALUE;
		int maxIndex = -1;
		for(int i=0; i<model.nrStates; i++) {
			double value = probLattice[instance.T-1][i] + Math.log(model.param.transition.get(model.nrStates, i));
			if(value > maxValue) {
				maxValue = value;
				maxIndex = i;
			}
		}
		decoded[instance.T-1] = maxIndex; 
		//backtrack
		for(int t=instance.T-2; t>=0; t--) {
			decoded[t] = stateLattice[t+1][decoded[t+1]];			
		}
		//MyArray.printTable(probLattice);	
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
		return decoded;
	}
}
