package corpus;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import util.SmoothWord;

import model.HMMBase;
import model.HMMType;
import model.inference.ForwardBackward;
import model.inference.ForwardBackwardLog;
import model.inference.ForwardBackwardNoScaling;
import model.inference.ForwardBackwardScaled;
import model.param.HMMParamBase;
import model.param.MultinomialRegular;

public class Instance {
	public int[] words;
	public int T; //sentence length
	Corpus c;	
	public ForwardBackward forwardBackward;
	public int nrStates;
	public int unknownCount;
	public List<String> unknownList = new ArrayList<String>();
	
	public Instance(Corpus c, String line) {
		this.c = c;
		unknownCount = 0;
		//read from line
		populateWordArray(line);
	}
	
	public void doInference(HMMBase model) {
		//forwardBackward = new ForwardBackwardNoScaling(model, this);
		if(model.hmmType == HMMType.LOG_SCALE) {
			forwardBackward = new ForwardBackwardLog(model, this);
		} else {
			forwardBackward = new ForwardBackwardScaled(model, this);
			//forwardBackward = new ForwardBackwardNoScaling(model, this);
		}
		nrStates = forwardBackward.model.nrStates;
		forwardBackward.doInference();
	}
	
	public void clearInference() {
		forwardBackward.clear();
		forwardBackward = null;
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
						unknownList.add(word);
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
