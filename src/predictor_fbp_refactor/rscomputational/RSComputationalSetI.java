package predictor_fbp_refactor.rscomputational;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import predictor_fbp_refactor.Maybe;
import predictor_fbp_refactor.Must;
import predictor_fbp_refactor.Pair;
import predictor_fbp_refactor.Utils;
import predictor_fbp_refactor.exceptions.NoReactionsFoundException;
import predictor_fbp_refactor.reactionsystem.RS;

public class RSComputationalSetI extends RSComputationalSet {
	Set<String> instances;
	public RSComputationalSetI(RS rs) {
		super(rs);
		instances = rs.allInstancesOfRS();
	}

	@Override
	protected List<Set<String>> includeInhibitorsMust(Set<String> ma, Set<String> ma_n, Set<String> mu,
			Set<String> mu_n, Must actual_must) throws NoReactionsFoundException {
		Set<String> must_list_not = actual_must.getMust_not();
        for(String el: must_list_not){
            Map.Entry<Must, Maybe> pair = neg_literals.get(el);

            if(pair == null){
                throw new NoReactionsFoundException(el);
            }

            mu = Sets.union(mu, pair.getKey().getMust());
            mu_n = Sets.union(mu_n, pair.getKey().getMust_not());
            ma = Sets.union(ma, pair.getValue().getMaybe());
            ma_n = Sets.union(ma_n, pair.getValue().getMaybeNot());       
        }
        
        List<Set<String>> ret = new ArrayList<>();
        ret.add(ma);
        ret.add(ma_n);
        ret.add(mu);
        ret.add(mu_n);
        
        return ret;
	}

	@Override
	protected Entry<Set<String>, Set<String>> includeInhibitorsMaybe(Set<String> ma, Set<String> ma_n,
			Maybe actual_maybe) throws NoReactionsFoundException {
		Set<String> maybe_list_not = actual_maybe.getMaybeNot();

        for(String el : maybe_list_not){
            Map.Entry<Must, Maybe> pair = neg_literals.get(el);
            
            if(pair == null){
                throw new NoReactionsFoundException(el);
            }

            ma = Sets.union(ma, pair.getKey().getMust());
            ma_n = Sets.union(ma_n, pair.getKey().getMust_not());
            ma = Sets.union(ma, pair.getValue().getMaybe());
            ma_n = Sets.union(ma_n, pair.getValue().getMaybeNot());
        }
        
        return Pair.of(ma, ma_n);
	}

	@Override
	protected Set<String> removeMustLW(Set<String> mu, Set<String> ma) {
		Set<String> mu_lw = mu.stream()
                .map((t) -> {
                    return t.toLowerCase();
                })
                .collect(Collectors.toSet());
        Set<String> mu_up = mu.stream()
                .map((t) -> {
                    return t.toUpperCase();
                })
                .collect(Collectors.toSet());


        // Remove the elements that are opposite to the must-haves, 
        // as they will always produce something inconsistent
        ma = Sets.difference(ma, mu_lw);
        ma = Sets.difference(ma, mu_up);
        
        return ma;
	}

	@Override
	protected Set<Set<String>> generateAllPossibilities(String elementAt) {
		// Transform string formula into a literals list (both positive and negative)
        List<String> lit_formula = Arrays.asList(elementAt.split("\\s+"));

        // create a list containing only negative literals
        String not = "¬";
        List<String> lit_formula_not = lit_formula.stream()
                .filter((t) -> {
                    return t.contains(not);
                })
                .sorted((o1, o2) -> {
                    return o1.substring(1).compareTo(o2.substring(1));
                })
                .collect(Collectors.toList());
        
        // create a string formed by only positive listerals
        String all_other = lit_formula.stream()
                .filter((t) -> {
                    return !t.contains(not);
                })
                .sorted()
                .reduce((el, u) -> {
                                return el + " " + u + " "; 
                })
                .orElse("");   
        
      
        // create a set of all literals involved in rs and
        // only leaves those literals that are missing from the not formula, 
        // since negative literals force their absence 
        LinkedHashSet<String> all_lit = instances.stream()
                .filter((t) -> {
                    return !lit_formula_not.contains(not + t);
                })
                .sorted((o1, o2) -> {
                    return o1.compareTo(o2);
                })
                .collect(Collectors.toCollection(LinkedHashSet<String>::new)); 
        
        List<String> all_other_a = Arrays.asList(all_other.split("\\s+"));
        return Utils.getAllCombinations(all_lit).stream()
                .filter((t) -> {
                    /*String s = t.stream()
                            .reduce((el, u) -> {
                                return el + " " + u + " "; 
                            })
                            .orElse("");
                    
                    return Utils.isSubSequence(all_other, s, all_other.length(), s.length());*/
                	return t.containsAll(all_other_a);
                })
                .collect(Collectors.toSet());
	}
}
