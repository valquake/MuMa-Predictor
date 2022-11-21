package predictor_fbp_refactor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.googlecode.javaewah.EWAHCompressedBitmap;

public class AttractorDetector {
	private HashMap<EWAHCompressedBitmap, List<EWAHCompressedBitmap>> definitive_cycle;
	private ArrayList<EWAHCompressedBitmap> possible_cycle;
	
	public AttractorDetector() {
		this.definitive_cycle = new HashMap<>();
		this.possible_cycle = new ArrayList<EWAHCompressedBitmap>();
	}
	
	public EWAHCompressedBitmap addAndCheckForPossibleCycle(EWAHCompressedBitmap actual_product, int partial_steps) {
		partial_steps--;
		List<EWAHCompressedBitmap> cycle = definitive_cycle.get(actual_product);
		if(cycle == null) {
			if(possible_cycle.contains(actual_product)) {
				
				if(Utils.DEBUG)
					System.out.printf("(Found a new cycle %s)", possible_cycle.toString());
				
				// you find a new cycle
				int init_index = possible_cycle.indexOf(actual_product);
				possible_cycle.add(actual_product);
				
				List<EWAHCompressedBitmap> copy_cycle = new ArrayList<>(possible_cycle);
				
				// build new entries for the new cycle, for all the members of the cycle
				// store partial path that lead to the first node of the cycle (called "starter")
				// e.g. the cycle A -> BC -> C -> A will be stored in definitive_cycle as:
				// 		A | BC -> C -> A
				// 		BC | C -> A
				// 		C | A
				for(int i = init_index; i < copy_cycle.size() - 1; i++) {
					List<EWAHCompressedBitmap> partial_list = copy_cycle.subList(i + 1, copy_cycle.size());
					
					if(i == init_index)
						cycle = partial_list;
					
					definitive_cycle.put(copy_cycle.get(i), partial_list);
				}
				
				possible_cycle.clear();
				
				if(partial_steps == 0)
					return actual_product;
				else // time to skip to the result
					return skipToResult(cycle ,partial_steps);
				
			}else {
				// not again a cycle, just add to arraylist
				possible_cycle.add(actual_product);
			}
		}else {
			if(Utils.DEBUG)
				System.out.printf("(Found cycle %s)", cycle.toString());
			
			if(partial_steps == 0)
				return actual_product;
			else // we are actually into a cycle, so jump to the result
				return skipToResult(cycle, partial_steps);
		}

		// we didn't find yet a cycle, so we don't return a result
		return null;
	}
	
	public void cleanPossibleCycle() {
		possible_cycle.clear();
	}

	private EWAHCompressedBitmap skipToResult(List<EWAHCompressedBitmap> cycle, int partial_steps) {
		int l = cycle.size();
		int r = partial_steps;
		
		// it is sufficient the partial cycle for answering
		if(r <= l) {
			return r != 0 ? cycle.get(r-1) : cycle.get(r);
		}else {
			
			// synchronize to the starting point of the cycle
			r -= l;
			EWAHCompressedBitmap starter = cycle.get(l-1);
			List<EWAHCompressedBitmap> complete_cycle = definitive_cycle.get(starter);
			int c = complete_cycle.size();
			
			// and then compute the relative position of the result
			int jump = (r % c) - 1;
			
			// this is the case in which the cycle length is a multiple of the partial steps
			// it's like not doing any jump at all
			if(jump == -1)
				return starter;
			// otherwise, jump to the right result
			else
				return complete_cycle.get(jump);
		}
		
	}
	
	public void printAllCycles() {
		
		System.out.printf("///////////////////////CYCLES///////////////////\n");
		for(Entry<EWAHCompressedBitmap, List<EWAHCompressedBitmap>> entry: definitive_cycle.entrySet()) {
			List<EWAHCompressedBitmap> ll = entry.getValue();
			int len = ll.size();
			
			if(ll.get(len - 1).equals(entry.getKey())) {
				List<EWAHCompressedBitmap> copy_ll = new ArrayList<>(ll);
				EWAHCompressedBitmap last = copy_ll.remove(len - 1);
				copy_ll.add(0, last);
				System.out.println(copy_ll);
			}
		}
		System.out.printf("////////////////////////////////////////////////\n");
	}

	public List<List<EWAHCompressedBitmap>> getAllCycles() {
		List<List<EWAHCompressedBitmap>> list_cycles = new ArrayList<>();
		for(Entry<EWAHCompressedBitmap, List<EWAHCompressedBitmap>> entry: definitive_cycle.entrySet()) {
			List<EWAHCompressedBitmap> ll = entry.getValue();
			int len = ll.size();
			
			if(ll.get(len - 1).equals(entry.getKey())) {
				List<EWAHCompressedBitmap> copy_ll = new ArrayList<>(ll);
				EWAHCompressedBitmap last = copy_ll.remove(len - 1);
				copy_ll.add(0, last);
				list_cycles.add(copy_ll);
			}
		}
		
		return list_cycles;
	}

}
