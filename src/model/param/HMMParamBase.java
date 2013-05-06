package model.param;

import java.util.Random;

import model.HMMBase;
import model.HMMType;

public abstract class HMMParamBase {
	public MultinomialBase initial;
	public MultinomialBase transition;
	public MultinomialBase observation;
	
	public HMMBase model;

	int nrStatesWithFake = -1; //the extending class should initialize this (for no fake, equals nrStates)
	int nrStates = -1;
	int nrObs = -1;
	
	
	public HMMParamBase(HMMBase model) {
		this.model = model;
		nrStates = model.nrStates;
		nrObs = model.nrObs;		
	}
	
	public void initializeZeros() {
		if(model.hmmType == HMMType.LOG_SCALE) {
			
		} else {
			initial = new MultinomialRegular(nrStates, 1);
			transition = new MultinomialRegular(nrStatesWithFake, nrStates);
			observation = new MultinomialRegular(nrObs, nrStates);
		}
	}
	
	public void initialize(Random r) {
		if(model.hmmType == HMMType.LOG_SCALE) {
			System.out.println("Log Scale Implementation");
		} else {
			initial = new MultinomialRegular(nrStates, 1);
			transition = new MultinomialRegular(nrStatesWithFake, nrStates);
			observation = new MultinomialRegular(nrObs, nrStates);
		}
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
	
	public void cloneFrom(HMMParamBase source) {
		initial.cloneFrom(source.initial);
		observation.cloneFrom(source.observation);
		transition.cloneFrom(source.transition);
	}
	
	public void clear() {
		initial = null;
		transition = null;
		observation = null;
	}
	
	@Override
	public boolean equals(Object other) {
		System.err.println("NOT IMPLEMENTED");
		return false;
	}
	
	public boolean equalsExact(HMMParamBase other) {
		if(nrStates != other.nrStates || nrObs != other.nrObs || nrStatesWithFake != other.nrStatesWithFake) {
			return false;
		}
		return (this.initial.equalsExact(other.initial) &&
				this.transition.equalsExact(other.transition) &&
				this.observation.equalsExact(other.observation));
	}
	
	public boolean equalsApprox(HMMParamBase other) {
		if(nrStates != other.nrStates || nrObs != other.nrObs || nrStatesWithFake != other.nrStatesWithFake) {
			return false;
		}
		return (this.initial.equalsApprox(other.initial) &&
				this.transition.equalsApprox(other.transition) &&
				this.observation.equalsApprox(other.observation));
	}
}
