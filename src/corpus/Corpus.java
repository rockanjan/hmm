package corpus;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Corpus {
	public String delimiter;
	public InstanceList trainInstanceList = new InstanceList();
	// testInstanceList can be empty
	public InstanceList testInstanceList;
	public InstanceList devInstanceList;
	public InstanceList randomTrainingSampleInstanceList;
	Random random = new Random(4321);

	public Vocabulary corpusVocab;

	int vocabThreshold;
	
	public int totalWords; 

	public Corpus(String delimiter, int vocabThreshold) {
		this.delimiter = delimiter;
		this.vocabThreshold = vocabThreshold;
	}
	
	public void readDev(String inFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(inFile));
		String line = null;
		int totalWords = 0;
		int totalUnknown = 0;
		devInstanceList = new InstanceList();
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (!line.isEmpty()) {
				Instance instance = new Instance(this, line);
				totalUnknown += instance.unknownCount;
				if (instance.words.length != 0) {
					devInstanceList.add(instance);
					totalWords += instance.words.length;
				} else {
					System.out.println("Could not read from dev file, line = "
							+ line);
				}
			}
		}
		System.out.println("Dev Instances: " + devInstanceList.size());
		System.out.println("Dev token count: " + totalWords);
		System.out.println("Dev unknown count : " + totalUnknown);
		br.close();
	}

	public void readTest(String inFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(inFile));
		String line = null;
		int totalWords = 0;
		int totalUnknown = 0;
		testInstanceList = new InstanceList();
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (!line.isEmpty()) {
				Instance instance = new Instance(this, line);
				totalUnknown += instance.unknownCount;
				if (instance.words.length != 0) {
					testInstanceList.add(instance);
					totalWords += instance.words.length;
				} else {
					System.out.println("Could not read from test file, line = "
							+ line);
				}
			}
		}
		System.out.println("Test Instances: " + testInstanceList.size());
		System.out.println("Test token count: " + totalWords);
		System.out.println("Test unknown count : " + totalUnknown);
		br.close();
	}

	public void readTrain(String inFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(inFile));
		String line = null;
		totalWords = 0;
		int totalUnknown = 0;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (!line.isEmpty()) {
				Instance instance = new Instance(this, line);
				totalUnknown += instance.unknownCount;
				if (instance.words.length != 0) {
					trainInstanceList.add(instance);
					totalWords += instance.words.length;
				} else {
					System.err
							.println("Could not read from train file, line = "
									+ line);
				}
			}
		}
		System.out.println("Train Instances: " + trainInstanceList.size());
		System.out.println("Train token count: " + totalWords);
		System.out.println("Train unknown count : " + totalUnknown);
		br.close();
	}

	public void readVocab(String inFile) throws IOException {
		corpusVocab = new Vocabulary();
		corpusVocab.vocabThreshold = vocabThreshold;
		corpusVocab.readVocabFromCorpus(this, inFile);
	}

	public void readVocabFromDictionary(String filename) {
		corpusVocab = new Vocabulary();
		corpusVocab.readVocabFromDictionary(filename);
	}
	
	public void generateRandomTrainingSample(int size) {
		randomTrainingSampleInstanceList = new InstanceList();
		if(trainInstanceList.size() <= size) {
			randomTrainingSampleInstanceList.addAll(trainInstanceList);
		} else {
			ArrayList<Integer> randomInts = new ArrayList<Integer>();			
			for(int i=0; i<trainInstanceList.size(); i++) {
				randomInts.add(i);
			}
			Collections.shuffle(randomInts,random);
			for(int i=0; i<size; i++) {
				Instance instance = trainInstanceList.get(randomInts.get(i));
				randomTrainingSampleInstanceList.add(instance);				
			}			
		}
	}	

	public void saveVocabFile(String filename) {
		PrintWriter dictionaryWriter;
		try {
			dictionaryWriter = new PrintWriter(filename);
			int V = corpusVocab.vocabSize;
			dictionaryWriter.println(V);
			for (int v = 0; v < V; v++) {
				dictionaryWriter.println(corpusVocab.indexToWord.get(v));
				dictionaryWriter.flush();
			}
			dictionaryWriter.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void debug() {
		StringBuffer sb = new StringBuffer();
		sb.append("DEBUG: Corpus\n");
		sb.append("=============\n");
		sb.append("vocab size : " + corpusVocab.vocabSize);
		sb.append("\nvocab frequency: \n");
		for (int i = 0; i < corpusVocab.vocabSize; i++) {
			sb.append(i + "\t" + corpusVocab.indexToWord.get(i) + " --> "
					+ corpusVocab.indexToFrequency.get(i));
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
