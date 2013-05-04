package model.param;

public class HMMParamNoFinalState extends HMMParamBase{
	public HMMParamNoFinalState(int nrStates, int nrObs) {
		super(nrStates, nrObs);
		nrStatesWithFake = nrStates;
	}
}
