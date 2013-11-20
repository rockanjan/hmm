package corpus;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;

import model.train.EM;
import program.Main;

public class Corpus {
	public String delimiter;
	public InstanceList trainInstanceList = new InstanceList();
	public InstanceList trainInstanceListRandomized;
	// testInstanceList can be empty
	public InstanceList testInstanceList;
	public InstanceList devInstanceList;
	public InstanceList randomTrainingSampleInstanceList;

	public Vocabulary corpusVocab;

	int vocabThreshold;
	
	public int totalWords; 
	
	public static boolean sampleSequential = true;

	public Corpus(String delimiter, int vocabThreshold) {
		this.delimiter = delimiter;
		this.vocabThreshold = vocabThreshold;
	}
	
	public void readDev(String inFile) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), "UTF8"));
		String line = null;
		int totalWords = 0;
		int totalUnknown = 0;
		devInstanceList = new InstanceList();
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (!line.isEmpty()) {
				Instance instance = new Instance(this, line);
				instance.unknownList = null;
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
		devInstanceList.numberOfTokens = totalWords;
		System.out.println("Dev Instances: " + devInstanceList.size());
		System.out.println("Dev token count: " + totalWords);
		double percent = 100.0 * totalUnknown / totalWords;
        System.out.format("Dev Unknown Count = %d, precent = %.2f\n", totalUnknown, percent);
		br.close();
	}

	public void readTest(String inFile) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), "UTF8"));
		String line = null;
		int totalWords = 0;
		int totalUnknown = 0;
		testInstanceList = new InstanceList();
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (!line.isEmpty()) {
				Instance instance = new Instance(this, line);
				totalUnknown += instance.unknownCount;
				try{
					PrintWriter pw = new PrintWriter(
							new OutputStreamWriter(new FileOutputStream("unknown_test_words.txt", true), "UTF-8"));
					for(String w : instance.unknownList) {
						pw.println(w);
					}
					instance.unknownList.clear();
					instance.unknownList = null;
					pw.close();
				} catch(Exception e) {
					System.err.println("error writing unknown test word");
				}
				if (instance.words.length != 0) {
					testInstanceList.add(instance);
					totalWords += instance.words.length;
				} else {
					System.out.println("Could not read from test file, line = "
							+ line);
				}
			}
		}
		testInstanceList.numberOfTokens = totalWords;
		System.out.println("Test Instances: " + testInstanceList.size());
		System.out.println("Test token count: " + totalWords);
		double percent = 100.0 * totalUnknown / totalWords;
        System.out.format("Test Unknown Count = %d, precent = %.2f\n", totalUnknown, percent);
		br.close();
	}

	public void readTrain(String inFile) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), "UTF8"));
		String line = null;
		totalWords = 0;
		int totalUnknown = 0;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (!line.isEmpty()) {
				Instance instance = new Instance(this, line);
				instance.unknownList = null;
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
		trainInstanceList.numberOfTokens = totalWords;
		System.out.println("Train Instances: " + trainInstanceList.size());
		System.out.println("Train token count: " + totalWords);
		double percent = 100.0 * totalUnknown / totalWords;
        System.out.format("Train Unknown Count = %d, precent = %.2f\n", totalUnknown, percent);
		br.close();
	}

	public void readVocab(String inFile) throws IOException {
		corpusVocab = new Vocabulary();
		corpusVocab.vocabThreshold = vocabThreshold;
		corpusVocab.readVocabFromCorpus(this, inFile);
	}

	public void readVocabFromDictionary(String filename) throws UnsupportedEncodingException {
		corpusVocab = new Vocabulary();
		corpusVocab.readVocabFromDictionary(filename);
	}
	
	public void generateRandomTrainingSample(int size, int iterCount) {
		randomTrainingSampleInstanceList = new InstanceList();
		if(trainInstanceList.size() <= size) {
			randomTrainingSampleInstanceList.addAll(trainInstanceList);
			randomTrainingSampleInstanceList.numberOfTokens = trainInstanceList.numberOfTokens;
		} else {
			if(sampleSequential) { //sequentially
				randomTrainingSampleInstanceList = new InstanceList();
				if(trainInstanceListRandomized == null) {
					trainInstanceListRandomized = new InstanceList();
					ArrayList<Integer> randomInts = new ArrayList<Integer>();			
					for(int i=0; i<trainInstanceList.size(); i++) {
						randomInts.add(i);
					}
					Collections.shuffle(randomInts,Main.random);
					for(int i=0; i<trainInstanceList.size(); i++) {
						Instance instance = trainInstanceList.get(randomInts.get(i));
						trainInstanceListRandomized.add(instance);
						trainInstanceListRandomized.numberOfTokens += instance.T;
					}					
				}
				int startIndex = (iterCount * EM.sampleSentenceSize) % trainInstanceListRandomized.size();
				int index = startIndex;
				for(int i=0; i<size; i++) {
					Instance instance = trainInstanceListRandomized.get(index);
					randomTrainingSampleInstanceList.add(instance);
					randomTrainingSampleInstanceList.numberOfTokens += instance.T;
					index++;
					//index can get higher than the size of the training corpus
					index = index % trainInstanceListRandomized.size();
				}
			} else { //randomly
				ArrayList<Integer> randomInts = new ArrayList<Integer>();			
				for(int i=0; i<trainInstanceList.size(); i++) {
					randomInts.add(i);
				}
				Collections.shuffle(randomInts,Main.random);
				for(int i=0; i<size; i++) {
					Instance instance = trainInstanceList.get(randomInts.get(i));
					randomTrainingSampleInstanceList.add(instance);				
					randomTrainingSampleInstanceList.numberOfTokens += instance.T;
				}
			}
		}				
	}
	
	
	public void saveVocabFile(String filename) throws UnsupportedEncodingException {
		PrintWriter dictionaryWriter;
		try {
			dictionaryWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-8"));
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
