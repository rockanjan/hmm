package corpus;

import java.util.ArrayList;

import model.HMMBase;

public class InstanceList extends ArrayList<Instance>{
	private static final long serialVersionUID = -2409272084529539276L;
	
	public double getLL(HMMBase model) {
		double LL = 0;
		for (int n=0; n<this.size(); n++) {
			Instance instance = get(n);
			instance.doInference(model);
			LL += instance.forwardBackward.logLikelihood;
			instance.clearInference();
		}
		return LL;
	}
	
	public InstanceList(){
		super();
	}
	
}
