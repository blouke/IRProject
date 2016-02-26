package ir.query;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import ir.indexer.DocInfo;
import ir.indexer.TermIndexer;
import ir.indexer.TokenInfo;
import ir.indexer.TokenOccurrence;

public class QueryProcessor {
	private TermIndexer index;
	private HashMap<String,Double> queryIndex;

	public QueryProcessor(TermIndexer index){
		this.index = index;
		queryIndex = new HashMap<String,Double>();
	}


	public ArrayList<Document> processQuery(String query){
		try {
			StandardTokenizer stream = new StandardTokenizer();
			stream.setReader(new StringReader(query));
			TokenStream tokenStream = new StopFilter(stream, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
			tokenStream = new PorterStemFilter(tokenStream);
			tokenStream.reset();
			CharTermAttribute token = tokenStream.addAttribute(CharTermAttribute.class);
			while (tokenStream.incrementToken()){
				String term = token.toString();
				queryIndex.put(term,1d);
			}
		tokenStream.end();
		tokenStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return generateResults();
}

	
public ArrayList<Document> generateResults(){

	HashMap<DocInfo, Double> searchResult = new HashMap<DocInfo,Double>();
	double queryVectorLength = 0d;
	
//	System.out.println(queryIndex.keySet());

	for (Map.Entry<String, Double> dictionaryEntry: queryIndex.entrySet()){
		String token = dictionaryEntry.getKey();
	
		if (index.dictionary.containsKey(token)){
			TokenInfo tokenInfo = index.dictionary.get(token);
			
			for (Map.Entry<Integer, TokenOccurrence> tokenOccEntry: tokenInfo.getOccMap().entrySet()){
				int docId = tokenOccEntry.getKey();
				int tokenCount = tokenOccEntry.getValue().getCount();
				double idf = tokenInfo.getIdf();
				DocInfo docInfo = index.docInfoList.get(docId);
				
				if (searchResult.containsKey(docInfo)){
					double score = searchResult.get(docInfo);
					double newScore = score*tokenCount*idf;
					searchResult.put(docInfo, newScore);
				} else {
					searchResult.put(docInfo, tokenCount*idf);
				}
			}
		}
	}
	
	// calculate the lenght of query vector
	for (Double l: queryIndex.values()){
		queryVectorLength += Math.pow(l, 2);
	}
	queryVectorLength = Math.sqrt(queryVectorLength);
	

	// calculate similarity score
	ArrayList<Document> result = new ArrayList<Document>();
	for (Map.Entry<DocInfo, Double> entry: searchResult.entrySet()){
		DocInfo docInfo = entry.getKey();
		double dotProduct = 5d;
		double norm = docInfo.getLength()*queryVectorLength;
		double score = dotProduct/norm;
		String snippet = "description";
		result.add(new Document(score,docInfo.getUrl(),snippet));
	}
	
	// sort the result list
	Collections.sort(result);
	return result;
}
}
