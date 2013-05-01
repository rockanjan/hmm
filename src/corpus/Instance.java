package corpus;

import java.util.ArrayList;

import util.SmoothWord;

import model.ForwardBackward;
import model.ForwardBackwardNoScaling;
import model.HMM;
import model.HMMParam;
import model.Multinomial;

public class Instance {
	public int[] words;
	public int T; //sentence length
	Corpus c;	
	ForwardBackward forwardBackward;
	public int nrStates;
	
	public Instance(Corpus c, String line) {
		this.c = c;
		//read from line
		populateWordArray(line);
	}
	
	public void doInference(HMM model) {
		nrStates = forwardBackward.model.nrStates;
		ForwardBackward forwardBackward = new ForwardBackwardNoScaling(model, this);
		forwardBackward.doInference();
	}
	
	public void clearInference() {
		
	}
	
	public void addToCounts(HMMParam param) { 
		addToInitial(param.initial);
		addToObservation(param.observation);
		addToTransition(param.observation);
	}
	
	public void addToInitial(Multinomial initial) {
		for(int i=0; i<nrStates; i++) {
			initial.addToCounts(i, 0, getStatePosterior(0, i));
		}
	}
	
	public void addToObservation(Multinomial observation) {
		
	}
	
	public void addToTransition(Multinomial transition) {
		
	}
	
	public double getStatePosterior(int t, int s) {
		return forwardBackward.posterior[t][s];
	}
	
	public void populateWordArray(String line) {
		String splitted[] = line.split(c.delimiter);
		ArrayList<Integer> tempWordArray = new ArrayList<Integer>(); 
		words = new int[splitted.length];
		for(int i=0; i<splitted.length; i++) {
			String word = splitted[i];
			if(c.corpusVocab.lower) {
				word = word.toLowerCase();
			}
			if(c.corpusVocab.smooth) {
				word = SmoothWord.smooth(word);
			}
			int index = c.corpusVocab.getIndex(word);
			if(index > 0) {
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
