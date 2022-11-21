package predictor_fbp_refactor.rscomputational;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import predictor_fbp_refactor.Maybe;
import predictor_fbp_refactor.Must;
import predictor_fbp_refactor.Pair;
import predictor_fbp_refactor.exceptions.NoReactionsFoundException;
import predictor_fbp_refactor.reactionsystem.RS;

import java.util.Set;

public class RSComputationalSetNOI extends RSComputationalSet {

	public RSComputationalSetNOI(RS rs) {
		super(rs);
		
	}

	@Override
	protected List<Set<String>> includeInhibitorsMust(Set<String> ma, Set<String> ma_n, Set<String> mu,
			Set<String> mu_n, Must actual_must) throws NoReactionsFoundException {
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
		return Pair.of(ma, ma_n);
	}

	@Override
	protected Set<String> removeMustLW(Set<String> mu, Set<String> ma) {
		return ma.stream()
				.filter(t -> {
					String t_op = Character.isLowerCase(t.charAt(0)) ? t.toUpperCase() : t.toLowerCase();
					return !mu.contains(t_op);
				})
				.collect(Collectors.toSet());
				
	}
	
	@Override
	protected Set<Set<String>> generateAllPossibilities(String elementAt) {
		Set<String> lit_formula = new LinkedHashSet<String>(Arrays.asList(elementAt.split("\\s+")));
		Set<Set<String>> possibilities =  new LinkedHashSet<>();
		possibilities.add(lit_formula);
		
		return possibilities;
	}
}
