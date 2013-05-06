package corpus;

import java.util.ArrayList;

import util.SmoothWord;

import model.HMMBase;
import model.HMMType;
import model.inference.ForwardBackward;
import model.inference.ForwardBackwardScaled;
import model.param.HMMParamBase;
import model.param.Multinomial;

public class Instance {
	public int[] words;
	public int T; //sentence length
	Corpus c;	
	public ForwardBackward forwardBackward;
	public int nrStates;
	public int unknownCount;
	
	public Instance(Corpus c, String line) {
		this.c = c;
		unknownCount = 0;
		//read from line
		populateWordArray(line);
	}
	
	public void doInference(HMMBase model) {
		//forwardBackward = new ForwardBackwardNoScaling(model, this);
		forwardBackward = new ForwardBackwardScaled(model, this);
		nrStates = forwardBackward.model.nrStates;
		forwardBackward.doInference();
	}
	
	public void clearInference() {
		forwardBackward.clear();
		forwardBackward = null;
	}
	
	public void addToCounts(HMMParamBase param) { 
		addToInitial(param.initial);
		addToObservation(param.observation);
		addToTransition(param.transition);
	}
	
	public void addToInitial(Multinomial initial) {
		for(int i=0; i<nrStates; i++) {
			initial.addToCounts(i, 0, getStatePosterior(0, i));
		}
	}
	
	public void addToObservation(Multinomial observation) {
		for(int t=0; t<T; t++) {
			for(int i=0; i<nrStates; i++) {
				observation.addToCounts(words[t], i, getStatePosterior(t, i));
			}
		}
	}
	
	public void addToTransition(Multinomial transition) {
		for(int t=0; t<T-1; t++) {
			for(int i=0; i<nrStates; i++) {
				for(int j=0; j<nrStates; j++) {
					transition.addToCounts(i, j, getTransitionPosterior(i, j, t));
				}
			}
		}
		if(forwardBackward.model.hmmType == HMMType.WITH_FINAL_STATE) {
			//transition to fake state
			for(int i=0; i<nrStates; i++) {
				//System.out.println(getStatePosterior(T-1, i));
				//double value = getStatePosterior(T-1, i)/c.totalWords;
				//double value = forwardBackward.alpha[T-1][i] * forwardBackward.model.param.transition.get(nrStates, i);
				//transition.addToCounts(nrStates, i, value);
			}
		}
	}
	
	/*
	 * Gives the transition posterior probability
	 */
	public double getTransitionPosterior(int currentState, int nextState, int position) {
		//xi in Rabiner Tutorial
		double alpha = forwardBackward.alpha[position][currentState];
		double trans = forwardBackward.model.param.transition.get(nextState, currentState); //transition to next given current
		double obs = forwardBackward.model.param.observation.get(words[position+1], nextState);
		double beta = forwardBackward.beta[position+1][nextState];		
		double value = alpha * trans * obs * beta;
		return value;
	}
	
	public double getStatePosterior(int t, int s) {
		return forwardBackward.posterior[t][s];
	}
	
	public String getWord(int position) {
		return c.corpusVocab.indexToWord.get(words[position]);
	}
	
	public void populateWordArray(String line) {
		//String splitted[] = line.split(c.delimiter);
		String splitted[] = line.split("(\\s+|\\t+)");
		ArrayList<Integer> tempWordArray = new ArrayList<Integer>();
		for(int i=0; i<splitted.length; i++) {
			String word = splitted[i];
			if(c.corpusVocab.lower) {
				word = word.toLowerCase();
			}
			if(c.corpusVocab.smooth) {
				word = SmoothWord.smooth(word);
			}
			int index = c.corpusVocab.getIndex(word);
			if(index >= 0) {
				if(index == 0) {
					unknownCount += 1;
				}
				tempWordArray.add(index);
			}
		}
		T = tempWordArray.size();
		words = new int[T];
		for(int i=0; i<T; i++) {
			words[i] = tempWordArray.get(i);
		}
		tempWordArray.clear();
		tempWordArray = null;
	}
}
