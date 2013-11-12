package config;

import corpus.Corpus;
import program.Main;
import model.train.EM;

public class Config {
	public static void setup() {
		Main.numIter = 500;
		Main.numStates = 50;
		Main.vocabThreshold = 3; //only above this included
		
		String trainFilename = "nepali_train.txt";
		String devFilename = "nepali_dev.txt";
		String testFilename = "nepali_test.txt";
		Main.USE_THREAD_COUNT = 2;
		//EM.sampleSentenceSize = Integer.MAX_VALUE;
		EM.sampleSentenceSize = 10000;
		Corpus.sampleSequential = true;
		EM.alpha = 0.5;
		
		Main.seed = 2;
		String decodeFolder = "out/decoded/";
		String dataFolder = "data/nepali/";
		
		Main.trainFile = dataFolder + trainFilename;
		Main.devFile = dataFolder + devFilename;
		Main.testFile = dataFolder + testFilename;
		
		Main.outFolderPrefix = "out/";
		Main.vocabFile = Main.trainFile;
		Main.outFileTrain = decodeFolder + trainFilename + ".decoded";
		Main.outFileDev = decodeFolder + devFilename + ".decoded";
		Main.outFileTest = decodeFolder + testFilename + ".decoded";
		System.out.println("Number of threads : " + Main.USE_THREAD_COUNT);
	}
	
	
}
