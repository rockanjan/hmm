package model.param;

import model.HMMBase;

public class HMMParamRegular extends HMMParamBase{
	public HMMParamRegular(HMMBase model) {
		super(model);
		nrStatesWithFake = nrStates;
	}
}
