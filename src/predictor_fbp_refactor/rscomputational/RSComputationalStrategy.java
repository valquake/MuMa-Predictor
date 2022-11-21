package predictor_fbp_refactor.rscomputational;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import predictor_fbp_refactor.Maybe;
import predictor_fbp_refactor.Must;
import predictor_fbp_refactor.Utils;
import predictor_fbp_refactor.exceptions.MustContradictionException;
import predictor_fbp_refactor.exceptions.NoReactionsFoundException;
import predictor_fbp_refactor.multithread.ResultThread;


public abstract class RSComputationalStrategy {
	protected int steps;
	protected Map.Entry<Must, Maybe> predictor_overapprox;
	protected Set<String> req_prod;
	
	public abstract Map.Entry<Must, Maybe> computeApproximatePredictor(Set<String> ps, int steps) throws MustContradictionException, NoReactionsFoundException;
	protected abstract Set<String> simulateEvolutionofReactionSystem(Set<String> T);
	protected abstract Set<Set<String>> generateAllPossibilities(String elementAt);
	public abstract LinkedHashSet<LinkedHashSet<LinkedHashSet<String>>> getAllCyclesMultithread();
	public abstract List<ResultThread> simulateEvolutionofReactionSystemForAllFormulasMultithread(List<String> formulas);
	public abstract LinkedHashSet<LinkedHashSet<LinkedHashSet<String>>> getAllCycles();
	
	public boolean checkFormula(String elementAt) {
		/**
         * It is not necessary to check formula for all possibilities,
         * since no inhibitors systems enjoy the addition property of 
         * instances in the result sequences
         * 
         * It is sufficient to check only for the truth on minimal
         * set of instances satisfying the formula
         */
    	Set<Set<String>> all_possibilities = generateAllPossibilities(elementAt);
        Set<String> result;

        if(all_possibilities.isEmpty())
            return false;
        
        for(Set<String> p : all_possibilities){
        	
        	if(Utils.DEBUG)
        		System.out.printf("%s || ", elementAt);
            result = simulateEvolutionofReactionSystem(p);
            
            if(!(result).containsAll(getProducts())){
                if(Utils.DEBUG)
                    System.out.println("Possibility " + p + " for formula " + elementAt + " is a counterexample\nIt produces " + result);
                return false;
            }
            
        }
        
        return true;
    }
	
	public int getSteps() {
		return steps;
	}
	public Set<String> getProducts() {
		return req_prod;
	}
	
	/**
	 * @return the predictor_overapprox
	 */
	public Map.Entry<Must, Maybe> getPredictor_overapprox() {
		return predictor_overapprox;
	}
	public List<String> generateAllSets() {
        String not = Utils.NO_INHIBITORS ? "[" : "¬";
        
        LinkedHashSet<String> must_not_withnot = Sets.newLinkedHashSet();
        
        LinkedHashSet<String> maybe_not = this.predictor_overapprox.getValue().getMaybeNot();
        LinkedHashSet<String> maybe = this.predictor_overapprox.getValue().getMaybe();
        LinkedHashSet<String> must = this.predictor_overapprox.getKey().getMust();
        LinkedHashSet<String> must_not = this.predictor_overapprox.getKey().getMust_not();
        
        Set<String> all_literals = Sets.union(maybe, maybe_not);
        
        int common = 0, max_cardinality_formula = 0;
        
        // da rivedere, è cambiata la logica
        if(Utils.NO_INHIBITORS && Utils.ONLY_COMPLETE_FORMULA){
            // count all equals letters 
            for(String letter : all_literals){
                String pattern = ".*" + letter + ".*" + letter + ".*";
                common += maybe
                            .toString()
                            .toLowerCase()
                            .matches(pattern) ? 1 : 0;
            }
            
        }
        
        if(Utils.ONLY_COMPLETE_FORMULA)
            max_cardinality_formula = Utils.NO_INHIBITORS? 
                    must.size() + maybe.size() - common
                    : 
                    must.size() + maybe.size() + maybe_not.size() + must_not.size() 
                    - Sets.intersection(must, must_not).size() - Sets.intersection(maybe, maybe_not).size();
        
        System.out.printf("max_card = %d\n", max_cardinality_formula);

        if(!Utils.NO_INHIBITORS)
            must_not_withnot = Sets.newLinkedHashSet(must_not)
                    .stream()
                    .map((t) -> {
                        return not + t;
                    })
                    .collect(Collectors.toCollection(LinkedHashSet<String>::new));
        
        if(!Utils.NO_INHIBITORS)
            maybe_not = maybe_not
                    .stream()
                    .map((t) -> {
                        return not + t;
                    })
                    .collect(Collectors.toCollection(LinkedHashSet<String>::new));
        
        List<String> alternative2 = Utils.getAllCombinationsAndFilter(Sets.union(maybe, maybe_not), must, must_not_withnot);
        System.out.println("ALTERNATIVE size = "+alternative2.size());
        /*Set<Set<String>> alternative = Sets.powerSet(Sets.union(maybe, maybe_not));
       
        
        LinkedHashSet<LinkedHashSet<String>> allmaybe_sets = Sets.newLinkedHashSet(alternative)
                .stream()
                .sorted((o1, o2) -> {
                    return Integer.valueOf(o1.size()).compareTo(o2.size());
                })
                .map((t) -> {
                    return Sets.newLinkedHashSet(t);
                })
                .collect(Collectors.toCollection(LinkedHashSet<LinkedHashSet<String>>::new));
        
        for(LinkedHashSet<String> s: allmaybe_sets){
            s.addAll(must);
            s.addAll(must_not_withnot);
        }
        
        int max_card = max_cardinality_formula;

        List<String> set_solutions = allmaybe_sets
                                    .stream()
                                    
                                    //If we want only completely characterized formula,
                                    //just return all formulas of max cardinality
                                    .filter((t) -> {
                                        boolean notempty = !t.isEmpty();
                                        
                                        if(Utils.ONLY_COMPLETE_FORMULA)
                                            return t.size() == max_card;
                                        
                                        return notempty;
                                    })
                                    .map((t) -> {
                                        return t.stream()
                                                //
                                                // We need to sort sets of set_solutions in the following order:
                                                // All positive literals in alphabetical order and then
                                                // all negative literals in alphabetical order
                                                //
                                                .sorted((o1, o2) -> {
                                                    if(o1.startsWith(not) && o2.startsWith(not)){
                                                        return o1.substring(1).compareTo(o2.substring(1));
                                                    }
                                                    if(o1.startsWith(not) && !o2.startsWith(not)){
                                                        return 1; // o1 is greater than o2
                                                    }
                                                    if(!o1.startsWith(not) && o2.startsWith(not)){
                                                        return -1; // o1 is less than o2
                                                    }

                                                    return o1.compareTo(o2);
                                                })
                                                 //
                                                 // Then we can transform every set in a unique string
                                                 //
                                                .reduce((s, u) -> {
                                                    return s + " "+ u + " ";
                                                }).orElse("");                                      
                                    })
                                    .collect(Collectors.toCollection(ArrayList<String>::new));
        
        
        
        // filters away all the strings in which a literal and its negated appear, 
        // since they are all logical contradictions
        // (equivalent) examples are: A a -> B, A ¬A -> B
        for(String letter : all_literals){
            set_solutions = set_solutions
                        .stream()
                        .filter((t) -> {
                            // this is a pattern to find more than one occurrence
                            // of the same letter
                        	String replaced = letter.replaceAll("\\[(.*?)\\]", "$1");
                            String pattern = ".*" + replaced + ".*" + replaced + ".*";
                            return !t.matches(pattern) && !t.toLowerCase().matches(pattern);
                        })
                        .collect(Collectors.toCollection(ArrayList<String>::new));
        }
        
        System.out.println("AFTER OLD PROCESS: "+set_solutions.size());*/
        
        // remember to add also must + must not alone
        alternative2.addAll(0, Sets.union(must, must_not_withnot));
        return alternative2.stream().sorted((o1, o2) -> {
		        	return o1.split("\\s+").length - o2.split("\\s+").length;
        		})
        		.collect(Collectors.toList());
    }
	
	
}
