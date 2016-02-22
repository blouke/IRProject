package ir.indexer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	
	private static List<DocInfo> docInfoList = new ArrayList<DocInfo>();
	private static HashMap<String, TokenInfo> dictionary = new HashMap<String, TokenInfo>();
	private static int docId=0;
	
	public static void main(String[] args) {
		try {
			PatternTokenizer tokenStream = new PatternTokenizer(Pattern.compile("(?s)(?<=URL::\\s).*?(?=Recno::)"),0);
			tokenStream.setReader(new InputStreamReader(new FileInputStream(new File("resources/dump"))));
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
//				System.out.println(url);	// remove this line
				if (content.length()>0){
					docInfoList.add(new DocInfo(docId,url));
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
				System.out.println("Term :"+key+"\tIDF :"+tokenInfo.getIdf());
			}
			
//			for (TokenInfo tokenInfo: dictionary.values()){
//		    	tokenInfo.calculateIdf(docId); 
//		    	System.out.println("IDF :"+tokenInfo.getIdf()); 		//remove this line
//		    }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static void createIndex(String content, int docId){
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
//				System.out.println(token.toString());	//remove this line
			}
			tokenStream.end();
			tokenStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
