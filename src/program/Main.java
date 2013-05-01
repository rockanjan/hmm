package program;

import java.io.IOException;
import java.util.Random;

import model.EM;
import model.HMM;
import corpus.Corpus;
import corpus.Vocabulary;

public class Main {
	
	/** user parameters **/
	static String delimiter = "\\+";
	static int numIter;
	static long seed = 1;
	
	static String trainFile;
	static String vocabFile;
	static String testFile;
	static String outFolderPrefix;
	static int numStates; 	
	static int vocabThreshold = 1;
	static HMM model;
	static Corpus corpus;
	
	/** user parameters end **/
	public static void main(String[] args) throws IOException {
		outFolderPrefix = "/home/anjan/workspace/HMM/data/out/";
		trainFile = "/home/anjan/workspace/HMM/data/train.txt.SPL";
		testFile = "/home/anjan/workspace/HMM/data/test.txt.SPL";
		vocabFile = trainFile;
		numStates = 2;
		numIter = 10;
		
		printParams();
		
		//start
		corpus = new Corpus("\\s+", 1);
		corpus.readVocab(vocabFile);
		corpus.readTrain(trainFile);
		
		model = new HMM(numStates, corpus.corpusVocab.vocabSize);
		Random r = new Random(seed);
		model.initializeRandom(r);
		EM em = new EM(numIter, corpus, model);
		//start training with EM
		em.start();
	}
		
	public static void printParams() {
		StringBuffer sb = new StringBuffer();
		sb.append("Train file : " + trainFile);
		sb.append("\nVocab file : " + vocabFile);
		sb.append("\nTest file : " + testFile);
		sb.append("\noutFolderPrefix : " + outFolderPrefix);
		sb.append("\nIterations : " + numIter);
		sb.append("\nNumStates : " + numStates);
		System.out.println(sb.toString());
	}
}
