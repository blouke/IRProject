package ir.query;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import ir.indexer.TermIndexer;

public class QueryProcessor {
	private TermIndexer index;

	public QueryProcessor(TermIndexer index){
		this.index = index;
	}


	public HashMap<String,Double> processQuery(String query){
		HashMap<String,Double> queryIndex = new HashMap<String,Double>();
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
	return queryIndex;
}

	
public ArrayList<Document> generateResults(String query){
	HashMap<String,Double> queryIndex = processQuery(query);
	System.out.println(queryIndex.keySet());






	return null;
}
}
