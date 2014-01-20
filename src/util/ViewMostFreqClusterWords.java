package util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ViewMostFreqClusterWords {
	public static void main(String[] args) throws IOException {
		int NUM = 20; //display top N words
		int CLUSTERS = 50;
		String inFile = "/home/anjan/workspace/HMM/out/decoded/nepali_decoded/nepali_train.txt.decoded";
		
		int WORD_COL = 2;
		int HMM_COL = 1;
		//BufferedReader br = new BufferedReader(new FileReader(inFile));
		BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream(inFile), "UTF-8"));
		ClusterContent[] clusters = new ClusterContent[CLUSTERS];
		for(int i=0; i<CLUSTERS; i++) {
			clusters[i] = new ClusterContent();
		}
		String line = "";
		while ( (line = br.readLine()) != null) {
			line = line.trim();
			if(! line.isEmpty() ) {
				String[] splitted = line.split("(\\s+|\\t+)");
				int hmm = Integer.parseInt(splitted[HMM_COL-1]);
				//System.out.println(hmm);
				String word = splitted[WORD_COL-1];
				clusters[hmm].add(word.toLowerCase());
			}
		}
		int counter = 0;
		for( ClusterContent cc : clusters ) {
			displayTop(counter, NUM, cc);
			counter++;
		}
		counter = 0;
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(inFile + ".cluster"), "UTF-8"));
		for( ClusterContent cc : clusters ) {
			writeTop(pw, counter, NUM, cc);
			counter++;
		}
		
		pw.close();
		
		br.close();
	}
	
	public static void writeTop(PrintWriter pw, int counter, int num, ClusterContent cc){
		Map<String, Integer> map = new HashMap<String, Integer>();
		for(String word : cc) {
			if(map.containsKey(word)) {
				map.put(word, map.get(word) + 1);
			} else {
				map.put(word, 1);
			}
		}
        ValueComparator bvc =  new ValueComparator(map);
        TreeMap<String,Integer> sorted_map = new TreeMap(bvc);
        sorted_map.putAll(map);
        
        //display
        pw.print("Cluster " + counter + " : ");
        int dispCount = 0;
        for(String key : sorted_map.keySet()) {
        	if(dispCount == num) break;
        	pw.print(key + " ");
        	dispCount++;
        }
        pw.println();
	}
	
	public static void displayTop(int counter, int num, ClusterContent cc){
		Map<String, Integer> map = new HashMap<String, Integer>();
		for(String word : cc) {
			if(map.containsKey(word)) {
				map.put(word, map.get(word) + 1);
			} else {
				map.put(word, 1);
			}
		}
        ValueComparator bvc =  new ValueComparator(map);
        TreeMap<String,Integer> sorted_map = new TreeMap(bvc);
        sorted_map.putAll(map);
        
        //display
        System.out.print("Cluster " + counter + " : ");
        int dispCount = 0;
        for(String key : sorted_map.keySet()) {
        	if(dispCount == num) break;
        	System.out.print(key + " ");
        	dispCount++;
        }
        System.out.println();
	}
}

class ValueComparator implements Comparator {

	  Map base;
	  public ValueComparator(Map base) {
	      this.base = base;
	  }

	  public int compare(Object a, Object b) {

	    if((Integer)base.get(a) < (Integer)base.get(b)) {
	      return 1;
	    } else if((Integer)base.get(a) == (Integer)base.get(b)) {
	      return 0;
	    } else {
	      return -1;
	    }
	  }
}

