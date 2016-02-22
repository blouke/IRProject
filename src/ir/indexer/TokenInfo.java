package ir.indexer;

import java.util.HashMap;

public class TokenInfo {
	
	private double idf;
	private HashMap<Integer,TokenOccurrence> occMap;
	
	public TokenInfo() {
		this.idf = 0.0;
		this.occMap = new HashMap<Integer,TokenOccurrence>();
	}
	
	public void addTokenOccurrence(int docId, int count){
		if (occMap.containsKey(docId)){
			TokenOccurrence tokenOccc = occMap.get(docId);
			tokenOccc.addTokenOcc(count);
		} else {
			occMap.put(docId, new TokenOccurrence(docId,count));
		}
	}
}
