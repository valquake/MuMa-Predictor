package predictor_fbp_refactor.reactionsystem;

import predictor_fbp_refactor.Utils;

public class RSFactory {
	
	public static RS getRS(String[] R, String[] I, String[] P) {
		if(Utils.NO_INHIBITORS)
			return new ReactionSystemNoInhibitors(R, I, P);
		else
			return new ReactionSystem(R, I, P);
	}

}
