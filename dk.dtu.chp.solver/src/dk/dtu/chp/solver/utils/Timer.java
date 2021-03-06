package dk.dtu.chp.solver.utils;

import java.util.ArrayList;

public class Timer {
	private static long start;
	private static ArrayList<Long> time = new ArrayList<Long>();
	
	public Timer(){
		
	}

	
	public void start(){
		start = System.nanoTime(); 
	}

	/**
	 * records the current time
	 * @return time since start
	 */
	public double time(){
		time.add(System.nanoTime()-start);
		return Time.NanoSeconds.ToSeconds(time.get(time.size()-1));
	}

	/**
	 * 
	 * @return time since last time
	 */
	public double difference(){
		time.add(System.nanoTime()-start);
		if(time.size()>=2){
			return Time.NanoSeconds.ToSeconds(time.get(time.size()-1)-time.get(time.size()-2));
		}else{
			return Time.NanoSeconds.ToSeconds(time.get(time.size()-1));
		}
	}
	
	public ArrayList<Long> getTimes(){
		return new ArrayList<Long>(time);
	}
	
	public String toSring(){
		return time.size()>0? Time.NanoSeconds.ToHMS(time.get(time.size()-1)):"Time not registered";
	}
}
