package model.param;

import model.HMMBase;

public class HMMParamNoFinalStateLog extends HMMParamBase{
	public HMMParamNoFinalStateLog(HMMBase model) {
		super(model);
		nrStatesWithFake = nrStates;
	}
}
