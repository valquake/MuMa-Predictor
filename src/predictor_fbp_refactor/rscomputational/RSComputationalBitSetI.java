package predictor_fbp_refactor.rscomputational;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import com.googlecode.javaewah.EWAHCompressedBitmap;
import com.googlecode.javaewah.IntIterator;

import predictor_fbp_refactor.Reaction;
import predictor_fbp_refactor.Utils;

public class RSComputationalBitSetI extends RSComputationalBitSet {
	private Set<String> instances;
	
	public RSComputationalBitSetI(List<Reaction> rs, LinkedHashSet<String> all_instances){
		super(rs, all_instances);
		instances = Set.of();
		for(Reaction r : rs)
			instances = Sets.union(instances, Sets.union(r.products(), r.inhibitors()));
	}

	@Override
	protected boolean filterEnabledRWithBitSet(int i, EWAHCompressedBitmap bs_t) {        
        return reactants.get(i).and(bs_t).cardinality() == reactants.get(i).cardinality() 
        		&& inhibitors.get(i).and(bs_t).cardinality() == 0;
	}

	@Override
	protected int[] findContradiction(EWAHCompressedBitmap bs_must_tmp, EWAHCompressedBitmap bs_must_not_tmp) {
		if(bs_must_tmp.intersects(bs_must_not_tmp))
			return bs_must_tmp.and(bs_must_not_tmp).toArray();
		else return null ;
	}

	@Override
	protected List<EWAHCompressedBitmap> excludeOppositeInstances(EWAHCompressedBitmap bs_must,
			EWAHCompressedBitmap bs_maybe, EWAHCompressedBitmap bs_must_not, EWAHCompressedBitmap bs_maybe_not) {
		List<EWAHCompressedBitmap> ll = new ArrayList<>();
		EWAHCompressedBitmap and_maybe = bs_must.and(bs_maybe);
		IntIterator it = and_maybe.intIterator();
		
		while(it.hasNext()) {
			int i = it.next();
			bs_maybe.clear(i);
		}
		
		and_maybe = bs_must.and(bs_maybe_not);
		
		it = and_maybe.intIterator();
		
		while(it.hasNext()) {
			int i = it.next();
			bs_maybe_not.clear(i);
		}
		
		and_maybe = bs_must_not.and(bs_maybe);
		
		it = and_maybe.intIterator();
		
		while(it.hasNext()) {
			int i = it.next();
			bs_maybe.clear(i);
		}
		
		ll.add(bs_must);
        ll.add(bs_maybe);
        ll.add(bs_must_not);
        ll.add(bs_maybe_not);
        
        return ll;
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
        
        List<String> all_other_a = !all_other.isEmpty()? Arrays.asList(all_other.split("\\s+")) : null;

        if(all_other_a != null)
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
        else return Sets.powerSet(all_lit);
	}

	@Override
	protected boolean findTrueFormulaSubsequence(String t, String s) {
		/***
         * In case of RS with inhibitors we need to separate subsequence checking for negative instances
         * from positive instances. If we do not, it can result that:
         * A C is a subsequence of A D -C (not is counted as a character apart)
         */
        String[] pos_neg = t.split("¬", 2);
        String[] pos_neg_s = s.split("¬", 2);

        if(pos_neg.length == 2){
            pos_neg[1] = "¬" + pos_neg[1];
        }if(pos_neg_s.length == 2){
            pos_neg_s[1] = "¬" + pos_neg_s[1];
        }


        if(pos_neg.length == 2 && pos_neg_s.length == 2)
            return Utils.isSubSequence(pos_neg[0], pos_neg_s[0], pos_neg[0].length(), pos_neg_s[0].length())
                    && Utils.isSubSequence(pos_neg[1], pos_neg_s[1], pos_neg[1].length(), pos_neg_s[1].length());
        else if(pos_neg.length == 1 && pos_neg_s.length == 1)
            return Utils.isSubSequence(pos_neg[0], pos_neg_s[0], pos_neg[0].length(), pos_neg_s[0].length());
        // e.g. pos_neg = [[A, B, C]], pos_neg_s = [[A, B, C, D],[-E]]
        // Check only if positive parts are one subsequence of the other, it does not make sense check the same
        // for negative ones.
        else if(pos_neg.length == 1 && pos_neg_s.length == 2)
            return Utils.isSubSequence(pos_neg[0], pos_neg_s[0], pos_neg[0].length(), pos_neg_s[0].length());
        else return false;
	}

	@Override
	protected List<EWAHCompressedBitmap> excludeDummyNodeBitSet(List<EWAHCompressedBitmap> ll) {
		// clear from all must/maybe sets the eventual presence of create
		ll.get(0).clear(map_instances.get("create"));
		ll.get(1).clear(map_instances.get("create"));
		ll.get(2).clear(map_instances.get("create"));
		ll.get(3).clear(map_instances.get("create"));
		return ll;
	}
}
