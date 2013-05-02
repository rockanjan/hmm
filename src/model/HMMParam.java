package model;

import java.util.Random;

public class HMMParam {
	public Multinomial initial;
	public Multinomial transition;
	public Multinomial observation;

	int nrStates;
	int nrObs;
	
	public HMMParam(int nrStates, int nrObs) {
		this.nrStates = nrStates;
		this.nrObs = nrObs;
	}
	
	public void initializeZeros() {
		initial = new Multinomial(nrStates, 1);
		transition = new Multinomial(nrStates+1, nrStates);
		observation = new Multinomial(nrObs, nrStates);
	}
	
	public void initialize(Random r) {
		initial = new Multinomial(nrStates, 1);
		transition = new Multinomial(nrStates+1, nrStates); //+1 for fake state
		observation = new Multinomial(nrObs, nrStates);
		initial.initializeRandom(r);
		transition.initializeRandom(r);
		observation.initializeRandom(r);
	}
	
	public void check() { 
		initial.checkDistribution();
		transition.checkDistribution();
		observation.checkDistribution();
	}
	
	public void normalize() {
		initial.normalize();
		transition.normalize();
		observation.normalize();
	}
	
	public void cloneFrom(HMMParam source) {
		initial.cloneFrom(source.initial);
		observation.cloneFrom(source.observation);
		transition.cloneFrom(source.transition);
	}
}
