package predictor_fbp_refactor;


import com.google.common.collect.Sets;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

/**
 *
 * @author valer
 */
public class Maybe {
    private final LinkedHashSet<String> maybe;
    private final LinkedHashSet<String> maybeNot;

    public Maybe(LinkedHashSet<String> maybe, LinkedHashSet<String> maybe_not) {
        this.maybe = maybe.stream().sorted().collect(Collectors.toCollection(LinkedHashSet<String>::new));
        this.maybeNot = maybe_not.stream().sorted().collect(Collectors.toCollection(LinkedHashSet<String>::new));
    }
    
    public Maybe(){
        this.maybe = Sets.newLinkedHashSet();
        this.maybeNot = Sets.newLinkedHashSet();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("Maybe(");
        
        if(!maybe.isEmpty() || !maybeNot.isEmpty()){          
            for(String s : maybe){
                str.append(s).append(", ");
            }          

            for(String s : maybeNot){
                str.append("¬ ")
                        .append(s)
                        .append(", ");
            }

            str.delete(str.length() - 2, str.length());
            str.append(") ");
        }else{
            str.append(") ");
        }
        
        return str.toString();
    }
    
    
    
    public LinkedHashSet<String> getMaybeNot() {
        return maybeNot;
    }
    
    public LinkedHashSet<String> getMaybe() {
        return maybe;
    }

    public boolean isEmpty() {
        return maybe.isEmpty() && maybeNot.isEmpty();
    }

	public void excludeDummyNode() {
		
		if(!Utils.NO_INHIBITORS) {
			maybe.remove("create");
			maybeNot.remove("create");
		}else {
			maybe.remove("create");
			maybe.remove("[create]");
		}
	}

}

