package program;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import model.HMMBase;
import model.HMMFinalState;
import model.HMMNoFinalState;
import model.HMMNoFinalStateLog;
import model.HMMType;
import model.inference.Decoder;
import model.train.EM;
import corpus.Corpus;
import corpus.Instance;
import corpus.InstanceList;

public class Main {
	
	/** user parameters **/
	static String delimiter = "\\+";
	static int numIter;
	static long seed = 37;
	
	static String trainFile;
	static String vocabFile;
	static String testFile;
	static String outFolderPrefix;
	static int numStates; 	
	static int vocabThreshold = 1; //only above this included
	static HMMBase model;
	static Corpus corpus;
	static HMMType modelType;
	
	/** user parameters end **/
	public static void main(String[] args) throws IOException {
		//defaults
		outFolderPrefix = "out/";
		trainFile = "/home/anjan/workspace/HMM/data/train.txt.small.SPL";
		testFile = "/home/anjan/workspace/HMM/data/test.txt.SPL";
		vocabFile = trainFile;
		numStates = 100;
		numIter = 500;
		String outFile = "out/decoded/test.decoded.txt";
		String outFileTrain = "out/decoded/train.decoded.txt";
		modelType = HMMType.LOG_SCALE;
		
		if(args.length > 0) {
			try{
				numStates = Integer.parseInt(args[0]);
				numIter = Integer.parseInt(args[1]);
				trainFile = args[2];
				testFile = args[3];
				vocabFile = args[4];
			} catch(Exception e) {
				System.out.println("<program> numStates numIter trainFile testFile vocabFile");
				System.exit(-1);
			}
			
		}
		//modelType = HMMType.LOG_SCALE;
		//modelType = HMMType.WITH_FINAL_STATE;
		
		printParams();
		corpus = new Corpus("\\s+", vocabThreshold);
		
		
		
		//TRAIN
		corpus.readVocab(vocabFile);
		corpus.readTrain(trainFile);
		corpus.readTest(testFile);
		//save vocab file
		corpus.saveVocabFile(outFolderPrefix + "/model/vocab.txt");
		if(modelType == HMMType.WITH_NO_FINAL_STATE) {
			System.out.println("HMM with no final state");
			model = new HMMNoFinalState(numStates, corpus.corpusVocab.vocabSize);			
		} else if(modelType == HMMType.WITH_FINAL_STATE) {
			System.out.println("HMM with final state");
			System.out.println("NOT WORKING");
			System.exit(-1);
			model = new HMMFinalState(numStates, corpus.corpusVocab.vocabSize);
		} else if(modelType == HMMType.LOG_SCALE) {
			System.out.println("HMM Log scale");
			model = new HMMNoFinalStateLog(numStates, corpus.corpusVocab.vocabSize);
		}
		
		Random r = new Random(seed);
		model.initializeRandom(r);
		EM em = new EM(numIter, corpus, model);
		//start training with EM
		em.start();
		model.saveModel();
		
		
		/*
		//TEST
		corpus.readVocabFromDictionary("out/model/vocab.txt");
		corpus.readTest(testFile);
		model = new HMMNoFinalStateLog(numStates, corpus.corpusVocab.vocabSize);
		model.loadModel("/home/anjan/workspace/HMM/out/model/model_final_states_10.txt");
		*/
		
		test(model, corpus.testInstanceList, outFile);		
		//test(model, corpus.trainInstanceList, outFileTrain);
		//testPosteriorDistribution(model, corpus.testInstanceList, outFile + ".posterior_distribution");
	}
	
	public static void testPosteriorDistribution(HMMBase model, InstanceList instanceList, String outFile) {
		System.out.println("Decoding Posterior distribution");
		Decoder decoder = new Decoder(model);
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(outFile));
			for(int n=0; n<instanceList.size(); n++) {
				Instance instance = instanceList.get(n);
				double[][] decoded = decoder.posteriorDistribution(instance);
				for(int t=0; t<decoded.length; t++) {
					String word = instance.getWord(t);
					pw.print(word + " ");
					for(int i=0; i<decoded[t].length; i++) {
						pw.print(decoded[t][i]);
						if(i != model.nrStates) {
							pw.print(" ");
						}
					}
					pw.println();
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
	
	public static void test(HMMBase model, InstanceList instanceList, String outFile) {
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
