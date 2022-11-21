package predictor_fbp_refactor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author valer
 */
public final class Utils {
    
    // equivalent form for #ifdef / #ifnotdef used in C++
    public static final boolean TIME = true;
    public static final boolean EFFICIENT_IMPLEMENTATION = true;
    public static boolean NO_INHIBITORS = false;
    public static boolean MULTITHREAD = false;
    public static boolean DUMMY_NODE = false;
    public static final boolean ONLY_COMPLETE_FORMULA = false;
    public static final boolean DEBUG = false;
    public static final boolean DEBUG_MULTITHREAD = false;
    public static final boolean ATTRACTOR_DETECTOR = true;
    
    // Returns true if str1[] is a subsequence
    // of str2[] m is length of str1 and n is
    // length of str2
    public static boolean isSubSequence(String str1, String str2, int m, int n)
    {
    	
    	/*String[] words = str1.split("\\s+");
    	
        for (String msbp : words) {
        	StringBuffer sb = new StringBuffer();
            sb.append("(^|\\s)").append(msbp.trim().replaceAll("([^0-9A-Za-z ])", "\\\\$1")).append("\\b");
            Pattern p = Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE);

            Matcher m1 = p.matcher(str2);
            if(!m1.find())
            	return false;
        }*/
    	// it can be simply one word searched in a one-word sentence (equals case) e.g. str1=-B vs str2=-B
    	if(str1.equals(str2))
    		return true;
    	
    	// if this is not the case, search every single word of str1 in the str2 sentence
    	String[] words = str1.split("\\s+");
    	
    	// we need to detach msbp so as to avoid 'false true' cases in which 
    	// e.g. B is contained in A [B]
    	for(String msbp : words) {
    		String middle = " "+msbp+" ";
    		String start = msbp + " ";
    		String end = " "+msbp;

    		// str2 is a sentence, so the single word msbp can be at the start, at the beginning or at the end...
    		if((!str2.contains(middle) && !str2.contains(start) && !str2.contains(end))) {
    			return false;		
    		}
    	}
        
        //System.out.println("TRUE >> " + str1 + " vs " + str2);
        
        return true;  
    }
    
    public static Set<Set<String>> getAllCombinations(Collection<String> inputSet) {
        // use inputSet.stream().distinct().collect(Collectors.toList());
        // to get only distinct combinations
        //  (in case source contains duplicates, i.e. is not a Set)
        List<String> input = new ArrayList<>(inputSet);
        final int size = input.size();
        // sort out input that is too large. In fact, even lower numbers might
        // be way too large. But using <63 bits allows to use long values
        if(size>=63) throw new OutOfMemoryError("not enough memory for "
            +BigInteger.ONE.shiftLeft(input.size()).subtract(BigInteger.ONE)+" permutations");

        // the actual operation is quite compact when using the Stream API
        return LongStream.range(1, 1L<<size).parallel() 
            .mapToObj(l -> BitSet.valueOf(new long[] {l}).stream()
                .mapToObj(input::get).collect(Collectors.toSet()))
            .collect(Collectors.toSet());
    }
    
    public static List<String> getAllCombinationsAndFilter(Collection<String> inputSet, Set<String> must, Set<String> must_not) {
        // use inputSet.stream().distinct().collect(Collectors.toList());
        // to get only distinct combinations
        //  (in case source contains duplicates, i.e. is not a Set)
        List<String> input = new ArrayList<>(inputSet);
        final int size = input.size();
        final String toReplace = Utils.NO_INHIBITORS ? "\\[(.*?)\\]" : "\\¬(.*?)";
        final String not = Utils.NO_INHIBITORS ? "[" : "¬";
        // sort out input that is too large. In fact, even lower numbers might
        // be way too large. But using <63 bits allows to use long values
        if(size>=63) throw new OutOfMemoryError("not enough memory for "
            +BigInteger.ONE.shiftLeft(input.size()).subtract(BigInteger.ONE)+" permutations");

        // the actual operation is quite compact when using the Stream API
        return LongStream.range(1, 1L<<size).parallel() 
            .mapToObj(l -> Stream.concat(Stream.concat(must.stream(), must_not.stream()),BitSet.valueOf(new long[] {l}).stream()
                .mapToObj(input::get))
                .sorted((o1, o2) -> {
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
                }).collect(Collectors.joining(" ")))
            
            /*.sorted((o1, o2) -> {
                return Integer.valueOf(o1.size()).compareTo(o2.size());
            })*/
            .filter((t) -> {
            	
                // this is a pattern to find more than one occurrence
                // of the same word (corresponds to contradictory case)
            	String replaced = t.replaceAll(toReplace, "$1");
                String reg = "(?i)(\\b\\w+\\b)(.*?) \\b\\1\\b";
                Pattern p = Pattern.compile(reg);
                Matcher m = p.matcher(replaced);
                return !m.find();
            })
            .collect(Collectors.toList());
    }
    
}
