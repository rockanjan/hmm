package corpus;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Corpus {
	public String delimiter;
	public InstanceList trainInstanceList = new InstanceList();
	//testInstanceList can be empty
	public InstanceList testInstanceList;
	
	public Vocabulary corpusVocab; 
	
	int vocabThreshold;
	
	public Corpus(String delimiter, int vocabThreshold) {
		this.delimiter = delimiter;
		this.vocabThreshold = vocabThreshold;
	}
	
	public void readTest(String inFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(inFile));
		String line = null;
		int totalWords = 0;
		testInstanceList = new InstanceList();		 
		while( (line = br.readLine()) != null ) {
			line = line.trim();
			if(! line.isEmpty()) {
				Instance instance = new Instance(this, line);
				if(instance.words.length != 0) {
					testInstanceList.add(instance);
					totalWords += instance.words.length;
				} else {
					System.out.println("Could not read from test file, line = " + line);
				}
			}
		}
		System.out.println("Test Instances: " + testInstanceList.size());
		System.out.println("Test token count: " + totalWords);
		br.close();
	}
	
	public void readTrain(String inFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(inFile));
		String line = null;
		int totalWords = 0;
		while( (line = br.readLine()) != null ) {
			line = line.trim();
			if(! line.isEmpty()) {
				Instance instance = new Instance(this, line);
				if(instance.words.length != 0) {
					trainInstanceList.add(instance);
					totalWords += instance.words.length;
				} else {
					System.err.println("Could not read from train file, line = " + line);
				}
			}
		}
		System.out.println("Train Instances: " + trainInstanceList.size());
		System.out.println("Train token count: " + totalWords);
		br.close();
	}
	
	public void readVocab(String inFile) throws IOException {
		corpusVocab = new Vocabulary();
		corpusVocab.featureThreshold = vocabThreshold;
		corpusVocab.readVocabFromFile(this, inFile);		
	}
	
	public void readVocabFromDictionary(String filename) {
		corpusVocab = new Vocabulary();
		corpusVocab.readVocabFromDictionary(filename);
	}
	
	public void debug() {
		StringBuffer sb = new StringBuffer();
		sb.append("DEBUG: Corpus\n");
		sb.append("=============\n");
		sb.append("vocab size : " + corpusVocab.vocabSize);
		sb.append("\nvocab frequency: \n");
		for(int i=0; i<corpusVocab.vocabSize; i++) {
			sb.append("\t" + corpusVocab.indexToWord.get(i) + " --> " + corpusVocab.indexToFrequency.get(i));
			sb.append("\n");
		}
		System.out.println(sb.toString());
	}
	
	public static void main(String[] args) throws IOException {
		String inFile = "/home/anjan/workspace/HMM/data/test.txt.SPL";
		int vocabThreshold = 1;
		Corpus c = new Corpus("\\s+", vocabThreshold);
		c.readVocab(inFile);
	}
}
