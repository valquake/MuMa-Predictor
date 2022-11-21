package predictor_fbp_refactor;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author valer
 */
public class Reaction {

	private final LinkedHashSet<String> R;
    private final LinkedHashSet<String> P;
    private final LinkedHashSet<String> I;
    
    public Reaction(String[] R, String[] P, String[] I) {
        this.R = Sets.newLinkedHashSet(Arrays.asList(R))
                .stream()
                .filter(s -> !s.isEmpty())
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet<String>::new));
        this.P = Sets.newLinkedHashSet(Arrays.asList(P))
                .stream()
                .filter(s -> !s.isEmpty())
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet<String>::new));
        this.I = Sets.newLinkedHashSet(Arrays.asList(I))
                .stream()
                .filter(s -> !s.isEmpty())
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet<String>::new));
    }
    
    public Reaction(Set<String> R, String P) {
        this.R = Sets.newLinkedHashSet(R);
        this.P = new LinkedHashSet<>();
        this.P.add(P);
        this.I = Sets.newLinkedHashSet();
    }
    
    public Reaction(Set<String> R, Set<String> P) {
        this.R = Sets.newLinkedHashSet(R);
        this.P = Sets.newLinkedHashSet(P);
        this.I = Sets.newLinkedHashSet();
    }
    
    public int total_size(){
        return R.size() + I.size();
    }
    
    public Set<Set<String>> applicability(){
        return Set.of(R, I);
    }
    
    public boolean isProduct(String p){
        return this.P.contains(p);
    } 

    public LinkedHashSet<String> allInstances(){
        LinkedHashSet<String> s = Sets.newLinkedHashSet(R);
        s.addAll(I);
        s.addAll(R);
        
        return s;
    }
    
    public Set<String> products(){
        return this.P;
    }
    
    public Set<String> reactants(){
        return this.R;
    }
    
    public Set<String> inhibitors(){
        return this.I;
    }

    public boolean isEnabledBy(Set<String> actual) {
        return actual.containsAll(R) && Sets.intersection(actual, I).isEmpty();
    }
    
    public static LinkedHashSet<String> generateNoIReaction(Set<String> R, Set<String> I){
    	Set<String> in = I.stream()
            .map((t) -> {
                return t.isEmpty() ? t.toLowerCase() : "["+t+"]";
            })
            .collect(Collectors.toCollection(LinkedHashSet<String>::new));
        
        if(!in.isEmpty())
        	return Sets.union(R, in).stream()
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet<String>::new));
                // .toArray(String[]::new);
        else return Sets.newLinkedHashSet(R);
    }
    
    @Override
	public String toString() {
		return "R: " + this.R.toString() + "| I: " + this.I.toString() + "| P: " + this.P.toString() + " || ";
	}
}
