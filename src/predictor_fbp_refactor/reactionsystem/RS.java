package predictor_fbp_refactor.reactionsystem;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import predictor_fbp_refactor.Maybe;
import predictor_fbp_refactor.Must;
import predictor_fbp_refactor.Reaction;
import predictor_fbp_refactor.Utils;
import predictor_fbp_refactor.exceptions.MustContradictionException;
import predictor_fbp_refactor.exceptions.NoReactionsFoundException;
import predictor_fbp_refactor.multithread.ResultThread;
import predictor_fbp_refactor.rscomputational.RSComputationalStrategy;

public abstract class RS {
    protected RSComputationalStrategy over;
    public abstract void addReaction(String[] R, String[] I, String[] P);
    public abstract void deleteReaction(int index);
    public abstract List<Reaction> getListofReactions();
    public abstract Map.Entry<List<Set<String>>, List<Set<String>>> cause(String p);
    public abstract Set<String> allProducts();
    public abstract Set<String> allReactantsOfAProduct(String p);
    public abstract Set<String> allInhibitorsOfAProduct(String p);
    public abstract Set<String> allInstancesOfRS();
    public abstract Set<String> getMustsR(String p);
    public abstract Set<String> getMustsI(String p);
    public abstract Map.Entry<Must, Maybe> computeOverApproximationEfficiently(Set<String> ps, int steps) throws MustContradictionException, NoReactionsFoundException;
    public abstract Set<Set<String>> resultsFromReactsEnabledByT(Set<String> T);
    public abstract void printAllCycles();
	public abstract List<ResultThread> checkAllFormulasMultithread(List<String> formulas);
    public abstract List<ResultThread> checkAllFormulas(List<String> elementAt);
    public abstract boolean isSubsequence(String s1, String s2);
	public Set<String> getReqProd(){
		return over != null ? over.getProducts() : null;
	}
	public Map.Entry<Must, Maybe> getPredictorOverapprox(){
		return over != null ? over.getPredictor_overapprox() : null;
	}
	public boolean isOverNull() {
		return over == null;
	}
	public int getSteps() {
		return over != null ? over.getSteps() : -1;
	}
	public List<String> generateAllSets(){
		return over != null ? over.generateAllSets() : null;
	}
	public LinkedHashSet<LinkedHashSet<LinkedHashSet<String>>> getAllCycles() {
		if(Utils.MULTITHREAD)
			return this.over.getAllCyclesMultithread();
		else return this.over.getAllCycles();
	}
}