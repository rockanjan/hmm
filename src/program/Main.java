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
	public static int USE_THREAD_COUNT = 8;
	/** user parameters **/
	static String delimiter = "\\+";
	static int numIter;
	static long seed = 4321;
	
	static String trainFile;
	static String vocabFile;
	static String testFile;
	static String devFile;
	static String outFolderPrefix;
	static int numStates; 	
	static int vocabThreshold = 2; //only above this included
	static HMMBase model;
	static Corpus corpus;
	static HMMType modelType;
	
	/** user parameters end **/
	public static void main(String[] args) throws IOException {		
		System.out.println("Number of threads : " + USE_THREAD_COUNT);
		//defaults
		outFolderPrefix = "out/";
		trainFile = "data/combined_pos.all.txt";
		devFile = "data/pos.dev.txt";
		testFile = "data/pos_ul.test.notag";
		vocabFile = trainFile;
		numStates = 80;
		numIter = 100;
		String outFileTrain = "out/decoded/combined_pos.all.txt.decoded";
		String outFileDev = "out/decoded/srl.decoded.txt";
		String outFileTest = "out/decoded/pos_ul.test.notag.decoded";
		//modelType = HMMType.LOG_SCALE;
		modelType = HMMType.WITH_NO_FINAL_STATE;
		
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
		printParams();
		corpus = new Corpus("\\s+", vocabThreshold);
		
		
		//TRAIN
		corpus.readVocab(vocabFile);
		corpus.readTrain(trainFile);
		corpus.readTest(testFile);
		corpus.readDev(devFile);
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
		corpus.readTrain(trainFile);
		corpus.readTest(testFile);
		corpus.readDev(devFile);
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
		model.loadModel("/home/anjan/workspace/HMM/out/model/model_final_states_80.txt");
		*/
		
		if(corpus.testInstanceList != null) {
			System.out.println("Test data LL = " + corpus.testInstanceList.getLL(model));
			test(model, corpus.testInstanceList, outFileTest);
			//testMaxPosterior(model, corpus.testInstanceList, outFileTest + ".posterior");
			//testPosteriorDistribution(model, corpus.testInstanceList, outFileTest + ".posterior_distribution");
		}
		/*
		if(corpus.devInstanceList != null) {
			System.out.println("Dev data LL = " + corpus.devInstanceList.getLL(model));
			test(model, corpus.devInstanceList, outFileDev);
			testMaxPosterior(model, corpus.testInstanceList, outFileDev + ".posterior");
			//testPosteriorDistribution(model, corpus.testInstanceList, outFileDev + ".posterior_distribution");
		}
		*/
		test(model, corpus.trainInstanceList, outFileTrain);
		//testMaxPosterior(model, corpus.trainInstanceList, outFileTrain + ".posterior");
		//testPosteriorDistribution(model, corpus.testInstanceList, outFileTrain + ".posterior_distribution");
		
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
		}
		catch (IOException e) {
			System.err.format("Could not open file for writing %s\n", outFile);
			e.printStackTrace();
		}
		System.out.println("Finished decoding");
	}
	
	public static void testMaxPosterior(HMMBase model, InstanceList instanceList, String outFile) {
		System.out.println("Decoding Data with Max Posterior");
		Decoder decoder = new Decoder(model);
		try{
			PrintWriter pw = new PrintWriter(new FileWriter(outFile));
			for(int n=0; n<instanceList.size(); n++) {
				Instance instance = instanceList.get(n);
				int[] decoded = decoder.posterior(instance);
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
	}
	
	public static void printParams() {
		StringBuffer sb = new StringBuffer();
		sb.append("Train file : " + trainFile);
		sb.append("\nVocab file : " + vocabFile);
		sb.append("\nTest file : " + testFile);
		sb.append("\nDev file : " + devFile);
		sb.append("\noutFolderPrefix : " + outFolderPrefix);
		sb.append("\nIterations : " + numIter);
		sb.append("\nNumStates : " + numStates);
		System.out.println(sb.toString());
	}
}
