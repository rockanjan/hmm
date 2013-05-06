package model.param;

import model.HMMBase;

public class HMMParamNoFinalState extends HMMParamBase{
	public HMMParamNoFinalState(HMMBase model) {
		super(model);
		nrStatesWithFake = nrStates;
	}
}
