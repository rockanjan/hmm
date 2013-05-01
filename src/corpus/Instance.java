package corpus;

import java.util.ArrayList;

import util.SmoothWord;

import model.ForwardBackward;
import model.HMM;

public class Instance {
	public int[] words;
	public int T; //sentence length
	Corpus c;	
	ForwardBackward forwardBackward;
	
	public Instance(Corpus c, String line) {
		this.c = c;
		//read from line
		populateWordArray(line);
	}
	
	public void doInference(HMM model) {
		ForwardBackward forwardBackward = new ForwardBackward(model, this);
		forwardBackward.doInference();
	}
	public void clearInference() {
		
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
