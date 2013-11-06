package config;

import corpus.Corpus;
import program.Main;
import model.train.EM;

public class Config {
	public static void setup() {
		Main.numIter = 201;
		Main.numStates = 100;
		Main.vocabThreshold = 3; //only above this included
		
		String trainFilename = "brown_train.txt";
		String devFilename = "brown_dev.txt";
		String testFilename = "brown_test.txt";
		Main.USE_THREAD_COUNT = 2;
		EM.sampleSentenceSize = Integer.MAX_VALUE;
		//EM.sampleSentenceSize = 2000;
		Corpus.sampleSequential = true;
		EM.alpha = 0.5;
		
		Main.seed = 1;
		String decodeFolder = "out/decoded/";
		String dataFolder = "data/";
		
		Main.trainFile = dataFolder + trainFilename;
		//Main.devFile = dataFolder + devFilename;
		Main.testFile = dataFolder + testFilename;
		
		Main.outFolderPrefix = "out/";
		Main.vocabFile = Main.trainFile;
		Main.outFileTrain = decodeFolder + trainFilename + ".decoded";
		//Main.outFileDev = decodeFolder + devFilename + ".decoded";
		Main.outFileTest = decodeFolder + testFilename + ".decoded";
		System.out.println("Number of threads : " + Main.USE_THREAD_COUNT);
	}
	
	
}
