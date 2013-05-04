package program;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import model.HMMNoFinalState;
import model.HMMType;
import model.inference.Decoder;
import model.train.EM;
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
	static int vocabThreshold = 1; //only above this included
	static HMMNoFinalState model;
	static Corpus corpus;
	
	/** user parameters end **/
	public static void main(String[] args) throws IOException {
		outFolderPrefix = "out/";
		trainFile = "data/train.txt.SPL";
		testFile = "data/test.txt.SPL";
		vocabFile = trainFile;
		numStates = 20;
		numIter = 100;
		String outFile = "out/decoded/test.decoded.txt";
		String outFileTrain = "out/decoded/train.decoded.txt";
		HMMType modelType = HMMType.WITH_NO_FINAL_STATE;
		printParams();
		//start
		corpus = new Corpus("\\s+", vocabThreshold);
		corpus.readVocab(vocabFile);
		corpus.readTrain(trainFile);
		corpus.readTest(testFile);
		//save vocab file
		corpus.saveVocabFile(outFolderPrefix + "/model/vocab.txt");
		if(modelType == HMMType.WITH_NO_FINAL_STATE) {
			model = new HMMNoFinalState(numStates, corpus.corpusVocab.vocabSize);
		}
		Random r = new Random(seed);
		model.initializeRandom(r);
		EM em = new EM(numIter, corpus, model);
		//start training with EM
		em.start();
		//test
		//model = new HMM();
		//model.loadModel("/home/anjan/workspace/HMM/out/model/model_iter_48_states_400.txt");
		test(model, corpus.testInstanceList, outFile);		
		test(model, corpus.trainInstanceList, outFileTrain);
	}
	
	public static void test(HMMNoFinalState model, InstanceList instanceList, String outFile) {
		System.out.println("Decoding Data");
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
		System.out.println("Finished decoding");
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
