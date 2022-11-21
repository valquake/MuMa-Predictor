package predictor_fbp_refactor.reactionsystem;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Sets;

import predictor_fbp_refactor.Maybe;
import predictor_fbp_refactor.Must;
import predictor_fbp_refactor.Pair;
import predictor_fbp_refactor.Reaction;
import predictor_fbp_refactor.Utils;
import predictor_fbp_refactor.exceptions.MustContradictionException;
import predictor_fbp_refactor.exceptions.NoReactionsFoundException;
import predictor_fbp_refactor.multithread.ResultThread;
import predictor_fbp_refactor.rscomputational.RSComputationalBitSet;
import predictor_fbp_refactor.rscomputational.RSComputationalBitSetNOI;
import predictor_fbp_refactor.rscomputational.RSComputationalSetNOI;

public class ReactionSystemNoInhibitors extends RS {
	private List<Reaction> reaction_system_woI;
	private HashMap<String, Set<String>> must_product_R_woI;
	private List<Reaction> pos_react;
	
	public ReactionSystemNoInhibitors(String[] R, String[] I, String[] P) {
		this.reaction_system_woI = new ArrayList<>();
		this.pos_react = new ArrayList<>();
		pos_react.add(new Reaction(R, P, I));
		
		generateRulesWoI(R, I, P);
		must_product_R_woI = new HashMap<>();
		String[] I_lw = Arrays.stream(I)
						.map(t -> "["+t+"]")
						.filter(t -> t.isEmpty())
						.toArray(String[]::new);
		
		String[] both = Stream.of(R, I_lw).flatMap(Stream::of)
                .toArray(String[]::new);
		
		for(String p: P){
            must_product_R_woI.put(p, Set.of(both));
        }
	}
	
	@Override
	public void addReaction(String[] R, String[] I, String[] P) {
		// check if this reaction has create as product (in this case create
		// is considered as a virtual entity, it will be omitted from
		// the computation
		if(!Utils.DUMMY_NODE)
			if(Arrays.asList(P).contains("create"))
				Utils.DUMMY_NODE = true;
		
		generateRulesWoI(R, I, P);
		recomputeMusts();
		pos_react.add(new Reaction(R, P, I));
		
		if(Utils.DEBUG){
			System.out.println("----------------------------");
			for(Reaction a : reaction_system_woI){
                System.out.printf("%s -> %s \n", a.reactants().toString(), a.products().toString());
            }
			
			System.out.printf("\n");
			
            System.out.println("\nR's Musts After Insert [no inhibitors]: ");
            for (Map.Entry<String, Set<String>> entry : must_product_R_woI.entrySet()) {
                String key = entry.getKey();
                Set<String> value = entry.getValue();
                System.out.print(key + " " + value + " | ");
            }
            
            System.out.printf("\n");
            System.out.println("----------------------------");
        }
	}

	@Override
	public void deleteReaction(int index) {
		Reaction to_delete = pos_react.get(index);
		Set<String> prod = to_delete.products();
		
		System.out.println(to_delete.products().toString());
		System.out.println(to_delete.reactants().toString());
		System.out.println(to_delete.inhibitors().toString());
		System.out.println(Reaction.generateNoIReaction(to_delete.reactants(), to_delete.inhibitors()).toString());
		
		for(String p : prod) {
			this.reaction_system_woI = this.reaction_system_woI
			.stream()
			.filter(t -> !t.products().contains(p) || 
						!t.reactants().equals(Reaction.generateNoIReaction(to_delete.reactants(), to_delete.inhibitors())))
			.collect(Collectors.toList());
		}
		recomputeMusts();
		pos_react.remove(index);
		
		if(Utils.DEBUG){
			System.out.println("----------------------------");
			for(Reaction a : reaction_system_woI){
                System.out.printf("%s -> %s \n", a.reactants().toString(), a.products().toString());
            }
			
            System.out.println("\nR's Musts After Delete [no inhibitors]: ");
            for (Map.Entry<String, Set<String>> entry : must_product_R_woI.entrySet()) {
                String key = entry.getKey();
                Set<String> value = entry.getValue();
                System.out.print(key + " " + value + " | ");
            }
            System.out.print("\n");
            System.out.println("----------------------------");
        }
	}
	
	private void recomputeMusts() {
		must_product_R_woI = new HashMap<>();
		Set<String> pds = this.allProducts();
        
        for(String p : pds){
            List<Reaction> product_reactions = this.reaction_system_woI
                        .stream()
                        .filter((t) -> {
                            return t.isProduct(p);
                        })
                        .collect(Collectors.toList());
            
            for(Reaction r : product_reactions){
                try{
                Set<String> all_r = this.must_product_R_woI.get(p);
                
                // Such a case may originate from a earlier deletion that caused 
                // the removal of all reactions that have p as a product.
                if(all_r.isEmpty()){
                    boolean firstReaction = this.reaction_system_woI
                        .stream()
                        .filter((t) -> {
                            return t.isProduct(p);
                        })
                        .findAny()
                        .isEmpty();
                    
                    if(firstReaction)
                        this.must_product_R_woI.put(p, r.reactants());
                }
                else{
                    Set<String> int_r = Sets.intersection(all_r, r.reactants());                
                    this.must_product_R_woI.put(p, int_r);
                }
                }catch(NullPointerException e){
                    // this exception comes from get, no instance of p in the hashtable
                    // We have to put into must
                    must_product_R_woI.put(p, r.reactants());
                }                           
            }
        }
	}

	@Override
	public Entry<List<Set<String>>, List<Set<String>>> cause(String p) {
		List<Set<String>> cause_pos = new ArrayList();
        List<Set<String>> cause_neg = new ArrayList();

        for(Reaction r: this.reaction_system_woI){
            if(r.isProduct(p)){
                List<Set<String>> ap = new ArrayList(r.applicability());
                cause_pos.add(ap.get(1));
            }
        }  

        return Pair.of(cause_pos, cause_neg);
	}
	
	

	@Override
	public Set<String> allProducts() {
		Set<String> pds = new HashSet<>();
        
        for(Reaction r: this.reaction_system_woI){
            pds = Sets.union(pds, r.products());
        }
        
        return pds;
	}

	@Override
	public Set<String> allReactantsOfAProduct(String p) {
		Set<String> Rs = Set.of(); 
        for(Reaction r: this.reaction_system_woI){
            if(r.isProduct(p))
                Rs = Sets.union(Rs, r.reactants());
        }
        
        return Rs;
	}

	@Override
	public Set<String> allInstancesOfRS() {
		Set<String> instances = Set.of();
        for(Reaction r: this.reaction_system_woI){
            instances = Sets.union(instances, Sets.union(r.products(), Sets.union(r.inhibitors(), r.reactants())));
        }
        
        return instances;
	}

	@Override
	public Set<String> getMustsR(String p) {		
		return this.must_product_R_woI.get(p);
	}
	
	@Override
	public Set<String> getMustsI(String p) {		
		return Sets.newLinkedHashSet();
	}

	@Override
	public Entry<Must, Maybe> computeOverApproximationEfficiently(Set<String> ps, int steps)
			throws MustContradictionException, NoReactionsFoundException {
		if(Utils.EFFICIENT_IMPLEMENTATION)
				this.over = new RSComputationalBitSetNOI(this.reaction_system_woI, Sets.newLinkedHashSet(allInstancesOfRS()));
		else
			this.over = new RSComputationalSetNOI(this);
		
		if(Utils.EFFICIENT_IMPLEMENTATION)
			System.out.println(((RSComputationalBitSetNOI) this.over).computeUnderApproximation(ps, steps + 1));
		return this.over.computeApproximatePredictor(ps, steps);
	}
	
	@Override
	public boolean isSubsequence(String t, String s) {
		return Utils.isSubSequence(t, s, t.length(), s.length());
	}

    @Override
    public List<ResultThread> checkAllFormulas(List<String> formulas) {
        long medium_time = 0, medium_time_false = 0, medium_time_true = 0;
        int trues = 0, trues_hashtab = 0;
        Instant absolute_start = Instant.now();
        int i = 0;
        boolean checkF = false;
        List<String> true_formulas = new ArrayList<>();
		List<ResultThread> results = new ArrayList<>();
        
    	for(String s : formulas){
            Instant start = Instant.now();
            
            // finds in the true formula list if there is a string totally contained in inst
            Optional<String> isAny = true_formulas
                    .stream()
                    .filter((t) -> {
                    		return isSubsequence(t, s);
                        })
                    .findFirst();

            if(isAny.isPresent()){
                if(Utils.DEBUG)
                    System.out.println("One can avoid checking " + s + " as it is implied by a more general formula / " + isAny.orElse(""));
                ResultThread res = new ResultThread(true, s);
                res.setDerivedFormula(isAny.orElse(""));
                results.add(res);
                trues_hashtab++;
            }else{  
                // we need to check if with all possible sets of substances satisfying actual boolean formula
                // we can obtain product after N + 1 steps
                checkF = this.over.checkFormula(s);
                
                if(checkF){
                    true_formulas.add(s);
                    results.add(new ResultThread(true, s));
                    /*JOptionPane.showMessageDialog(this, "Instance " + inst + " lead to product " + this.over.getProduct() 
                            + " in " + this.over.getSteps() + " steps.",
                            "Success!", JOptionPane.INFORMATION_MESSAGE);*/
                }else{
                    results.add(new ResultThread(false, s));
                    /*JOptionPane.showMessageDialog(this, "Instance " + inst + " doesn't lead to product " + this.over.getProduct() 
                            + " in " + this.over.getSteps() + " steps.",
                            "Error!", JOptionPane.INFORMATION_MESSAGE);*/
                }
            }
            
            Instant finish = Instant.now();
            long timeElapsed = Duration.between(start, finish).toMillis();  //in nanos
            medium_time += timeElapsed;
            
            if(checkF){
                medium_time_true += timeElapsed;
                trues++;
            }else
                medium_time_false += timeElapsed;
            
            //this.model_table_over.addRow(o);
            i++;
        }
    	Instant absolute_finish = Instant.now();
        long absolute_elapsed_time = Duration.between(absolute_start, absolute_finish).toMillis();
        
    	if(Utils.TIME){
            System.out.println("(Medium) elapsed time for checking one formula = " + (float)(medium_time / formulas.size()));
            
            try{
                System.out.println("(Medium) elapsed time for checking one formula (true) = " + (float)(medium_time_true / trues));
            }catch(ArithmeticException ex){
                System.out.println("(Medium) elapsed time for checking one formula (true) = undef (no true formulas)");
            }
            System.out.println("Tot true formulas = " + trues);
            System.out.println("Formulas found to be true through hashing = " + trues_hashtab);
            
            try{
                System.out.println("(Medium) elapsed time for checking one formula (false) = " + (float)(medium_time_false / (formulas.size() - trues)));
            }catch(ArithmeticException ex){
                System.out.println("(Medium) elapsed time for checking one formula (false) = undef (no false formulas)");
            }
            System.out.println("Total elapsed time = " + absolute_elapsed_time);
            System.out.println("Tot false formulas = " + (formulas.size() - trues - trues_hashtab));
            System.out.println("TOT formulas = " + formulas.size());
        }
    	
    	return results;
    }


	@Override
	public Set<Set<String>> resultsFromReactsEnabledByT(Set<String> S) {
		return this.reaction_system_woI.stream()
				.filter((t) -> t.isEnabledBy(S))
				.map(t -> t.products())
				.collect(Collectors.toSet());
	}

	private void generateRulesWoI(String[] R, String[] I, String[] P){
        List<Reaction> rs_woi = new ArrayList();
        
        for(String p : P)
        	rs_woi.addAll(generateRulesWoI_Product(R, I, p));
        
        HashMap<Set<String>, Set<String>> mp = new HashMap<Set<String>, Set<String>>();
        
        for(Reaction r : reaction_system_woI) {
        	try{
                Set<String> common_products = mp.get(r.reactants());
                common_products.addAll(r.products());
                mp.put(r.reactants(), common_products);
            }catch (NullPointerException e) {
                mp.put(r.reactants(), r.products());
            }
        }
        
        for(Reaction r : rs_woi){
            try{
                Set<String> common_products = mp.get(r.reactants());
                common_products.addAll(r.products());
                mp.put(r.reactants(), common_products);
            }catch (NullPointerException e) {
                mp.put(r.reactants(), r.products());
            }
        }
        
        reaction_system_woI.clear();
        
        for (Map.Entry<Set<String>, Set<String>> entry : mp.entrySet()) {
        	reaction_system_woI.add(new Reaction(entry.getKey(), entry.getValue()));
        }
        
    }

	private List<Reaction> generateNegativeRulesWoI_Product(List<Set<String>> positive_rules_per_product, String p){
        Set<Set<String>> negative_rules = Sets.cartesianProduct(positive_rules_per_product)
                .stream()
                .map((t) -> {
                	return t.stream()
                            .map((s) -> {
                            	if(s.isEmpty())
                            		return s;
                            	
                                StringBuilder sb = new StringBuilder(s);
                                /*for (int index = 0; index < sb.length(); index++) {
                                    char c = sb.charAt(index);
                                    if (Character.isLowerCase(c)) {
                                        sb.setCharAt(index, Character.toUpperCase(c));
                                    } else {
                                        sb.setCharAt(index, Character.toLowerCase(c));
                                    }
                                }*/
                                
                                if(sb.charAt(0) == '[') {
                                	return sb.deleteCharAt(0).deleteCharAt(sb.length() - 1).toString();
                                }else return "[" + sb.toString() + "]";
                            })
                            .collect(Collectors.toSet());
                })
                // filter away all contradictory rules (e.g. a A -> h)
                .filter((t) -> {
                	
                	String conc_str =  t
                            .stream()
                            .reduce((s, u) -> {
                                return s + " " + u;
                            })
                            .orElse("");
                	
                    
                    for(String letter : t){
                     	letter = letter.replaceAll("\\[(.*?)\\]", "$1");
                        String pattern = ".*" + letter + ".*" + letter + ".*";
                        
                        if(conc_str.matches(pattern))
                            return false;
                    }
                	
                    
                    return true;
                    
                })
                .collect(Collectors.toSet());
        
        System.out.println("Rules to be examinated: " + negative_rules.size());
        
        // filter away all the dominated rules (i.e. all that rules that contain
        // all the elements of another rule)
        Set<Set<String>> dominated = new LinkedHashSet<>();
        for(Set<String> sts : negative_rules){
            
            dominated.addAll(negative_rules
                    .stream()
                    .filter((t) -> {
                        return t.containsAll(sts); 
                    })
                    .filter((t) -> {
                        return t.size() != sts.size(); 
                    })
                    .toList());
        }
        
        negative_rules.removeAll(dominated);
        
        // turns sets into reactions that produce p (lowercase)
        return negative_rules.stream()
                .map((t) -> {
                    return new Reaction(t, p);
                })
                .collect(Collectors.toList());
    }
    
    private List<Reaction> generateRulesWoI_Product(String[] R, String[] I, String p){
        List<Set<String>> positive_rules = this.reaction_system_woI.stream()
        		.filter(t -> t.isProduct(p))
        		.map(t -> t.reactants())
        		.collect(Collectors.toList());
        
        String p_lw = "["+p+"]";
        List<Reaction> all_rct_with_p = this.reaction_system_woI.stream()
    			.filter(t -> {
    					return t.products().toString().contains(p_lw) || t.products().toString().contains(p);
    			})
    			.toList();
        
        for(Reaction r : all_rct_with_p) {
    		Set<String> other_pds = r.products();
    		other_pds.remove(p);
    		other_pds.remove(p_lw);
    		
    		if(!other_pds.isEmpty())
    			this.reaction_system_woI.add(new Reaction(r.reactants(), other_pds));
    			
    		this.reaction_system_woI.remove(r);
    	}
        
        Set<String> R_and_I = Reaction.generateNoIReaction(Set.of(R), Set.of(I));
        R_and_I.removeAll(Set.of(""));
        positive_rules.add(R_and_I);
 
        List<Reaction> negative_rules_react = generateNegativeRulesWoI_Product(positive_rules, "["+p+"]");
        
        // turns sets into reactions that produce p 
        List<Reaction> positive_rules_react = positive_rules
                .stream()
                .map((t) -> {
                    return new Reaction(t, p);
                })
                .collect(Collectors.toList());
        
        
        positive_rules_react.addAll(negative_rules_react);
        
        return positive_rules_react;
    }

	@Override
	public Set<String> allInhibitorsOfAProduct(String p) {
		return Sets.newLinkedHashSet();
	}
	
	@Override
	public void printAllCycles() {
		if(Utils.ATTRACTOR_DETECTOR && Utils.EFFICIENT_IMPLEMENTATION) {
			((RSComputationalBitSet) this.over).printAllCycles();
		}
	}

	@Override
	public
	List<Reaction> getListofReactions() {
		return pos_react;
	}

	@Override
	public List<ResultThread> checkAllFormulasMultithread(List<String> formulas) {
		if(Utils.MULTITHREAD)
			return ((RSComputationalBitSet) this.over).simulateEvolutionofReactionSystemForAllFormulasMultithread(formulas);
		else throw new UnsupportedOperationException("The multithread flag must be active for no-inhibitors systems");
	}

}
