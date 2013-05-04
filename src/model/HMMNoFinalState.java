package model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import model.param.HMMParamBase;
import model.param.HMMParamNoFinalState;

public class HMMNoFinalState extends HMMBase{
	public HMMNoFinalState() {
		super();
	}

	public HMMNoFinalState(int nrStates, int nrObs) {
		super();
		this.nrStates = nrStates;
		this.nrObs = nrObs;
		this.hmmType = HMMType.WITH_NO_FINAL_STATE;
	}

	public void initializeRandom(Random r) {
		param = new HMMParamNoFinalState(nrStates, nrObs);
		param.initialize(r);
	}
	
	public void initializeZeros() {
		param = new HMMParamNoFinalState(nrStates, nrObs);
		param.initializeZeros();
	}

	public void checkModel() {
		param.check();
	}

	public void updateFromCounts(HMMParamBase counts) {
		counts.normalize();
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
				for (int j = 0; j < nrStates; j++) {
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
		//TODO: currently loads the exact same file format saved
		try {
			BufferedReader br = new BufferedReader(new FileReader(location));
			try{
				this.nrStates = Integer.parseInt(br.readLine());
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
					if(nrStates != splitted.length) {
						br.close();
						System.err.format("nrStates=%d, from file=%d\n", nrStates, splitted.length);
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
			this.param = new HMMParamNoFinalState(nrStates, nrObs);
		}
		this.param.cloneFrom(other.param);
	}
	
	
	public static void main(String[] args) {
		//check saving and loading model
		int nrStates = 20;
		int nrObs = 50;
		HMMNoFinalState hmm = new HMMNoFinalState(nrStates, nrObs);
		hmm.initializeRandom(new Random());
		HMMParamBase beforeSaving = new HMMParamNoFinalState(nrStates, nrObs);
		beforeSaving.initializeZeros();
		beforeSaving.cloneFrom(hmm.param);
		String fileSaved = hmm.saveModel();
		hmm.param.clear();
		hmm = null;
		
		HMMNoFinalState loaded = new HMMNoFinalState();
		loaded.loadModel(fileSaved);
		if(beforeSaving.equalsExact(loaded.param)) {
			System.out.println("Saved and Loaded models match exactly");
		} else if(beforeSaving.equalsApprox(loaded.param)) {
			System.out.println("Saved and Loaded models match approx");
		} else {
			System.out.println("Saved and Loaded models do not match");
		}
		
	}
}
