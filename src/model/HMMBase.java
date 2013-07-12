package model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import model.param.HMMParamBase;
import model.param.HMMParamNoFinalState;

public abstract class HMMBase {
	public int nrStatesWithFake = -1; //the extending class should initialize this (for no fake, equals nrStates)
	public int nrStates = -1;
	public int nrObs = -1;
	public HMMParamBase param;
	String baseDir = "out/model/";
	
	public HMMType hmmType;
	
	public abstract void initializeRandom(Random r);
	public abstract void initializeZeros();

	public void checkModel() {
		param.check();
	}

	public void updateFromCounts(HMMParamBase counts) {
		//counts.normalize();
		counts.normalize(param);
		param.cloneFrom(counts);
	}
	
	public String saveModel() {
		return saveModel(-1);
	}
	/*
	 * return the location saved
	 */
	public String saveModel(int iterCount) {
		String modelFile;
		if(iterCount < 0) {
			modelFile = baseDir + "model_final_states_" + nrStates + ".txt";
		} else {
			modelFile = baseDir + "model_iter_" + iterCount + "_states_" + nrStates + ".txt";
		}
		PrintWriter pw;
		try {
			pw = new PrintWriter(modelFile);

			pw.println(nrStates);
			pw.println(nrObs);
			pw.println();
			// initial
			for (int i = 0; i < nrStates; i++) {
				pw.print(param.initial.get(i, 0));
				if (i != nrStates) {
					pw.print(" ");
				}
			}
			pw.println();
			pw.println();
			// transition
			for (int i = 0; i < nrStates; i++) {
				for (int j = 0; j < nrStatesWithFake; j++) {
					pw.print(param.transition.get(j, i));
					if (j != nrStates) {
						pw.print(" ");
					}
				}
				pw.println();
			}
			pw.println();
			for (int i = 0; i < nrStates; i++) {
				for (int j = 0; j < nrObs; j++) {
					pw.print(param.observation.get(j, i));
					if (j != nrObs) {
						pw.print(" ");
					}
				}
				pw.println();
			}
			pw.println();
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		return modelFile;
	}

	public void loadModel(String location) {
		//TODO: currently loads only the exact same file format saved
		try {
			BufferedReader br = new BufferedReader(new FileReader(location));
			try{
				nrStates = Integer.parseInt(br.readLine());
				if(hmmType == HMMType.WITH_FINAL_STATE) {
					nrStatesWithFake = nrStates + 1;
				} else if(hmmType == HMMType.WITH_NO_FINAL_STATE) {
					nrStatesWithFake = nrStates;
				}
				this.nrObs = Integer.parseInt(br.readLine());
				this.initializeZeros();
				br.readLine();
				//load initial
				String splitted[] = br.readLine().split("(\\s+|\\t+)");
				if(nrStates != splitted.length) {
					br.close();
					throw new RuntimeException("Loading model, Initial parameters not matching number of states");
				}
				for(int i=0; i<nrStates; i++) {
					double prob = Double.parseDouble(splitted[i]);
					this.param.initial.set(i, 0, prob);
				}
				br.readLine();
				//transition
				for(int i=0; i<nrStates; i++) {
					splitted = br.readLine().split("(\\s+|\\t+)");
					if(nrStatesWithFake != splitted.length) {
						br.close();
						System.err.format("For transition: nrStates=%d, from file=%d\n", nrStatesWithFake, splitted.length);
						throw new RuntimeException("Loading model, transition parameters not matching number of states");
					}
					for(int j=0; j<splitted.length; j++) {
						double prob = Double.parseDouble(splitted[j]);
						this.param.transition.set(j, i, prob);
					}
				}
				br.readLine();
				//observation
				for(int i=0; i<nrStates; i++) {
					splitted = br.readLine().split("(\\s+|\\t+)");
					if(nrObs != splitted.length) {
						br.close();
						System.err.format("nrStates=%d, from file=%d\n", nrObs, splitted.length);
						throw new RuntimeException("Loading model, obs parameters not matching number of states");
					}
					for(int j=0; j<splitted.length; j++) {
						double prob = Double.parseDouble(splitted[j]);
						this.param.observation.set(j, i, prob);
					}
				}
				br.close();
				System.out.format("Model loaded successfully with %d states and %d observations \n", nrStates, nrObs);
			} catch(NumberFormatException e) {
				e.printStackTrace();
				System.err.println("Error loading model");
				System.exit(-1);
			}			
		} 
		catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void clone(HMMNoFinalState other) {
		if(this.param == null) {
			this.param = new HMMParamNoFinalState(this);
		}
		this.param.cloneFrom(other.param);
	}
}
