package predictor_fbp_refactor;


import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author valer
 */
public class Must {

    public Must(LinkedHashSet<String> must, LinkedHashSet<String> must_not) {
        this.must = must.stream().filter((t) -> {
            return !t.isEmpty(); 
        }).sorted().collect(Collectors.toCollection(LinkedHashSet<String>::new));
        this.must_not = must_not.stream().filter((t) -> {
            return !t.isEmpty();
        }).sorted().collect(Collectors.toCollection(LinkedHashSet<String>::new));
        this.size = must.size() + must_not.size();
    }
    

    public LinkedHashSet<String> getMust() {
        return must;
    }

    public int getSize() {
        return size;
    }

    public LinkedHashSet<String> getMust_not() {
        return must_not;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("Must(");
        
        if(!must.isEmpty() || !must_not.isEmpty()){
            List<String> sorted_must = must.stream()
                .sorted()
                .collect(Collectors.toList());
            
            for(String s : sorted_must){
                str.append(s).append(", ");
            }
            
            List<String> sorted_must_not = must_not.stream()
                .sorted()
                .collect(Collectors.toList());

            for(String s : sorted_must_not){
                str.append("¬ ")
                        .append(s)
                        .append(", ");
            }

            str.delete(str.length() - 2, str.length());
        }
        str.append(")");
        
        
        return str.toString();
    }
    
    
    
    private final LinkedHashSet<String> must;
    private final LinkedHashSet<String> must_not;
    private final int size;
	public void excludeDummyNode() {
		if(!Utils.NO_INHIBITORS) {
			must.remove("create");
			must_not.remove("create");
		}else {
			must.remove("create");
			must.remove("[create]");
		}
	}
}
