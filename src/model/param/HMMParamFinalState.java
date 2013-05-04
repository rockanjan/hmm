package model.param;

public class HMMParamFinalState extends HMMParamBase{
	public HMMParamFinalState(int nrStates, int nrObs) {
		super(nrStates, nrObs);
		nrStatesWithFake = nrStates + 1;
	}
}
