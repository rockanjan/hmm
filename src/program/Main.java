package program;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import model.EM;
import model.HMM;
import model.Decoder;
import corpus.Corpus;
import corpus.Instance;
import corpus.InstanceList;
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
		trainFile = "/home/anjan/workspace/HMM/data/train.txt.small.SPL";
		testFile = "/home/anjan/workspace/HMM/data/test.txt.SPL";
		vocabFile = trainFile;
		numStates = 2;
		numIter = 40;
		
		printParams();
		
		//start
		corpus = new Corpus("\\s+", 1);
		corpus.readVocab(vocabFile);
		corpus.readTrain(trainFile);
		corpus.readTest(testFile);
		model = new HMM(numStates, corpus.corpusVocab.vocabSize);
		Random r = new Random(seed);
		model.initializeRandom(r);
		EM em = new EM(numIter, corpus, model);
		//start training with EM
		em.start();
		
		String outFile = "/home/anjan/workspace/HMM/out/decoded/test.decoded.txt";
		test(model, corpus.testInstanceList, outFile);
	}
	
	public static void test(HMM model, InstanceList instanceList, String outFile) {
		Decoder decoder = new Decoder(model);
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(outFile));
			for(int n=0; n<instanceList.size(); n++) {
				Instance instance = instanceList.get(n);
				int[] decoded = decoder.viterbi(instance);
				for(int t=0; t<decoded.length; t++) {
					String word = instance.getWord(t);
					int state = decoded[t];
					pw.println(state + "\t" + word);
				}
				pw.println();
			}
			pw.close();
		} catch (IOException e) {
			System.err.format("Could not open file for writing %s\n", outFile);
			e.printStackTrace();
		}
		System.out.println("Finished Test decoding");
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
