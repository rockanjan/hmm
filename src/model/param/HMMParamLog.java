package model.param;

import model.HMMBase;

public class HMMParamLog extends HMMParamBase{
	public HMMParamLog(HMMBase model) {
		super(model);
		nrStatesWithFake = nrStates;
	}
}
