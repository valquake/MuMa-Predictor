package predictor_fbp_refactor.rscomputational;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.googlecode.javaewah.EWAHCompressedBitmap;
import com.googlecode.javaewah.IntIterator;

import predictor_fbp_refactor.Reaction;
import predictor_fbp_refactor.Utils;

public class RSComputationalBitSetNOI extends RSComputationalBitSet {

	public RSComputationalBitSetNOI(List<Reaction> rs, LinkedHashSet<String> all_instances){
		super(rs, all_instances);
	}

	@Override
	protected boolean filterEnabledRWithBitSet(int i, EWAHCompressedBitmap bs_t) {
        return reactants.get(i).and(bs_t).cardinality() == reactants.get(i).cardinality();
	}

	@Override
	protected int[] findContradiction(EWAHCompressedBitmap bs_must_tmp, EWAHCompressedBitmap bs_must_not_tmp) {
		IntIterator it = bs_must_tmp.intIterator();
		int len = this.map_instances.values().size() / 2;
		
		while(it.hasNext()) {
			int next = it.next();
			if(next < len) 
				if(bs_must_tmp.get(next + len))
					return new int[] {next}; 
		}
		
		
		return null;
	}

	@Override
	protected List<EWAHCompressedBitmap> excludeOppositeInstances(EWAHCompressedBitmap bs_must,
			EWAHCompressedBitmap bs_maybe, EWAHCompressedBitmap bs_must_not, EWAHCompressedBitmap bs_maybe_not) {
		List<EWAHCompressedBitmap> ll = new ArrayList<>();
		
		// exclude instances opposite to must instances presented in maybe
        IntIterator it = bs_must.intIterator();
        int len = this.map_instances.size() / 2;
        
        while(it.hasNext()) {
        	int i = it.next();
        	if(i < len && bs_maybe.get(i + len))
        		bs_maybe.clear(i + len);
        	else if(i >= len && bs_maybe.get(i - len))
        		bs_maybe.clear(i - len);
        }
        
        ll.add(bs_must);
        ll.add(bs_maybe);
        ll.add(bs_must_not);
        ll.add(bs_maybe_not);
        
        return ll;
	}

	@Override
	protected Set<Set<String>> generateAllPossibilities(String elementAt) {
		Set<String> lit_formula = new LinkedHashSet<String>(Arrays.asList(elementAt.split("\\s+")));
		Set<Set<String>> possibilities =  new LinkedHashSet<>();
		possibilities.add(lit_formula);
		
		return possibilities;
	}

	@Override
	protected boolean findTrueFormulaSubsequence(String t, String s) {
		return Utils.isSubSequence(t, s, t.length(), s.length());
	}

	@Override
	protected List<EWAHCompressedBitmap> excludeDummyNodeBitSet(List<EWAHCompressedBitmap> ll) {
		// clear from all must/maybe sets the eventual presence of create
		ll.get(0).clear(map_instances.get("create"));
		ll.get(1).clear(map_instances.get("create"));
		ll.get(2).clear(map_instances.get("create") + map_instances.size());
		ll.get(3).clear(map_instances.get("create") + map_instances.size());
		return ll;
	}

}
