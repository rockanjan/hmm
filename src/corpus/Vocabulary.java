package corpus;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import util.SmoothWord;

public class Vocabulary {
	boolean debug = false;
	boolean smooth = true;
	boolean lower = true;
	public int featureThreshold = 0;
	//index zero reserved for __OOV__ (low freq features)
	
	private int index = 1;
	public int vocabSize = -1;
	public String UNKNOWN = "*unk*";
	public Map<String, Integer> wordToIndex = new HashMap<String, Integer>();
	public ArrayList<String> indexToWord = new ArrayList<String>();
	public Map<Integer, Integer> indexToFrequency = new HashMap<Integer, Integer>();
	
	private int addItem(String word) {
		int returnId = -1;
		if(wordToIndex.containsKey(word)) {
			int wordIndex = wordToIndex.get(word);
			int oldFreq = indexToFrequency.get(wordIndex);
			indexToFrequency.put(wordIndex, oldFreq + 1);
			returnId = wordIndex;
		} else {
			wordToIndex.put(word, index);
			indexToWord.add(word);
			indexToFrequency.put(index, 1);
			returnId = index;
			index++;
		}
		return returnId;
	}
	
	public void readVocabFromFile(Corpus c, String filename) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line = null;
		wordToIndex.put(UNKNOWN, 0);
		indexToFrequency.put(0, 0);
		indexToWord.add(UNKNOWN);
		while( (line = br.readLine()) != null) {
			line = line.trim();
			if(! line.isEmpty()) {
				String words[] = line.split(c.delimiter);
				for(int i=0; i<words.length; i++) {
					String word = words[i];
					if(lower) {
						word = word.toLowerCase();
					}
					if(smooth) {
						word = SmoothWord.smooth(word);
					}
					int wordId = addItem(word);					
				}
			}
		}
		vocabSize = wordToIndex.size();
		System.out.println("Vocab Size including UNKNOWN : " + vocabSize);
		if(debug) {
			c.debug();
		}		
		br.close();
		
	}
	
	//reads from the dictionary
	public void readVocabFromDictionary(String filename) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		String line = null;
		try {
			line = br.readLine().trim();
			vocabSize = Integer.parseInt(line);
			while( (line = br.readLine()) != null) {
				line = line.trim();
				if(line.isEmpty()) {
					continue;
				}
				addItem(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("error reading vocab file");
		}
		if(vocabSize != wordToIndex.size()) {
			System.out.println("Vocab file corrputed: header size and the vocab size do not match");
			System.exit(-1);
		}
	}
	
	public int getIndex(String word) {
		if(wordToIndex.containsKey(word)) {
			return wordToIndex.get(word);
		} else {
			//word not found in vocab
			if(debug) {
				System.out.println(word + " not found in vocab");
			}
			return 0; //unknown id
		}
	}
	
}
