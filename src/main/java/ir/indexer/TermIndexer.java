package ir.indexer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.pattern.PatternTokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import ir.indexer.DocInfo;
import ir.indexer.TokenInfo;

public class TermIndexer {
	
	public static HashMap<Integer,DocInfo> docInfoList = new HashMap<Integer,DocInfo>();
	public static HashMap<String, TokenInfo> dictionary = new HashMap<String, TokenInfo>();
	private static int docId=0;
	private InputStream inputStream;
	
	public TermIndexer(InputStream inputStream){
		this.inputStream = inputStream;
	}
	
	public void initialize() {
		try {
			PatternTokenizer tokenStream = new PatternTokenizer(Pattern.compile("(?s)(?<=URL::\\s).*?(?=Recno::)"),0);
			tokenStream.setReader(new InputStreamReader(inputStream));
			tokenStream.reset();
			CharTermAttribute token = tokenStream.addAttribute(CharTermAttribute.class);
			String[] tokenString;
			String content;
			String url;
			while (tokenStream.incrementToken()){
				tokenString = token.toString().split("\\R",4);
				url = tokenString[0];
				content = tokenString[3];
				content = content.trim();
				if (content.length()>0){
					docInfoList.put(docId, new DocInfo(docId,url));
					createIndex(content, docId);
					docId+=1;
				}
			}
			tokenStream.end();
			tokenStream.close();
			
			// calculating IDF
			for (Map.Entry<String, TokenInfo> entry: dictionary.entrySet()){
				String key = entry.getKey();
				TokenInfo tokenInfo = entry.getValue();
				tokenInfo.calculateIdf(docId);
//				System.out.println("Term :"+key+"\tIDF :"+tokenInfo.getIdf());	`remove this line
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void createIndex(String content, int docId){
		try {
			StandardTokenizer stream = new StandardTokenizer();
			stream.setReader(new StringReader(content));
			TokenStream tokenStream = new StopFilter(stream, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
			tokenStream = new PorterStemFilter(tokenStream);
			tokenStream.reset();
			CharTermAttribute token = tokenStream.addAttribute(CharTermAttribute.class);
			while (tokenStream.incrementToken()){
				String term = token.toString();
				TokenInfo tokenInfo = dictionary.get(term);
				if (tokenInfo!=null){
					tokenInfo.addTokenOccurrence(docId,1);
				} else {
					TokenInfo newTokenInfo = new TokenInfo();
					newTokenInfo.addTokenOccurrence(docId,1);
					dictionary.put(term, newTokenInfo);
				}
			}
			tokenStream.end();
			tokenStream.close();
			
			
			// calculate document length
			
			for (Map.Entry<String, TokenInfo> dictionaryEntry: dictionary.entrySet()){
				double idf = dictionaryEntry.getValue().getIdf();
				for (Map.Entry<Integer, TokenOccurrence> tokenOccEntry: dictionaryEntry.getValue().getOccMap().entrySet()){
					int tokenCount =tokenOccEntry.getValue().getCount();
					DocInfo docInfo = docInfoList.get(tokenOccEntry.getKey());
					docInfo.setLength(docInfo.getLength()+Math.pow(tokenCount*idf, 2));
				}
			}
			
			for (Map.Entry<Integer, DocInfo> doc: docInfoList.entrySet()){
				double newLength = Math.sqrt(doc.getValue().getLength());
				doc.getValue().setLength(newLength);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
