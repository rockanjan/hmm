package program;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import model.HMMBase;
import model.HMMFinalState;
import model.HMMNoFinalState;
import model.HMMNoFinalStateLog;
import model.HMMType;
import model.inference.Decoder;
import model.train.EM;
import config.Config;
import corpus.Corpus;
import corpus.Instance;
import corpus.InstanceList;

public class Main {
	public static int USE_THREAD_COUNT = 1;
	/** user parameters **/
	static String delimiter = "\\+";
	public static int numIter;
	public static long seed = 1;
	public static Random random;
	public static String trainFile;
	public static String vocabFile;
	public static String testFile;
	public static String devFile;

	public static String outFileTrain;
	public static String outFileTest;
	public static String outFileDev;

	public static String outFolderPrefix;
	public static int numStates;
	public static int vocabThreshold; //only above this included
	static HMMBase model;
	static Corpus corpus;
	static HMMType modelType;

	/** user parameters end **/
	public static void main(String[] args) throws IOException {
		Config.setup();
		random =  new Random(seed);
		if(Config.devFilename != null && !Config.devFilename.isEmpty()) {
			Main.outFileDev = Config.decodeFolder + Config.devFilename + ".decoded";
			devFile = Config.dataFolder + Config.devFilename;
		}
		if(Config.testFilename != null && !Config.testFilename.isEmpty()) {
			Main.outFileTest = Config.decodeFolder + Config.testFilename + ".decoded";
			testFile = Config.dataFolder + Config.testFilename;
		}
		File unknownTestWord = new File("unknown_test_words.txt");
		if(unknownTestWord.exists()) {
			unknownTestWord.delete();
		}
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
		trainNew();
		//trainContinue(350); //-1 for final model
		testAll();
	}

	public static void trainNew() throws IOException {
		corpus = new Corpus("\\s+", vocabThreshold);
		//TRAIN
		corpus.readVocab(vocabFile);
		corpus.readTrain(trainFile);
		if(testFile != null)
			corpus.readTest(testFile);
		if(devFile != null)
			corpus.readDev(devFile);
		//save vocab file
		corpus.saveVocabFile(outFolderPrefix + "/model/vocab.txt");
		writeSmoothedCorpus("nepali_smoothed_train.txt");
		//System.exit(-1);
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
		model.initializeRandom(random);
		EM em = new EM(numIter, corpus, model);
		//start training with EM
		em.start();
		model.saveModel();
	}

	public static void trainContinue(int iter) throws IOException {
		corpus = new Corpus("\\s+", vocabThreshold);
		corpus.readVocabFromDictionary("out/model/vocab.txt");
		corpus.readTrain(trainFile);
		
		if(testFile != null)
			corpus.readTest(testFile);
		
		if(devFile != null)
			corpus.readDev(devFile);
		
		//writeSmoothedCorpus("combined-smoothed.txt");
		//System.exit(-1);
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
		if(iter < 0) {
			model.loadModel("/home/anjan/workspace/HMM/out/model/model_final" + "_states_" + numStates + ".txt");
		} else {
			model.loadModel("/home/anjan/workspace/HMM/out/model/model_iter_" + iter + "_states_" + numStates + ".txt");
		}
		
		EM em = new EM(numIter, corpus, model);
		em.start();
		model.saveModel();
		

	}

	public static void writeSmoothedCorpus(String outFile) {
		InstanceList instanceList = corpus.trainInstanceList;
		try {
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8"));
			for(int n=0; n<instanceList.size(); n++) {
				Instance instance = instanceList.get(n);
				for(int t=0; t<instance.T; t++) {
					String word = instance.getWord(t);
					pw.print(word + " ");
				}
				pw.println();
			}
			pw.close();
		}
		catch (IOException e) {
			System.err.format("Could not open file for writing %s\n", outFile);
			e.printStackTrace();
		}
		System.out.println("Finished writing smoothed corpus");
	}

	public static void testAll() {
		if(corpus.testInstanceList != null) {
			double testLL = corpus.testInstanceList.getLL(model);
			testLL = testLL / corpus.testInstanceList.numberOfTokens;
			double testPerplexity = Math.pow(2, -testLL/Math.log(2));
			System.out.println("Test data LL = " + testLL + " perplexity = " + testPerplexity);
			test(model, corpus.testInstanceList, outFileTest);
			//testMaxPosterior(model, corpus.testInstanceList, outFileTest + ".posterior");
			//testPosteriorDistribution(model, corpus.testInstanceList, outFileTest + ".posterior_distribution");
		}
        /*
		if(corpus.devInstanceList != null) {
			System.out.println("Dev data LL = " + corpus.devInstanceList.getLL(model));
			test(model, corpus.devInstanceList, outFileDev);
			//testMaxPosterior(model, corpus.testInstanceList, outFileDev + ".posterior");
			//testPosteriorDistribution(model, corpus.testInstanceList, outFileDev + ".posterior_distribution");
		}


		test(model, corpus.trainInstanceList, outFileTrain);
		//testMaxPosterior(model, corpus.trainInstanceList, outFileTrain + ".posterior");
		//testPosteriorDistribution(model, corpus.testInstanceList, outFileTrain + ".posterior_distribution");
        */
	}

	public static void testPosteriorDistribution(HMMBase model, InstanceList instanceList, String outFile) {
		System.out.println("Decoding Posterior distribution");
		Decoder decoder = new Decoder(model);
		try {
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8"));
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

	private static class DecoderWorker extends Thread {
		final int startIndex;
		final int endIndex;
		final InstanceList instanceList;
		final Decoder decoder;
		public DecoderWorker(int startIndex, int endIndex, InstanceList instanceList, Decoder decoder) {
			this.startIndex = startIndex;
			this.endIndex = endIndex;
			this.instanceList = instanceList;
			this.decoder = decoder;
		}

		@Override
		public void run() {
			for (int n = startIndex; n < endIndex; n++) {
				Instance instance = instanceList.get(n);
				instance.decodedStates = decoder.viterbi(instance);
			}
		}
	}

	public static void test(HMMBase model, InstanceList instanceList, String outFile) {
		System.out.println("Decoding Data");
		Decoder decoder = new Decoder(model);
		if(USE_THREAD_COUNT <=1) {
			for(int n=0; n<instanceList.size(); n++) {
				Instance instance = instanceList.get(n);
				instance.decodedStates = decoder.viterbi(instance);
			}
		} else { //multithread
			int divideSize = instanceList.size()
					/ Main.USE_THREAD_COUNT;
			List<DecoderWorker> threadList = new ArrayList<DecoderWorker>();
			int startIndex = 0;
			int endIndex = divideSize;
			for (int i = 0; i < Main.USE_THREAD_COUNT; i++) {
				DecoderWorker worker = new DecoderWorker(startIndex, endIndex, instanceList, decoder);
				threadList.add(worker);
				worker.start();
				startIndex = endIndex;
				endIndex = endIndex + divideSize;
			}
			// there might be few remainders
			DecoderWorker finalWorker = new DecoderWorker(startIndex, instanceList.size(), instanceList, decoder);
			finalWorker.start();
			threadList.add(finalWorker);
			// wait for the threads to complete
			for (DecoderWorker worker : threadList) {
				try {
					worker.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("done with decoding... writing to file");
		//start writing
		try {
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8"));
			for(int n=0; n<instanceList.size(); n++) {
				Instance instance = instanceList.get(n);
				for(int t=0; t<instance.T; t++) {
					String word = instance.getWord(t);
					int state = instance.decodedStates[t];
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
		System.out.println("finished writing to file!");
	}

	public static void testMaxPosterior(HMMBase model, InstanceList instanceList, String outFile) {
		System.out.println("Decoding Data with Max Posterior");
		Decoder decoder = new Decoder(model);
		try{
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8"));
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
		sb.append("\nvocab thres : " + vocabThreshold);
		sb.append("\nseed : " + seed);
		System.out.println(sb.toString());
	}
}
