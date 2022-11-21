package predictor_fbp_refactor.rscomputational;

import java.util.Map.Entry;
import java.util.stream.Collectors;
import com.google.common.collect.Sets;

import predictor_fbp_refactor.Maybe;
import predictor_fbp_refactor.Must;
import predictor_fbp_refactor.Pair;
import predictor_fbp_refactor.Utils;
import predictor_fbp_refactor.exceptions.MustContradictionException;
import predictor_fbp_refactor.exceptions.NoReactionsFoundException;
import predictor_fbp_refactor.multithread.ResultThread;
import predictor_fbp_refactor.reactionsystem.RS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class RSComputationalSet extends RSComputationalStrategy {
	private HashMap<String, Map.Entry<Must, Maybe>> literals;
    protected HashMap<String, Map.Entry<Must, Maybe>> neg_literals;
    private RS reaction_system;
    
    public RSComputationalSet(RS rs) {
    	Set<String> list_of_products = rs.allProducts();
        this.literals = new HashMap<>();
        this.neg_literals = new HashMap<>();
        this.reaction_system = rs;
        
        
        for(String el: list_of_products){
            Set<String> all_rs = rs.allReactantsOfAProduct(el);
            Set<String> all_rs_musts = rs.getMustsR(el);
            
            Set<String> all_is = rs.allInhibitorsOfAProduct(el);
            Set<String> all_is_musts = rs.getMustsI(el);
            
            if(Utils.DEBUG)
            	System.out.printf("%s - R%s R_Must%s I%s I_Must%s", el, all_rs.toString(), all_rs_musts.toString(), all_is.toString(), all_is_musts.toString());
                       
            literals.put(el, Pair.of(new Must(Sets.newLinkedHashSet(all_rs_musts), Sets.newLinkedHashSet(all_is_musts)), 
                                    new Maybe(Sets.newLinkedHashSet(Sets.difference(all_rs, all_rs_musts)), 
                                            Sets.newLinkedHashSet(Sets.difference(all_is, all_is_musts)))));
        }
        
        for(String e: literals.keySet()){
            Map.Entry<Must, Maybe> pair = negativity(literals.get(e));
            neg_literals.put(e, pair);
        }
        
        if(Utils.DEBUG) {
        	System.out.printf("literals: %s\n", literals.toString());
        	System.out.printf("neg_literals: %s\n", neg_literals.toString());
        }
    }

    public void setProduct(String product) {
        this.req_prod = Sets.newHashSet(product.split(","));
    }
    
    private Map.Entry<Must, Maybe> negativity(Map.Entry<Must, Maybe> lit) {
        Must must_lit = lit.getKey();
        Maybe maybe_lit = lit.getValue();
        
        
        // We have only Must constructor, but not Maybe constructor
        if(must_lit.getSize() != 0 && maybe_lit.isEmpty()){
            // negative literals are obtained by swapping the not list with the non-not list.
            if(must_lit.getSize() > 1){
                // We have to move all in maybe, we are sure that we have only 1 reaction, so
                // min = max = 1
                return Pair.of(new Must(Sets.newLinkedHashSet(), Sets.newLinkedHashSet()),
                        new Maybe(must_lit.getMust_not(), must_lit.getMust()));
            }else{
                return Pair.of(new Must(must_lit.getMust_not(), must_lit.getMust()),
                        new Maybe());
            }
        }
        // We have both Maybe and Must constructors, we should put
        // all in Maybe, but we are sure that min is 1, 
        // because we have at least one instance in must in common
        // to all the conjunctions
        if(must_lit.getSize() != 0 && !maybe_lit.isEmpty()){
            return Pair.of(new Must(Sets.newLinkedHashSet(), Sets.newLinkedHashSet()),
                    new Maybe(Sets.newLinkedHashSet(Sets.union(must_lit.getMust_not(), maybe_lit.getMaybeNot())),
                            Sets.newLinkedHashSet(Sets.union(must_lit.getMust(), maybe_lit.getMaybe()))));
        }
        
        if(maybe_lit.getMaybe().size() + maybe_lit.getMaybeNot().size() == 1){
            // this is a particular case, in which we have a certain number of reactions formed
            // by 1 reactant, so all maybe literals have to be put into must
            return Pair.of(new Must(maybe_lit.getMaybeNot(), maybe_lit.getMaybe()),
                    new Maybe());
        }
        
        return Pair.of(new Must(Sets.newLinkedHashSet(), Sets.newLinkedHashSet()),
                    new Maybe(Sets.newLinkedHashSet(Sets.union(must_lit.getMust_not(), maybe_lit.getMaybeNot())),
                            Sets.newLinkedHashSet(Sets.union(must_lit.getMust(), maybe_lit.getMaybe()))));
    }
	
	@Override
	public Entry<Must, Maybe> computeApproximatePredictor(Set<String> ps, int steps)
			throws MustContradictionException, NoReactionsFoundException {
		Map.Entry<Must, Maybe> actual = Pair.of(new Must(Sets.newLinkedHashSet(ps), Sets.newLinkedHashSet()),
                new Maybe(Sets.newLinkedHashSet(), Sets.newLinkedHashSet()));
		this.req_prod = ps;
		this.steps = steps;
		
		if(Utils.DEBUG)
        	System.out.printf("Mu%s -> ", ps.toString());

		int tot_steps = steps + 1;
		steps++;
        while(steps > 1){
            Must actual_must = actual.getKey();
            Maybe actual_maybe = actual.getValue();

            Set<String> mu = Sets.newLinkedHashSet();
            Set<String> mu_n = Sets.newLinkedHashSet();
            Set<String> ma = Sets.newLinkedHashSet();
            Set<String> ma_n = Sets.newLinkedHashSet();

            List<Maybe> ll = new ArrayList<>();

            Set<String> must_list = actual_must.getMust();

            for(String el: must_list){
                Map.Entry<Must, Maybe> pair = literals.get(el);

                if(pair == null){
                    throw new NoReactionsFoundException(el);
                }

                mu = Sets.union(mu, pair.getKey().getMust());
                mu_n = Sets.union(mu_n, pair.getKey().getMust_not());
                ma = Sets.union(ma, pair.getValue().getMaybe());
                ma_n = Sets.union(ma_n, pair.getValue().getMaybeNot());
            }
            
            List<Set<String>> must_not_computation = includeInhibitorsMust(ma, ma_n, mu, mu_n, actual_must);
            
            ma = must_not_computation.get(0);
            ma_n = must_not_computation.get(1);
            mu = must_not_computation.get(2);
            mu_n = must_not_computation.get(3);

            // there is a maybe
            if(!actual_maybe.isEmpty()){
                Set<String> maybe_list = actual_maybe.getMaybe();

                for(String el : maybe_list){
                    Map.Entry<Must, Maybe> pair = literals.get(el);

                    if(pair == null){
                        throw new NoReactionsFoundException(el);
                    }

                    ma = Sets.union(ma, pair.getKey().getMust());
                    ma_n = Sets.union(ma_n, pair.getKey().getMust_not());
                    ma = Sets.union(ma, pair.getValue().getMaybe());
                    ma_n = Sets.union(ma_n, pair.getValue().getMaybeNot());
                }
                
                Map.Entry<Set<String>, Set<String>> maybe_not_computation = includeInhibitorsMaybe(ma, ma_n, actual_maybe);
                
                ma = maybe_not_computation.getKey();
                ma_n = maybe_not_computation.getValue();
            }
            
            // Remove the elements that are opposite to the must-haves, 
            // as they will always produce something inconsistent
            ma = removeMustLW(mu, ma);           
            ma = Sets.difference(ma, mu_n);
            ma_n = Sets.difference(ma_n, mu);
            
            LinkedHashSet<String> final_ma = Sets.newLinkedHashSet(Sets.difference(ma, mu));
            LinkedHashSet<String> final_ma_n = Sets.newLinkedHashSet(Sets.difference(ma_n, mu_n));
            LinkedHashSet<String> final_mu = Sets.newLinkedHashSet(mu);
            LinkedHashSet<String> final_mu_n = Sets.newLinkedHashSet(mu_n);
            
            Set<String> interSect;
            if(!(interSect = Sets.intersection(final_mu, final_mu_n)).isEmpty())
                throw new MustContradictionException(interSect.toString() + " at the step " + (tot_steps - steps + 1));
            
            actual = Pair.of(new Must(final_mu, final_mu_n), new Maybe(final_ma, final_ma_n));
            
            if(Utils.DEBUG)
            	System.out.printf("Mu%s Ma%s MuN%s MaN%s -> ", final_mu.toString(), final_ma.toString(), final_mu_n.toString(), final_ma_n.toString());
             
            steps = steps - 1;
        }  
       
        if(Utils.DEBUG)
        	System.out.printf("\n");
        
        if(Utils.DUMMY_NODE) {
        	actual.getKey().excludeDummyNode();
        	actual.getValue().excludeDummyNode();
        }
        	
        
        this.predictor_overapprox = actual;           
        return actual;
	}
	

	protected abstract List<Set<String>> includeInhibitorsMust(Set<String> ma, Set<String> ma_n, Set<String> mu, Set<String> mu_n,
			Must actual_must) throws NoReactionsFoundException;

	protected abstract Entry<Set<String>, Set<String>> includeInhibitorsMaybe(Set<String> ma, Set<String> ma_n,
			Maybe actual_maybe) throws NoReactionsFoundException;

	protected abstract Set<String> removeMustLW(Set<String> mu, Set<String> ma);

	@Override
	public Set<String> simulateEvolutionofReactionSystem(Set<String> T) {
		if(Utils.DEBUG )
            System.out.printf("%s -> ", T);
		
		int temp_steps = this.steps;
		
		// if we have create node, we have to remember to add it in the initial context
        // (otherwise some reactions may not be legally enabled)
        if(Utils.DUMMY_NODE)
        	T.add("create");
        
		Set<String> T_alias = Sets.newLinkedHashSet(T);
		
		while(temp_steps > 1){
	        
            Set<String> product_result = Sets.newLinkedHashSet();
            Set<Set<String>> resultReactionsEnabledByT = this.reaction_system.resultsFromReactsEnabledByT(T_alias);
            
            for(Set<String> ps : resultReactionsEnabledByT){
            	product_result = Sets.union(product_result, ps);
            }

            if(T_alias.equals(product_result) || product_result.isEmpty()) {
            	if(Utils.DEBUG ) 
                    System.out.printf("%s -> ", T_alias);
            	
            	T_alias = product_result;
                break;
            }
            
            T_alias = product_result;
            
            if(Utils.DEBUG )
                System.out.printf("%s -> ", T_alias);
            temp_steps = temp_steps - 1;
        }
        
        if(Utils.DEBUG )
            System.out.printf("\n");
        
        LinkedHashSet<String> ordered_actual = Sets.newLinkedHashSet(T_alias)
                .stream()
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet<String>::new));

        return ordered_actual;
	}

	@Override
	public LinkedHashSet<LinkedHashSet<LinkedHashSet<String>>> getAllCyclesMultithread() {
		throw new UnsupportedOperationException("Multithread is not supported for Set Implementation, if you want to use\n"
				+ "it, please, set efficient implementation flag");
	}
	
	@Override
	public List<ResultThread> simulateEvolutionofReactionSystemForAllFormulasMultithread(List<String> formulas) {
		throw new UnsupportedOperationException("Multithread is not supported for Set Implementation, if you want to use\n"
				+ "it, please, set efficient implementation flag");
	}
	
	@Override
	public LinkedHashSet<LinkedHashSet<LinkedHashSet<String>>> getAllCycles() {
		throw new UnsupportedOperationException("Attractors optimization is not supported for Set Implementation, if you want to use\n"
				+ "it, please, set efficient implementation flag");
	}
}
