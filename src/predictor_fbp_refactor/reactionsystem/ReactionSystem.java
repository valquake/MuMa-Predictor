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
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import predictor_fbp_refactor.Maybe;
import predictor_fbp_refactor.Must;
import predictor_fbp_refactor.Pair;
import predictor_fbp_refactor.Reaction;
import predictor_fbp_refactor.Utils;
import predictor_fbp_refactor.exceptions.MustContradictionException;
import predictor_fbp_refactor.exceptions.NoReactionsFoundException;
import predictor_fbp_refactor.jxtreetable.OneNode;
import predictor_fbp_refactor.multithread.ResultThread;
import predictor_fbp_refactor.rscomputational.RSComputationalBitSet;
import predictor_fbp_refactor.rscomputational.RSComputationalBitSetI;
import predictor_fbp_refactor.rscomputational.RSComputationalSetI;

public class ReactionSystem extends RS{
    protected List<Reaction> reaction_system;
    
    // the Rs common to all the reactions whose p (the key) is product
    protected final HashMap<String, Set<String>> must_product_R;
    
    // the Is common to all the reactions whose p (the key) is product
    protected final HashMap<String, Set<String>> must_product_I;
    
    public ReactionSystem(String[] R, String[] I, String[] P) {
        Reaction r = new Reaction(R, P, I);
        this.reaction_system = new ArrayList<>();     
        this.must_product_R = new HashMap<>();
        this.must_product_I = new HashMap<>();

        for(String p: P){
            must_product_R.put(p, Set.of(R));
            must_product_I.put(p, Set.of(I));
        }
        
        this.reaction_system.add(r);
    }
    
    @Override
    public void addReaction(String[] R, String[] I, String[] P){ 
		// check if this reaction has create as product (in this case create
		// is considered as a virtual entity, it will be omitted from
		// the computation
		if(!Utils.DUMMY_NODE)
			if(Arrays.asList(P).contains("create"))
				Utils.DUMMY_NODE = true;
    	
        for(String p: P){         
            try{
                Set<String> all_r = this.must_product_R.get(p);
                
                // Such a case may originate from a earlier deletion that caused 
                // the removal of all reactions that have p as a product.
                if(all_r.isEmpty()){
                    boolean firstReaction = this.reaction_system
                        .stream()
                        .filter((t) -> {
                            return t.isProduct(p);
                        })
                        .findAny()
                        .isEmpty();
                    
                    if(firstReaction)
                        this.must_product_R.put(p, Set.of(R));
                }
                else{
                    Set<String> int_r = Sets.intersection(all_r, Set.of(R));                
                    this.must_product_R.put(p, int_r);
                }
            }catch(NullPointerException e){
                // this exception comes from get, no instance of p in the hashtable
                // We have to put into must
                must_product_R.put(p, Set.of(R));
            }
            
            try {
                Set<String> all_i = this.must_product_I.get(p);
                
                // Such a case may originate from a earlier deletion that caused 
                // the removal of all reactions that have p as a product.
                if(all_i.isEmpty()){
                    boolean firstReaction = this.reaction_system
                        .stream()
                        .filter((t) -> {
                            return t.isProduct(p);
                        })
                        .findAny()
                        .isEmpty();
                    
                    if(firstReaction)
                        this.must_product_I.put(p, Set.of(I));
                }
                else{
                    Set<String> int_i = Sets.intersection(all_i, Set.of(I));
                    this.must_product_I.put(p, int_i);
                }
            } catch (NullPointerException e) {
                // this exception comes from get, no instance of p in the hashtable
                // We have to put into must
                must_product_I.put(p, Set.of(I));
            }
        }
        
        this.reaction_system.add(new Reaction(R, P, I));
        
        
        if(Utils.DEBUG){
            System.out.println("\nR After Insert: ");
            for (Map.Entry<String, Set<String>> entry : must_product_R.entrySet()) {
                String key = entry.getKey();
                Set<String> value = entry.getValue();
                System.out.print(key + " " + value + " | ");
            }

            System.out.println("\nI After Insert: ");
            for (Map.Entry<String, Set<String>> entry : must_product_I.entrySet()) {
                String key = entry.getKey();
                Set<String> value = entry.getValue();
                System.out.print(key + " " + value + " | ");
            }
        }
        
    }

    @Override
    public void deleteReaction(int index){
        Reaction r = this.reaction_system.get(index);
        this.reaction_system.remove(index);        
        
        Set<String> p = r.products();
        Set<String> all = allInstancesOfRS();
        
        // By deleting this reaction, some instances no longer exist, 
        // we need to remove the corresponding entries from the hash table
        Set<String> nomore = Sets.union(Sets.difference(p, all), Sets.difference(r.allInstances(), all));
        
        for(String el: p){
            int c = 0;
            Set<String> remaining_must_r = Sets.newLinkedHashSet();
            Set<String> remaining_must_i = Sets.newLinkedHashSet();
            
            for(Reaction react: this.reaction_system){
                if(react.isProduct(el)){
                    if(c == 0){
                        remaining_must_r = react.reactants();
                        remaining_must_i = react.inhibitors();
                        c++;
                    }
                    else{
                        remaining_must_r = Sets.intersection(remaining_must_r, react.reactants());
                        remaining_must_i = Sets.intersection(remaining_must_i, react.inhibitors());
                    }                    
                }
            }
            
            this.must_product_R.put(el, remaining_must_r);          
            this.must_product_I.put(el, remaining_must_i);
        }
        
        for(String el : nomore){
            this.must_product_I.remove(el);
            this.must_product_R.remove(el);
        }
        
        if(Utils.DEBUG){
            System.out.println("\nR After Delete: ");
            for (Map.Entry<String, Set<String>> entry : must_product_R.entrySet()) {
                String key = entry.getKey();
                Set<String> value = entry.getValue();
                System.out.print(key + " " + value + " | ");
            }

            System.out.println("\nI After Delete: ");
            for (Map.Entry<String, Set<String>> entry : must_product_I.entrySet()) {
                String key = entry.getKey();
                Set<String> value = entry.getValue();
                System.out.print(key + " " + value + " | ");
            }
        }
        
    }
    
    @Override
    public Map.Entry<List<Set<String>>, List<Set<String>>> cause(String p){
        List<Set<String>> cause_pos = new ArrayList();
        List<Set<String>> cause_neg = new ArrayList();

        for(Reaction r: this.reaction_system){
            if(r.isProduct(p)){
                List<Set<String>> ap = new ArrayList(r.applicability());
                cause_pos.add(ap.get(1));
                cause_neg.add(ap.get(0));
            }
        }  

        return Pair.of(cause_pos, cause_neg);
    }
    
    @Override
    public Set<String> allProducts() {
        Set<String> pds = new HashSet<>();
        
        for(Reaction r: this.reaction_system){
            pds = Sets.union(pds, r.products());
        }
        
        
        return pds;
    }
    
    @Override
    public Set<String> allReactantsOfAProduct(String p){        
        Set<String> Rs = Set.of(); 
        for(Reaction r: this.reaction_system){
            if(r.isProduct(p))
                Rs = Sets.union(Rs, r.reactants());
        }
        
        return Rs;
    }
    
    @Override
    public Set<String> allInhibitorsOfAProduct(String p){        
        Set<String> Is = Set.of(); 
        for(Reaction r: this.reaction_system){
            if(r.isProduct(p))
                Is = Sets.union(Is, r.inhibitors());
        }
        
        return Is;
    }
    
    @Override
    public Set<String> allInstancesOfRS(){
        Set<String> instances = Set.of();
        for(Reaction r: this.reaction_system){
            instances = Sets.union(instances, Sets.union(r.products(), Sets.union(r.inhibitors(), r.reactants())));
        }
        
        return instances;
    }
    
    @Override
    public Set<String> getMustsR(String p){
        return must_product_R.get(p);
    }
    
    @Override
    public Set<String> getMustsI(String p){
        return must_product_I.get(p);
    }

    @Override
    public Map.Entry<Must, Maybe> computeOverApproximationEfficiently(Set<String> ps, int steps) throws MustContradictionException, NoReactionsFoundException{
		if(Utils.EFFICIENT_IMPLEMENTATION)
			this.over = new RSComputationalBitSetI(this.reaction_system, Sets.newLinkedHashSet(allInstancesOfRS()));
		else
			this.over = new RSComputationalSetI(this);
    	
    	
    	return this.over.computeApproximatePredictor(ps, steps);
    }
    
    @Override
    public void printAllCycles() {
		if(Utils.ATTRACTOR_DETECTOR && Utils.EFFICIENT_IMPLEMENTATION) {
			((RSComputationalBitSet) this.over).printAllCycles();
		}
	}
    
    @Override
    public boolean isSubsequence(String t, String s) {
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
                    .filter((t) -> isSubsequence(t, s))
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
	public Set<Set<String>> resultsFromReactsEnabledByT(Set<String> T) {
		return this.reaction_system.stream()
				.filter((t) -> t.isEnabledBy(T))
				.map(t -> t.products())
				.collect(Collectors.toSet());
	}

	@Override
	public List<Reaction> getListofReactions() {
		return reaction_system;
	}
	
	@Override
	public List<ResultThread> checkAllFormulasMultithread(List<String> formulas) {
		if(Utils.MULTITHREAD)
			return ((RSComputationalBitSet) this.over).simulateEvolutionofReactionSystemForAllFormulasMultithread(formulas);
		else throw new UnsupportedOperationException("The multithread flag must be active for no-inhibitors systems");
	}
}
