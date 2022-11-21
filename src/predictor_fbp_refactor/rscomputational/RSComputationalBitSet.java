package predictor_fbp_refactor.rscomputational;

import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.googlecode.javaewah.EWAHCompressedBitmap;
import com.googlecode.javaewah.IntIterator;

import predictor_fbp_refactor.AttractorDetector;
import predictor_fbp_refactor.Maybe;
import predictor_fbp_refactor.Must;
import predictor_fbp_refactor.Pair;
import predictor_fbp_refactor.Reaction;
import predictor_fbp_refactor.Utils;
import predictor_fbp_refactor.exceptions.MustContradictionException;
import predictor_fbp_refactor.exceptions.NoReactionsFoundException;
import predictor_fbp_refactor.multithread.AttractorDetectorMultithread;
import predictor_fbp_refactor.multithread.ResultThread;

import static java.util.stream.Collectors.toMap;

import java.time.Duration;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
*
* @author valer
* 
* This class takes inspiration from observations done by the authors of the paper
* 
* """Ferretti, Claudio & Leporati, Alberto & Manzoni, Luca & Porreca, Antonio. (2019). 
* The Many Roads to the Simulation of Reaction Systems. Fundamenta Informaticae. 171. 175-188. 10.3233/FI-2020-1878. """
* 
* Thank you for your contribution.
* 
*/
public abstract class RSComputationalBitSet extends RSComputationalStrategy {
	protected List<EWAHCompressedBitmap> reactants;
    protected List<EWAHCompressedBitmap> inhibitors;
    protected List<EWAHCompressedBitmap> products;
    protected Map<String, Integer> map_instances;
    private Map<Integer, Map.Entry<EWAHCompressedBitmap, EWAHCompressedBitmap>> must;
    private Map<Integer, Map.Entry<EWAHCompressedBitmap, EWAHCompressedBitmap>> maybe;
    private AttractorDetector attractors;
    
    // an helpful data structure to support efficiently simulation of the system
    protected MutableGraph<Integer> reaction_graph;
    
    public RSComputationalBitSet(List<Reaction> rs, LinkedHashSet<String> all_instances){
        this.reactants = new ArrayList<>();
        this.inhibitors = new ArrayList<>();
        this.products = new ArrayList<>();
        String not = Utils.NO_INHIBITORS ? "[" : "¬";
        
        List<String> all_instances_sorted = all_instances
                        .stream()
                        .sorted((o1, o2) -> {
                            /*if(Character.isLowerCase(o1.charAt(0)) && Character.isLowerCase(o2.charAt(0))){
                                return o1.substring(0).compareTo(o2.substring(0));
                            }
                            if(Character.isLowerCase(o1.charAt(0)) && Character.isUpperCase(o2.charAt(0))){
                                return 1; // o1 is greater than o2
                            }
                            if(Character.isUpperCase(o1.charAt(0)) && Character.isLowerCase(o2.charAt(0))){
                                return -1; // o1 is less than o2
                            }

                            return o1.compareTo(o2);*/
                        	if(o1.startsWith(not) && o2.startsWith(not)){
                                return o1.substring(1).compareTo(o2.substring(1));
                            }
                            if(o1.startsWith(not) && !o2.startsWith(not)){
                                return 1; // o1 is greater than o2
                            }
                            if(!o1.startsWith(not) && o2.startsWith(not)){
                                return -1; // o1 is less than o2
                            }

                            return o1.compareTo(o2);
                        })
                        .collect(Collectors.toList()); 
        
        this.map_instances = IntStream.range(0, all_instances_sorted.size())
                        .boxed()
                        .collect(toMap(all_instances_sorted::get, Function.identity()));

        this.reaction_graph = GraphBuilder
                    .directed()
                    .allowsSelfLoops(true)
                    .expectedNodeCount(rs.size())
                    .build();
        
        for(Reaction r : rs){
        	EWAHCompressedBitmap bs_r = new EWAHCompressedBitmap(all_instances.size());
        	EWAHCompressedBitmap bs_i = new EWAHCompressedBitmap(all_instances.size());
        	EWAHCompressedBitmap bs_p = new EWAHCompressedBitmap(all_instances.size());
            
            Set<String> string_reactants = r.reactants();
            Set<String> string_inhibitors = r.inhibitors();
            Set<String> string_products = r.products();
            
            for(String s : string_reactants)
                bs_r.set(map_instances.get(s));
            
            for(String s : string_inhibitors)
                bs_i.set(map_instances.get(s));
            
            for(String s : string_products)
                bs_p.set(map_instances.get(s));
            
            this.reactants.add(bs_r);
            this.inhibitors.add(bs_i);
            this.products.add(bs_p);
        }
        
        for(Reaction r : rs){
            int index_r = rs.indexOf(r);
            EWAHCompressedBitmap bs_p = this.products.get(index_r);
            
            
            List<Integer> intersect_s = IntStream.range(0, this.reactants.size())
                                    // A (directed) edge from v to u exists if reactants of v
                                    // will have something in common with products of u
                                    .filter((i) -> {
                                    	EWAHCompressedBitmap tmp = this.reactants.get(i);
                                        return tmp.intersects(bs_p);
                                    })
                                    .boxed()
                                    .collect(Collectors.toList());
            

            if(intersect_s.isEmpty())
            	reaction_graph.addNode(index_r);
            
            for(Integer e : intersect_s)
                reaction_graph.putEdge(index_r, e);
        }

        if(Utils.DEBUG){
            System.out.println(this.map_instances);
            System.out.println(this.reactants);
            System.out.println(this.inhibitors);
            System.out.println(this.products);
            System.out.println(this.reaction_graph.toString());
        }
        
        if(Utils.ATTRACTOR_DETECTOR)
        	attractors = new AttractorDetector();
        
        buildMustMaybeBitSet();
    }
    
    private void buildMustMaybeBitSet(){
        this.must = new HashMap<>();
        this.maybe = new HashMap<>();
        Collection<Integer> instances = this.map_instances.values();
        
        for(Integer ins : instances){  
        	// take all reactions in which ins is product in form of a list of pairs <R,I>, with R reactants, I inhibitors
            List<Map.Entry<EWAHCompressedBitmap, EWAHCompressedBitmap>> all_infos = IntStream.range(0, this.reactants.size())
                            .filter((i) -> (this.products.get(i).get(ins)))
                            .boxed()
                            .map((i) -> {
                                return new AbstractMap.SimpleEntry<>(reactants.get(i), inhibitors.get(i));
                            })
                            .collect(Collectors.toList());
           if(all_infos.size() > 0) {
        	   EWAHCompressedBitmap must_ins, maybe_ins, maybe_not_ins, must_not_ins;
        	   // must and must not are initially set to an all-ones bitmap
        	   must_ins = new EWAHCompressedBitmap(this.map_instances.size());
        	   for(Integer i : instances)
        		   must_ins.set(i);
        	   must_not_ins = new EWAHCompressedBitmap(this.map_instances.size());
        	   for(Integer i : instances)
        		   must_not_ins.set(i);
        	   // while maybe and maybe not are initially set to an all-zeros bitmap
        	   maybe_ins = new EWAHCompressedBitmap(this.map_instances.size());
        	   maybe_not_ins = new EWAHCompressedBitmap(this.map_instances.size());
        	   
        	   // prepare the must/maybe entry for ins
        	   Entry<EWAHCompressedBitmap, EWAHCompressedBitmap> el;
        	   
        	   for(int i = 0; i < all_infos.size(); i++) {
        		   	// accumulate values in the bitmask 
					el = all_infos.get(i);
					// for must, put in and reactants, for must not, put in and inhibitors
					must_ins = must_ins.and(el.getKey());
				    must_not_ins = must_not_ins.and(el.getValue());
				    // for maybe, put in or reactants, for maybe not, put in or inhibitors
				    maybe_ins = maybe_ins.or(el.getKey());
				    maybe_not_ins = maybe_not_ins.or(el.getValue());
				}
	
				EWAHCompressedBitmap allOnes = new EWAHCompressedBitmap(this.map_instances.size());
				for(Integer i : instances)
					allOnes.set(i);
				// now, do the complement of must...
				EWAHCompressedBitmap complement_must = must_ins.xor(allOnes);
				EWAHCompressedBitmap complement_must_not = must_not_ins.xor(allOnes);
				// ... for excluding from maybe instances already inserted in must
				EWAHCompressedBitmap maybe_ins_and = maybe_ins.and(complement_must);
				EWAHCompressedBitmap maybe_not_ins_and = maybe_not_ins.and(complement_must_not);
				
				// must and maybe for cause(ins) are ready
				must.put(ins, Pair.of(must_ins, must_not_ins));
				maybe.put(ins, Pair.of(maybe_ins_and, maybe_not_ins_and));
				
				// compute it's time to compute nocause(ini)
				
				// if there's only one reaction, with only one reactant, then put must not this
				// lonely reactant               
	            if(must_ins.cardinality() == 1 && maybe_not_ins_and.cardinality() == 0 
	            		&& maybe_ins_and.cardinality() == 0 && must_not_ins.cardinality() == 0){
	            	must.put(ins + this.map_instances.size(), Pair.of(must_not_ins, must_ins));
	                maybe.put(ins + this.map_instances.size(), Pair.of(maybe_not_ins_and, maybe_ins_and));
	            }
	            // for all the other cases, it must be put all (both inhibitors and reactants) in maybe
	            // with maybe/maybe not exchanged
	            else{
	                must.put(ins + this.map_instances.size(), Pair.of(new EWAHCompressedBitmap(this.map_instances.size()), new EWAHCompressedBitmap(this.map_instances.size())));
	                maybe.put(ins + this.map_instances.size(), Pair.of(must_not_ins.or(maybe_not_ins), must_ins.or(maybe_ins)));
	            }
            }else {
            	// if no reactions have ins as product, put in the map a null value
            	must.put(ins, null);
				maybe.put(ins, null);
				must.put(ins + this.map_instances.size(), null);
                maybe.put(ins + this.map_instances.size(), null);
            }
           
        }
        
        if(Utils.DEBUG)
        	System.out.printf("Must : %s \nMaybe : %s\n", this.must.toString(), this.maybe.toString());
    }
    
    private Map.Entry<Must, Maybe> convertToMustMaybe(EWAHCompressedBitmap must_ins, EWAHCompressedBitmap maybe_ins, EWAHCompressedBitmap must_not_ins, EWAHCompressedBitmap maybe_not_ins){
        LinkedHashSet<String> f_must = Arrays.stream(must_ins.toArray())
                .mapToObj((i) -> {
                    return this.map_instances
                        .entrySet()
                        .stream()
                        .filter(entry -> Objects.equals(entry.getValue(), i))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .get();
                })
                .collect(Collectors.toCollection(LinkedHashSet<String>::new)); 
        LinkedHashSet<String> f_maybe = Arrays.stream(maybe_ins.toArray())
                .mapToObj((i) -> {
                    return this.map_instances
                        .entrySet()
                        .stream()
                        .filter(entry -> Objects.equals(entry.getValue(), i))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .get();
                })
                .collect(Collectors.toCollection(LinkedHashSet<String>::new)); 
        LinkedHashSet<String> f_maybe_not = Arrays.stream(maybe_not_ins.toArray())
                .mapToObj((i) -> {
                    return this.map_instances
                        .entrySet()
                        .stream()
                        .filter(entry -> Objects.equals(entry.getValue(), i))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .get();
                })
                .collect(Collectors.toCollection(LinkedHashSet<String>::new)); 
        LinkedHashSet<String> f_must_not = Arrays.stream(must_not_ins.toArray())
                .mapToObj((i) -> {
                    return this.map_instances
                        .entrySet()
                        .stream()
                        .filter(entry -> Objects.equals(entry.getValue(), i))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .get();
                })
                .collect(Collectors.toCollection(LinkedHashSet<String>::new)); 
    
        this.predictor_overapprox = Pair.of(new Must(f_must, f_must_not), new Maybe(f_maybe, f_maybe_not));
        return this.predictor_overapprox;
    }
	
	@Override
	public Entry<Must, Maybe> computeApproximatePredictor(Set<String> ps, int steps)
			throws MustContradictionException, NoReactionsFoundException {
		this.steps = steps;
		this.req_prod = ps;
		int tot_steps = steps + 1;
		steps++;
		EWAHCompressedBitmap bs_must = new EWAHCompressedBitmap(this.map_instances.size());
        
        for(String s : ps) {
        	Integer key = this.map_instances.get(s);
        	if(key == null)
        		throw new NoReactionsFoundException(s);
            bs_must.set(key);
        }
        
        EWAHCompressedBitmap allOnes = new EWAHCompressedBitmap(this.map_instances.size());
        for(Integer i : this.map_instances.values())
        	allOnes.set(i);
         
        EWAHCompressedBitmap bs_maybe = new EWAHCompressedBitmap(this.map_instances.size());
        EWAHCompressedBitmap bs_maybe_not = new EWAHCompressedBitmap(this.map_instances.size());
        EWAHCompressedBitmap bs_must_not = new EWAHCompressedBitmap(this.map_instances.size());
        
        EWAHCompressedBitmap bs_must_tmp = new EWAHCompressedBitmap(this.map_instances.size());
        EWAHCompressedBitmap bs_maybe_tmp = new EWAHCompressedBitmap(this.map_instances.size());
        EWAHCompressedBitmap bs_maybe_not_tmp = new EWAHCompressedBitmap(this.map_instances.size());
        EWAHCompressedBitmap bs_must_not_tmp = new EWAHCompressedBitmap(this.map_instances.size());
        List<EWAHCompressedBitmap> ll;
        
        if(Utils.DEBUG) {
        	System.out.printf("Mu%s Ma%s MuN%s MaN%s -> ", bs_must.toString(), bs_maybe_tmp.toString(),
        			bs_must_not_tmp.toString(), bs_maybe_tmp.toString());
        }
        
        Map.Entry<EWAHCompressedBitmap, EWAHCompressedBitmap> tmp;
        IntIterator it;
        int[] contradiction;
        while(steps > 1){
            bs_must_tmp.clear();
            bs_maybe_tmp.clear();
            bs_maybe_not_tmp.clear();
            bs_must_not_tmp.clear();
            
            
            it = bs_must.intIterator();
            
            while(it.hasNext()) {
            	int i = it.next();
            	tmp = this.must.get(i);
                if(tmp == null)
                    throw new NoReactionsFoundException(getKeyByValue(i));
                
                bs_must_tmp = bs_must_tmp.or(tmp.getKey());
                bs_must_not_tmp = bs_must_not_tmp.or(tmp.getValue());
                tmp = this.maybe.get(i);
                if(tmp == null)
                    throw new NoReactionsFoundException(getKeyByValue(i));
                
                bs_maybe_tmp = bs_maybe_tmp.or(tmp.getKey());
                bs_maybe_not_tmp = bs_maybe_not_tmp.or(tmp.getValue());
            }
            
            it = bs_must_not.intIterator();
            
            while(it.hasNext()) {
            	int i = it.next();
            	tmp = this.must.get(i + this.map_instances.size());
                if(tmp == null)
                    throw new NoReactionsFoundException(getKeyByValue(i));
                
                bs_must_tmp = bs_must_tmp.or(tmp.getKey());
                bs_must_not_tmp = bs_must_not_tmp.or(tmp.getValue());
                tmp = this.maybe.get(i + this.map_instances.size());
                if(tmp == null)
                    throw new NoReactionsFoundException(getKeyByValue(i));
                
                bs_maybe_tmp = bs_maybe_tmp.or(tmp.getKey());
                bs_maybe_not_tmp = bs_maybe_not_tmp.or(tmp.getValue());
            }
            
            it = bs_maybe.intIterator();
            
            while(it.hasNext()) {
            	int i = it.next();
            	tmp = this.must.get(i);
                if(tmp == null)
                    throw new NoReactionsFoundException(getKeyByValue(i));
                
                bs_maybe_tmp = bs_maybe_tmp.or(tmp.getKey());
                bs_maybe_not_tmp = bs_maybe_not_tmp.or(tmp.getValue());
                tmp = this.maybe.get(i);
                if(tmp == null)
                    throw new NoReactionsFoundException(getKeyByValue(i));
                
                bs_maybe_tmp = bs_maybe_tmp.or(tmp.getKey());
                bs_maybe_not_tmp = bs_maybe_not_tmp.or(tmp.getValue());
            }
            
            it = bs_maybe_not.intIterator();
            
            while(it.hasNext()) {
            	int i = it.next();
            	tmp = this.must.get(i + this.map_instances.size());
                if(tmp == null)
                    throw new NoReactionsFoundException(getKeyByValue(i));
                
                bs_maybe_tmp = bs_maybe_tmp.or(tmp.getKey());
                bs_maybe_not_tmp = bs_maybe_not_tmp.or(tmp.getValue());
                tmp = this.maybe.get(i + this.map_instances.size());
                if(tmp == null)
                    throw new NoReactionsFoundException(getKeyByValue(i));
                
                bs_maybe_tmp = bs_maybe_tmp.or(tmp.getKey());
                bs_maybe_not_tmp = bs_maybe_not_tmp.or(tmp.getValue());
            }
                   
            
            EWAHCompressedBitmap complement_must = bs_must_tmp.xor(allOnes);
            EWAHCompressedBitmap complement_must_not = bs_must_not_tmp.xor(allOnes);
            EWAHCompressedBitmap bs_maybe_tmp_and = bs_maybe_tmp.and(complement_must).and(complement_must_not);
            EWAHCompressedBitmap bs_maybe_tmp_not_and = bs_maybe_not_tmp.and(complement_must_not).and(complement_must);
            
            if((contradiction = findContradiction(bs_must_tmp, bs_must_not_tmp)) != null) {
            	String res = Arrays.stream(contradiction)
                .mapToObj((i) -> {
                    return this.map_instances
                        .entrySet()
                        .stream()
                        .filter(entry -> Objects.equals(entry.getValue(), i))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .get();
                })
                .toList()
                .toString();
            	
                throw new MustContradictionException(res + " at the step " + (tot_steps - steps + 1));
            }
            
            ll = excludeOppositeInstances(bs_must_tmp, bs_maybe_tmp_and, bs_must_not_tmp, bs_maybe_tmp_not_and);
            
            
            try {
				bs_must = ll.get(0).clone();
				bs_maybe = ll.get(1).clone();
	            bs_maybe_not = ll.get(3).clone();
	            bs_must_not = ll.get(2).clone();
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            if(Utils.DEBUG) {
            	System.out.printf("Mu%s Ma%s MuN%s MaN%s -> ", bs_must.toString(), bs_maybe.toString(),
            			bs_must_not.toString(), bs_maybe_not.toString());
            }
            
            steps--;
        }
        
        ll = excludeOppositeInstances(bs_must, bs_maybe, bs_must_not, bs_maybe_not);
        
        if(Utils.DUMMY_NODE) 
        	// we have to exclude from the computation all references to dummy node
        	ll = excludeDummyNodeBitSet(ll);       	
        
        if(Utils.DEBUG) {
        	System.out.printf("OVER COMPUTED Mu%s Ma%s MuN%s MaN%s\n", ll.get(0).toString(), ll.get(1).toString(),
        			ll.get(2).toString(), ll.get(3).toString());
        }
        
        return convertToMustMaybe(ll.get(0), ll.get(1), ll.get(2), ll.get(3));
	}

	protected abstract List<EWAHCompressedBitmap> excludeDummyNodeBitSet(List<EWAHCompressedBitmap> ll);

	private String getKeyByValue(int i) {
		for (Entry<String, Integer> entry : map_instances.entrySet()) {
	        if (Objects.equals(i, entry.getValue())) {
	            return entry.getKey();
	        }
	    }
	    return "undef";
	}

	protected abstract List<EWAHCompressedBitmap> excludeOppositeInstances(EWAHCompressedBitmap bs_must,
			EWAHCompressedBitmap bs_maybe, EWAHCompressedBitmap bs_must_not, EWAHCompressedBitmap bs_maybe_not);

	protected abstract int[] findContradiction(EWAHCompressedBitmap bs_must_tmp,
			EWAHCompressedBitmap bs_must_not_tmp);
	
	public void printAllCycles() {
		attractors.printAllCycles();
	}
	
	@Override
	public Set<String> simulateEvolutionofReactionSystem(Set<String> T) {
		int temp_steps = this.steps;
		temp_steps++;
		EWAHCompressedBitmap bs_t = new EWAHCompressedBitmap(this.map_instances.size());
        for(String s : T)
            bs_t.set(this.map_instances.get(s));
        
        // if we have create node, we have to remember to add it in the initial context
        // (otherwise some reactions may not be legally enabled)
        if(Utils.DUMMY_NODE)
        	bs_t.set(this.map_instances.get("create"));
        
        // case where there are only not in formula (generates {} as satisfying set)
        if(bs_t.cardinality() == 0)
        	return fromEWAHCompressedBitmap2SetOfStrings(bs_t); 
        
        if(Utils.ATTRACTOR_DETECTOR) {
        	EWAHCompressedBitmap res = attractors.addAndCheckForPossibleCycle(bs_t, temp_steps);
        	
        	if(res != null) {
        		System.out.printf("---->'1'");
                System.out.printf("%s\n", res.toString());
        		attractors.cleanPossibleCycle();
        		return fromEWAHCompressedBitmap2SetOfStrings(res);
        	}
        }
        
        if(Utils.DEBUG) {
        	System.out.printf("'%s'",temp_steps);
            System.out.printf("%s -> ", bs_t.toString());
        }
        
        Set<Integer> reaction_enabled = IntStream.range(0, this.reactants.size())
        							.filter(i -> filterEnabledRWithBitSet(i, bs_t))
                                    .boxed()
                                    .collect(Collectors.toSet());
        
        EWAHCompressedBitmap pds = new EWAHCompressedBitmap(this.reactants.size());

        for(Integer t : reaction_enabled)
            pds = pds.or(this.products.get(t));
        
        temp_steps--;
        EWAHCompressedBitmap tmp_pds = new EWAHCompressedBitmap(this.reactants.size());

        IntIterator it = pds.intIterator();
        
        while(it.hasNext())
        	tmp_pds.set(it.next());
        
        if(Utils.DEBUG) {
        	System.out.printf("'%s'",temp_steps);
            System.out.printf("%s -> ", pds.toString());
        }
        
        if(Utils.ATTRACTOR_DETECTOR) {
        	EWAHCompressedBitmap copy_pds = new EWAHCompressedBitmap(this.map_instances.size());
        	it = tmp_pds.intIterator();
            
            while(it.hasNext())
            	copy_pds.set(it.next());
            
        	EWAHCompressedBitmap res = attractors.addAndCheckForPossibleCycle(copy_pds, temp_steps);
        	
        	if(res != null) {
        		System.out.printf("---->'1'");
                System.out.printf("%s\n", res.toString());
        		attractors.cleanPossibleCycle();
        		return fromEWAHCompressedBitmap2SetOfStrings(res);
        	}
        }

        while(temp_steps > 1 && !reaction_enabled.isEmpty()){
            Set<Integer> reactions_to_see = new LinkedHashSet<>();
            
            for(Integer t : reaction_enabled)
                reactions_to_see.addAll(this.reaction_graph.successors(t));

                    
            reaction_enabled.clear();
            
            for(Integer i : reactions_to_see) 
	    		// when there are no actual instances in common with inhibitors and
	            // there are all the reagents, then tmp can be kept
	            if(filterEnabledRWithBitSet(i, tmp_pds))
	            	reaction_enabled.add(i);
            
            tmp_pds.clear();
            
            // compute all the products in this step
            for(Integer t : reaction_enabled)
                tmp_pds = tmp_pds.or(this.products.get(t));
            
            
            // if it is equals to the previous product result or it produces nothing,
            // leave the loop
            if(/*pds.equals(tmp_pds) ||*/ tmp_pds.cardinality() == 0) {
            	
            	if(Utils.DEBUG)
            		System.out.printf("Break");
            	
                break;
            }
            
            pds.clear();
            it = tmp_pds.intIterator();
            
            while(it.hasNext())
            	pds.set(it.next());
            
            temp_steps--;
            
            
            if(Utils.ATTRACTOR_DETECTOR) {
            	
            	EWAHCompressedBitmap copy_pds = new EWAHCompressedBitmap(this.map_instances.size());
            	it = pds.intIterator();
                
                while(it.hasNext())
                	copy_pds.set(it.next());
            	
            	EWAHCompressedBitmap res = attractors.addAndCheckForPossibleCycle(copy_pds, temp_steps);
            	
            	if(res != null) { 
            		System.out.printf("---->'1'");
                    System.out.printf("%s\n", res.toString());
            		attractors.cleanPossibleCycle();
            		return fromEWAHCompressedBitmap2SetOfStrings(res);
            	}
            }
            
            if(Utils.DEBUG) {
            	System.out.printf("'%s'",temp_steps);
            	if(temp_steps == 1)
            		System.out.printf("%s ", tmp_pds.toString());
            	else
            		System.out.printf("%s ->", tmp_pds.toString());
            }
        }
        
        if(Utils.DEBUG)
            System.out.printf("\n");
       
        if(Utils.ATTRACTOR_DETECTOR)
        	attractors.cleanPossibleCycle();
        
        return fromEWAHCompressedBitmap2SetOfStrings(tmp_pds);
	}
	
	protected Set<String> fromEWAHCompressedBitmap2SetOfStrings(EWAHCompressedBitmap tmp_pds){
		return Arrays.stream(tmp_pds.toArray())
                .mapToObj((i) -> {
                    return this.map_instances
                        .entrySet()
                        .stream()
                        .filter(entry -> Objects.equals(entry.getValue(), i))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .get();
                })
                .collect(Collectors.toSet()); 
	}
	
	protected abstract boolean filterEnabledRWithBitSet(int i, EWAHCompressedBitmap bs_t);

	public String computeUnderApproximation(Set<String> ps, int steps){
		EWAHCompressedBitmap bs_must = new EWAHCompressedBitmap(this.map_instances.size());
		
		Collection<Integer> instances = this.map_instances.values();
		this.steps = steps;
		this.req_prod = ps;
        
        for(String s : ps)
            bs_must.set(this.map_instances.get(s));
        
        List<Integer> reaction_enabled = IntStream.range(0, this.products.size())
                                    // all reactant present in T
                                    .filter((i) -> {
                                        return products.get(i).and(bs_must).cardinality() != 0;
                                    })
                                    .boxed()
                                    .collect(Collectors.toList());  
        
        while(steps > 1 && !reaction_enabled.isEmpty()){
            Set<Integer> reactions_to_see = new LinkedHashSet<>();
            
            for(Integer t : reaction_enabled)
                reactions_to_see.addAll(this.reaction_graph.predecessors(t));
            
            if(reactions_to_see.equals(reaction_enabled))
            	break;
            
            reaction_enabled.clear();
            reaction_enabled.addAll(reactions_to_see);
            
            steps--;
        }
        
        // Build a unique formula from reactants and inhibitors leaved from reaction enabled
        List<EWAHCompressedBitmap> all_infos = reaction_enabled.stream()
                            .map((i) -> products.get(i))
                            .collect(Collectors.toList());
        
        EWAHCompressedBitmap union_ins;
        union_ins = new EWAHCompressedBitmap(this.map_instances.size());
        
        for(int i = 0; i < all_infos.size(); i++) {
            union_ins = union_ins.or(all_infos.get(i));
        }
        
        /*// Gets the maybe/must from the enabled reactions
        List<Map.Entry<EWAHCompressedBitmap, EWAHCompressedBitmap>> all_infos = reaction_enabled.stream()
                            .map((i) -> new AbstractMap.SimpleEntry<>(reactants.get(i), inhibitors.get(i)))
                            .collect(Collectors.toList());
                
        EWAHCompressedBitmap must_ins, maybe_ins, maybe_not_ins, must_not_ins;
        must_ins = new EWAHCompressedBitmap(this.map_instances.size());
        for(Integer i : instances)
        	must_ins.set(i);
        must_not_ins = new EWAHCompressedBitmap(this.map_instances.size());
        for(Integer i : instances)
        	must_not_ins.set(i);
        maybe_ins = new EWAHCompressedBitmap(this.map_instances.size());
        maybe_not_ins = new EWAHCompressedBitmap(this.map_instances.size());
        
        for(int i = 0; i < all_infos.size(); i++) {
            must_ins = must_ins.and(all_infos.get(i).getKey());
            must_not_ins = must_not_ins.and(all_infos.get(i).getValue());
            maybe_ins = maybe_ins.or(all_infos.get(i).getKey());
            maybe_not_ins = maybe_not_ins.or(all_infos.get(i).getValue());
        }
        
        
        EWAHCompressedBitmap allOnes = new EWAHCompressedBitmap(this.map_instances.size());
        for(Integer i : instances)
        	allOnes.set(i);
        EWAHCompressedBitmap complement_must = must_ins.xor(allOnes);
        EWAHCompressedBitmap complement_must_not = must_not_ins.xor(allOnes);
        EWAHCompressedBitmap maybe_ins_and = maybe_ins.and(complement_must);
        EWAHCompressedBitmap maybe_not_ins_and = maybe_not_ins.and(complement_must_not);*/
   
        return Arrays.stream(union_ins.toArray())
                .mapToObj((i) -> {
                    return this.map_instances
                        .entrySet()
                        .stream()
                        .filter(entry -> Objects.equals(entry.getValue(), i))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .get();
                })
                .collect(Collectors.toCollection(LinkedHashSet<String>::new))
                .toString();
    }

	public LinkedHashSet<LinkedHashSet<LinkedHashSet<String>>> getAllCycles() {
		List<List<EWAHCompressedBitmap>> cycles = this.attractors.getAllCycles();
		
		int k = Utils.DUMMY_NODE ? this.map_instances.get("create") : -1;
		
		return cycles.stream()
				.map(t -> {
					return t.stream()
							.map(u -> {
								return Arrays.stream(u.toArray())
				                .mapToObj((i) -> {
				                    return this.map_instances
				                        .entrySet()
				                        .stream()
				                        .filter(entry -> Objects.equals(entry.getValue(), i))
				                        .map(Map.Entry::getKey)
				                        .findFirst()
				                        .get();
				                })
				                .filter(entry -> !entry.equals("create"))
				                .collect(Collectors.toCollection(LinkedHashSet<String>::new));
							})
							.filter(u -> !u.isEmpty())
							.collect(Collectors.toCollection(LinkedHashSet<LinkedHashSet<String>>::new));
				})
				.collect(Collectors.toCollection(LinkedHashSet<LinkedHashSet<LinkedHashSet<String>>>::new));
	}

	private static int NUM_THREAD = 8;
	private ExecutorService executor;
    private CopyOnWriteArrayList<String> true_formulas;
	private List<String> allFormulas;
	private List<ResultThread> results;
	private AtomicLong medium_time = new AtomicLong(0), medium_time_false = new AtomicLong(0), medium_time_true = new AtomicLong(0);
	private AtomicInteger trues = new AtomicInteger(0), trues_hashtab = new AtomicInteger(0);
	
	private class SimulationTask implements Callable<List<ResultThread>>{
		private List<String> listView;
		private List<ResultThread> partial_res;
		private AttractorDetectorMultithread att;
		
		public SimulationTask(List<String> listView, Map<EWAHCompressedBitmap, List<EWAHCompressedBitmap>> attractors, ReadWriteLock lock) {
			this.listView = listView;
			this.partial_res = new ArrayList<>();
			this.att = new AttractorDetectorMultithread(attractors, lock);
		}
		
		@Override
		public List<ResultThread> call() {
			for(String ff : listView) {
				String formula = new String(ff);
				
				Optional<String> isAny = true_formulas.stream()
						.filter(t -> findTrueFormulaSubsequence(t, formula))
						.findFirst();
				
				if(isAny.isPresent()) {
					if(Utils.DEBUG)
						synchronized(this) {
							System.out.println("One can avoid checking " + formula + " as it is implied by a more general formula / " + isAny.orElse(""));
						}
					ResultThread res = new ResultThread(true, formula);
					res.setDerivedFormula(isAny.orElse(""));
					partial_res.add(res);
					trues_hashtab.incrementAndGet();
				}else {
				
					// generate all the initial contests satisfying formula
					Set<Set<String>> lit_formula = generateAllPossibilities(formula);
					
					Set<String> res = null;
					Instant start = null, finish = null;
					start = Instant.now();
					boolean checkFalse = false;
					
					if(lit_formula.isEmpty()) {
						partial_res.add(new ResultThread(false, formula));
						checkFalse = true;
					}
					
					// simulate
					for(Set<String> lit_f : lit_formula) {
						if(Utils.DEBUG)
							synchronized (this) {
				        		System.out.printf("%s || ", formula);
							}
						res = doSimulation(lit_f);
					
						if(res == null || !res.containsAll(getProducts())) {
							finish = Instant.now();
				            long timeElapsed = Duration.between(start, finish).toMillis();  //in millis
				            medium_time.addAndGet(timeElapsed);
				            medium_time_false.addAndGet(timeElapsed);
				            if(Utils.DEBUG)
				            	synchronized(this) {
				            		System.out.println("Possibility " + lit_f + " for formula " + formula + " is a counterexample\nIt produces " + res.toString());
				            	}
			            	partial_res.add(new ResultThread(false, formula));
							checkFalse = true;
							break;
						}
					}
					
					if(Utils.DEBUG_MULTITHREAD)
						synchronized(this) {
							System.out.println(Thread.currentThread().getId() + ": steps done for "+formula);
						}
					
					if(!checkFalse) {
						finish = Instant.now();
			            long timeElapsed = Duration.between(start, finish).toMillis();  //in millis
						medium_time.addAndGet(timeElapsed);
						medium_time_true.addAndGet(timeElapsed);
						partial_res.add(new ResultThread(true, formula));
						true_formulas.add(formula);
						trues.incrementAndGet();
					}
				}
			}
			
			return partial_res;
		}
		
		
		private Set<String> doSimulation(Set<String> T){
			int temp_steps = steps;
			temp_steps++;
			EWAHCompressedBitmap bs_t = new EWAHCompressedBitmap(map_instances.size());
	        for(String s : T)
	            bs_t.set(map_instances.get(s));
	        
	        if(Utils.DUMMY_NODE)
	        	bs_t.set(map_instances.get("create"));
	        
	        // case corresponding to a formula composed by only nots (generates {})
	        if(bs_t.cardinality() == 0)
	        	return fromEWAHCompressedBitmap2SetOfStrings(bs_t);
	        
	        if(Utils.ATTRACTOR_DETECTOR) {
	        	EWAHCompressedBitmap res = att.addAndCheckForPossibleCycle(bs_t, temp_steps);
	        	
	        	if(res != null) {
	        		if(Utils.DEBUG)
		        		synchronized(this) {
			        		System.out.printf("---->'1'");
		                    System.out.printf("%s\n", res.toString());
		        		}
	        		att.cleanPossibleCycle();
	        		return fromEWAHCompressedBitmap2SetOfStrings(res);
	        	}
	        }
	        
	        if(Utils.DEBUG) {
	        	synchronized(this) {
		        	System.out.printf("'%s'",temp_steps);
		            System.out.printf("%s -> ", bs_t.toString());
	        	}
	        }
	        
	        Set<Integer> reaction_enabled = IntStream.range(0, reactants.size())
	        							.filter(i -> filterEnabledRWithBitSet(i, bs_t))
	                                    .boxed()
	                                    .collect(Collectors.toSet());
	        
	        EWAHCompressedBitmap pds = new EWAHCompressedBitmap(reactants.size());

	        for(Integer t : reaction_enabled)
	            pds = pds.or(products.get(t));
	        
	        temp_steps--;
	        EWAHCompressedBitmap tmp_pds = new EWAHCompressedBitmap(reactants.size());

	        IntIterator it = pds.intIterator();
	        
	        while(it.hasNext())
	        	tmp_pds.set(it.next());
	        
	        if(Utils.DEBUG) {
	        	synchronized(this) {
		        	System.out.printf("'%s'",temp_steps);
		            System.out.printf("%s -> ", pds.toString());
	        	}
	        }
	        
	        if(Utils.ATTRACTOR_DETECTOR) {
	        	EWAHCompressedBitmap copy_pds = new EWAHCompressedBitmap(map_instances.size());
	        	it = tmp_pds.intIterator();
	            
	            while(it.hasNext())
	            	copy_pds.set(it.next());
	            
	        	EWAHCompressedBitmap res = att.addAndCheckForPossibleCycle(copy_pds, temp_steps);
	        	
	        	if(res != null) {
	        		if(Utils.DEBUG)
		        		synchronized(this) {
			        		System.out.printf("---->'1'");
		                    System.out.printf("%s\n", res.toString());
		        		}
	        		att.cleanPossibleCycle();
	        		return fromEWAHCompressedBitmap2SetOfStrings(res);
	        	}
	        }

	        while(temp_steps > 1 && !reaction_enabled.isEmpty()){
	            Set<Integer> reactions_to_see = new LinkedHashSet<>();
	            
	            for(Integer t : reaction_enabled)
	                reactions_to_see.addAll(reaction_graph.successors(t));
	            
	            reaction_enabled.clear();
	            
	            for(Integer i : reactions_to_see) 
		    		// when there are no actual instances in common with inhibitors and
		            // there are all the reagents, then tmp can be kept
		            if(filterEnabledRWithBitSet(i, tmp_pds))
		            	reaction_enabled.add(i);
	            
	            tmp_pds.clear();
	            
	            // compute all the products in this step
	            for(Integer t : reaction_enabled)
	                tmp_pds = tmp_pds.or(products.get(t));
	            
	            if(Utils.DEBUG) {
	            	synchronized(this) {
		            	System.out.printf("'%s'",temp_steps);
		                System.out.printf("%s -> ", tmp_pds.toString());
	            	}
	            }
	            
	            // if it is equals to the previous product result or it produces nothing,
	            // leave the loop
	            if(/*pds.equals(tmp_pds) ||*/ tmp_pds.cardinality() == 0) {
	            	
	            	if(Utils.DEBUG)
	            		synchronized(this) {
	            			System.out.printf("Break");
	            		}
	            	
	                break;
	            }
	            
	            pds.clear();
	            it = tmp_pds.intIterator();
	            
	            while(it.hasNext())
	            	pds.set(it.next());
	            
	            temp_steps--;
	            
	            
	            if(Utils.ATTRACTOR_DETECTOR) {
	            	EWAHCompressedBitmap copy_pds = new EWAHCompressedBitmap(map_instances.size());
	            	it = pds.intIterator();
	                
	                while(it.hasNext())
	                	copy_pds.set(it.next());
	            	
	            	EWAHCompressedBitmap res = att.addAndCheckForPossibleCycle(copy_pds, temp_steps);
	            	
	            	if(res != null) { 
	            		if(Utils.DEBUG) {
		            		synchronized(this) {
			            		System.out.printf("---->'1'");
			                    System.out.printf("%s\n", res.toString());
		            		}
	            		}
	            		att.cleanPossibleCycle();
	            		return fromEWAHCompressedBitmap2SetOfStrings(res);
	            	}
	            }
	        }
	        
	        if(Utils.DEBUG)
	        	synchronized(this) {
	        		System.out.printf("\n");
	        	}
	       
	        if(Utils.ATTRACTOR_DETECTOR)
	        	att.cleanPossibleCycle();
	        
	        return fromEWAHCompressedBitmap2SetOfStrings(tmp_pds);
		}
	}
	
	private List<Future<List<ResultThread>>> tasks;
	private Map<EWAHCompressedBitmap, List<EWAHCompressedBitmap>> attractor_in_common; 
	
	@Override
	public List<ResultThread> simulateEvolutionofReactionSystemForAllFormulasMultithread(List<String> formulas){
		this.allFormulas = formulas;
		medium_time = new AtomicLong(0);
		medium_time_false = new AtomicLong(0);
		medium_time_true = new AtomicLong(0);
		trues = new AtomicInteger(0);
		trues_hashtab = new AtomicInteger(0);
		
		int listAmount;
		int block_size = (int) NUM_THREAD;
	    if(allFormulas.size()%block_size != 0)
	        listAmount = allFormulas.size()/block_size + 1;
	    else
	        listAmount = allFormulas.size()/block_size;

	    List<String>[] lists = new List[listAmount];

	    for(int i = 1; i <= listAmount; i++){
	        if(i * block_size > allFormulas.size()){
	            lists[i - 1] = allFormulas.subList( (i - 1) * block_size, allFormulas.size());
	        }
	        else{
	            lists[i - 1] = allFormulas.subList( (i - 1) * block_size, i * block_size);
	        }        
	    }
	    
	    executor = Executors.newFixedThreadPool(NUM_THREAD);
		true_formulas = new CopyOnWriteArrayList<>();
		results = new ArrayList<>();
		tasks = new ArrayList<>();
	    attractor_in_common = new HashMap<EWAHCompressedBitmap, List<EWAHCompressedBitmap>>(); 
	    ReadWriteLock lock = new ReentrantReadWriteLock();
	    Instant absolute_start = Instant.now();
	    for(List<String> subList : lists) {
	    	SimulationTask task = new SimulationTask(subList, attractor_in_common, lock);
			tasks.add(executor.submit(task));
	    }
	    
	    for(Future<List<ResultThread>> future : tasks)
			try {
				if(future != null)
					results.addAll(future.get());
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    
	    executor.shutdown();
	    
	    Instant absolute_finish = Instant.now();
	    long absolute_elapsed_time = Duration.between(absolute_start, absolute_finish).toMillis();
        
    	if(Utils.TIME){
            System.out.println("(Medium) elapsed time for checking one formula = " + (float)(medium_time.get() / formulas.size()));
            
            try{
                System.out.println("(Medium) elapsed time for checking one formula (true) = " + (float)(medium_time_true.get() / trues.get()));
            }catch(ArithmeticException ex){
                System.out.println("(Medium) elapsed time for checking one formula (true) = undef (no true formulas)");
            }
            System.out.println("Tot true formulas = " + trues);
            System.out.println("Formulas found to be true through hashing = " + trues_hashtab);
            
            try{
                System.out.println("(Medium) elapsed time for checking one formula (false) = " + (float)(medium_time_false.get() / (formulas.size() - trues.get())));
            }catch(ArithmeticException ex){
                System.out.println("(Medium) elapsed time for checking one formula (false) = undef (no false formulas)");
            }
            System.out.println("Total elapsed time = " + absolute_elapsed_time);
            System.out.println("Tot false formulas = " + (formulas.size() - trues.get() - trues_hashtab.get()));
            System.out.println("TOT formulas = " + formulas.size());
        }
	    
	    return results;
	}
	
	protected abstract boolean findTrueFormulaSubsequence(String t, String formula);

	public LinkedHashSet<LinkedHashSet<LinkedHashSet<String>>> getAllCyclesMultithread() {
		
		return AttractorDetectorMultithread.getAllCycles(attractor_in_common).stream()
				.map(t -> {
					return t.stream()
							.map(u -> {
								return Arrays.stream(u.toArray())
				                .mapToObj((i) -> {
				                    return this.map_instances
				                        .entrySet()
				                        .stream()
				                        .filter(entry -> Objects.equals(entry.getValue(), i))
				                        .map(Map.Entry::getKey)
				                        .findFirst()
				                        .get();
				                })
				                .filter(entry -> !entry.equals("create"))
				                .collect(Collectors.toCollection(LinkedHashSet<String>::new));
							})
							.collect(Collectors.toCollection(LinkedHashSet<LinkedHashSet<String>>::new));
				})
				.collect(Collectors.toCollection(LinkedHashSet<LinkedHashSet<LinkedHashSet<String>>>::new));
	}
}
