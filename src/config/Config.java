package config;

import model.train.EM;
import program.Main;
import corpus.Corpus;

public class Config {
	public static String trainFilename;
	public static String devFilename;
	public static String testFilename;

	public static String decodeFolder;
	public static String dataFolder;

	public static void setup() {
		Main.numIter = 100;
		Main.numStates = 50;
		Main.vocabThreshold = 1; //only above this included

		trainFilename = "conll2000_train.txt.SPL";
        devFilename = "conll2000_test.txt.SPL";
        testFilename = "conll2000_test.txt.SPL";

		Main.USE_THREAD_COUNT = 2;
		EM.sampleSentenceSize = Integer.MAX_VALUE;
		//EM.sampleSentenceSize = 5000;
		Corpus.sampleSequential = true;
		EM.alpha = 0.5;

		Main.seed = 1;
		decodeFolder = "out/decoded/";
		dataFolder = "data/";

		Main.trainFile = dataFolder + trainFilename;
        Main.devFile = dataFolder + devFilename;
        Main.testFile = dataFolder + testFilename;

		Main.outFolderPrefix = "out/";
		Main.vocabFile = Main.trainFile;
		Main.outFileTrain = decodeFolder + trainFilename + ".decoded";
		System.out.println("Number of threads : " + Main.USE_THREAD_COUNT);
	}
}
