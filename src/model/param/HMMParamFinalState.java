package model.param;

import model.HMMBase;

public class HMMParamFinalState extends HMMParamBase{
	public HMMParamFinalState(HMMBase model) {
		super(model);
		nrStatesWithFake = nrStates + 1;
	}
}
