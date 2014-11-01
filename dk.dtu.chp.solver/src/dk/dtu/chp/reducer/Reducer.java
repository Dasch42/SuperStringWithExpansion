package dk.dtu.chp.reducer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import dk.dtu.chp.decoder.SWEDecoder;
import dk.dtu.chp.optimized_decoder.Decoder;

public class Reducer {
	// personal
	//ArrayList<String> substrings;
	HashSet<String> substrings;
	HashMap<Character, ArrayList<String>> personal_r;
	boolean print;
	
	// parsed
	SWEDecoder decoder;
	
	public Reducer(SWEDecoder decoder, boolean print){
		// Parsed variables
		this.decoder = decoder;
		
		// Personal variables
		this.print = print;
		//this.substrings = new ArrayList<String>();
		this.substrings = new HashSet<String>();
		this.personal_r = new HashMap<Character, ArrayList<String>>(this.decoder.getR());
	}
	
	private void printSetSize(HashMap<Character, ArrayList<String>> set){
		for( int i = 65 ; i < 91 ; i++ ){
			char c = (char)i;
			
			if( set.containsKey(c) ){
				ArrayList<String> t = set.get(c);
				System.out.print("" + c + ": [" + t.size() + "] ");
				for(int j = 0;j<t.size();j++){
					System.out.print("" + t.get(j)+ ",");
				}
				System.out.println();
					
			}
			
		}
	}
	
	private void constructSubstrings(){
		for( int i = 0 ; i < decoder.getS().length() ; i++ ){
			for( int j = 1 ; j <= decoder.getS().length() - i ; j++ ){
				//this.substrings.add(decoder.getS().substring(i, j+i));
				this.substrings.add(decoder.getS().substring(i, j+i));
			}
		}
	}
	
	private void removeUnused(){
		int[] is = new int[26];
		for( int i = 0 ; i < this.decoder.getT().size() ; i++ ){
			for( int j = 0; j < this.decoder.getT().get(i).length() ; j++ ){

				if( Character.isUpperCase(this.decoder.getT().get(i).charAt(j)) ){ 
					int index = (int) this.decoder.getT().get(i).charAt(j) - 65;
					is[index] = 1;
				}
			}
		}
		
		for( int i = 0 ; i < 26 ; i++ ){
			if( is[i] == 0 ){
				char c = (char) (i+65);
				this.personal_r.remove(c);
			}
		}
	}
	
	private void reduceSet(HashMap<Character, ArrayList<String>> newR){
		for( int i = 65 ; i < 91 ; i++ ){
			char c = (char)i;
			
			// For each: A,B, ... Z
			if( this.personal_r.containsKey(c) ){
				// Get the list of replacements
				ArrayList<String> temp = this.personal_r.get(c);
				ArrayList<String> newSet = new ArrayList<String>();
				
				// for each of the replacements check if it exists in the permutations
				for( int j = 0 ; j < temp.size() ; j++ ){
					if( this.substrings.contains(temp.get(j))){
						newSet.add(temp.get(j));
					}
				}
				newR.put(c, newSet);	
			}
		}
	}
	
	private HashMap<Character, String> recursive(HashMap<Character, ArrayList<String>> set, ArrayList<String> T, HashMap<Character, String> assigned){
	
		
		// Completely naive depth first solution
		
		// Stop criteria
		if( set.isEmpty() ){
			ArrayList<String> testT = new ArrayList<String>();
			
			for( String s :  T ){
				String t = "";

				for( int i = 0 ; i < s.length() ; i++ ){
					
					if( Character.isUpperCase( s.charAt(i) ) ){
						t = t + assigned.get(s.charAt(i));
					}else{
						t = t + s.charAt(i);
					}
				}
				testT.add(t);
			}
			
			for( String s : testT ){
				if( !this.substrings.contains(s) ){
					return null;
				}
			}
			return assigned;
		}

		
		// Remove first entry in set, assign new variables in assigned.
		for( int i = 65 ; i < 91 ; i++ ){
			char c = (char)i;
			if( set.containsKey(c) ){
				HashMap<Character, ArrayList<String>> tempSet = new HashMap<Character, ArrayList<String>>(set);
				
				
				ArrayList<String> temp = tempSet.get(c);
				tempSet.remove(c);

				for( int j = 0 ; j < temp.size() ; j++ ){
					HashMap<Character, String> newAssigned = new HashMap<>(assigned);
					newAssigned.put(c, temp.get(j));
					
					HashMap<Character, String> result = recursive(tempSet, T, newAssigned);
					if( result != null ){
						return result;
					}
				}
			}
		}
		
		return null;
	}
	
	
	

	private HashMap<Character, ArrayList<String>> recursive_singular(HashMap<Character, ArrayList<String>> set, String T, int t_index, HashMap<Character, String> assigned){
			if( T.length() == t_index ){
				String T2 = "";
				for( int i = 0 ; i < t_index ; i++ ){
					if( Character.isUpperCase(T.charAt(i)) ){
						T2 = T2 + assigned.get(T.charAt(i));
					}else{
						T2 = T2 + T.charAt(i);
					}
				}
				HashMap<Character, ArrayList<String>> basis_set = new HashMap<Character, ArrayList<String>>();
				if( T2 != "" && this.substrings.contains(T2) ){
					for( Character chr : assigned.keySet() ){
						ArrayList<String> basis_list = new ArrayList<String>();
						basis_list.add( assigned.get(chr) );
						basis_set.put(chr, basis_list);
					}
				}
				return basis_set;
			}
		
		
			char c = T.charAt(t_index);
			if( Character.isLowerCase(c) ){
				return recursive_singular(set, T, t_index+1, assigned);
			}
			
			ArrayList<String> temp = set.get(c);
			HashMap<Character, ArrayList<String>> newSet = new HashMap<Character, ArrayList<String>>(set);
			newSet.remove(c);
			
			HashMap<Character, ArrayList<String>> accumulatedResult = new HashMap<Character, ArrayList<String>>();
			for( int j = 0 ; j < temp.size() ; j++ ){
				HashMap<Character, String> newAssigned = new HashMap<Character, String>(assigned);
				newAssigned.put(c, temp.get(j));
				
				HashMap<Character, ArrayList<String>> result = recursive_singular(set, T, t_index+1, newAssigned);
				
				for( Character chr : result.keySet() ){
					ArrayList<String> insertIntoList = result.get(chr);	
					ArrayList<String> mainList = accumulatedResult.get(chr);
					if( mainList == null ){
						mainList = new ArrayList<String>();
					}
					
					for(String s : insertIntoList ){
						if(!mainList.contains(s)){
							mainList.add(s);
						}
					}
					
					if( accumulatedResult.get(chr) == null ){
						accumulatedResult.put(chr, mainList);
					}
					
					for( String s : (ArrayList<String>) result.get(chr) ){
						if( accumulatedResult.get(chr) != null && !accumulatedResult.get(chr).contains(s) ){
							accumulatedResult.get(chr).add(s);
						}
					}
				}
				
				
				
			}
			
			return accumulatedResult;
			
			
			
	}
	
	private void printResult(HashMap<Character, String> assigned){
		if( assigned == null ){
			System.out.println("NO");
		}else{
			for( Character c : assigned.keySet() ){
				System.out.println(c + ":" + assigned.get(c));
			}
		}
	}
	
	private void printResultList(HashMap<Character, ArrayList<String>> assigned){
		if( assigned == null ){
			System.out.println("NO");
		}else{
			for( Character c : assigned.keySet() ){
				System.out.println(c + ":" + assigned.get(c).get(0));
			}
		}
	}
	
	
	
	public void start(){
		if(this.print){
			System.out.println("Before reduction");
			this.printSetSize(this.decoder.getR());
		}
		this.removeUnused();
		
		this.constructSubstrings();
		HashMap<Character, ArrayList<String>> newR = new HashMap<Character, ArrayList<String>>();
		this.reduceSet(newR);
	
		if(this.print){
			System.out.println("After reduction");
			this.printSetSize(newR);
		}
		
		HashMap<Character, String> assigned = new HashMap<Character, String>();
		HashMap<Character, ArrayList<String>> result = null;
		for(String s : this.decoder.getT() ){
			result = recursive_singular(newR, s, 0, assigned);
			
			if( result.isEmpty() ){
				System.out.println("NO");
				return;
			}
			
			for( Character c : result.keySet() ){
				newR.remove(c);
				newR.put(c, result.get(c));
			}
		}
		
				
		System.out.println(newR);
		//this.printResultList(newR);
		//HashMap<Character, String> result = this.recursive(newR, this.decoder.getT(), assigned);
		//this.printResult(newR);

		
	}
	
	
	
	
	
	
}
