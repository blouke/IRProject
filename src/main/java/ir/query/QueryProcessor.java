package ir.query;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

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
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.PointerUtils;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.dictionary.Dictionary;

public class QueryProcessor {
	private TermIndexer index;
	private HashMap<String,Double> queryIndex;

	public QueryProcessor(TermIndexer index){
		this.index = index;
		queryIndex = new HashMap<String,Double>();
	}


	public ArrayList<Document> processQuery(String query){
		try {
			
			String jwnlProp = System.getProperty("jwnlProp");
			JWNL.initialize(new FileInputStream(jwnlProp));
			final Dictionary wordnet = Dictionary.getInstance();

			StandardTokenizer stream = new StandardTokenizer();
			stream.setReader(new StringReader(query));
			TokenStream tokenStream = new StopFilter(stream, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
			tokenStream.reset();
			CharTermAttribute token = tokenStream.addAttribute(CharTermAttribute.class);
			StringBuilder str = new StringBuilder();
			
			while (tokenStream.incrementToken()){
				String term = token.toString();
				str.append(term+"\t");
				IndexWord indexWord = wordnet.lookupIndexWord(POS.NOUN, term);
				if (indexWord != null){
					Synset[] senses = indexWord.getSenses();
					for (Synset synset: senses){
						Word[] words = synset.getWords(); 
						for (Word word: words){
							str.append(word.getLemma()+"\t");
						}
					
//					PointerTargetNodeList relatedList = PointerUtils.getInstance().getSynonyms(synset);					
//					Iterator i = relatedList.iterator();
//					while (i.hasNext()){
//						PointerTargetNode synonymNode = (PointerTargetNode)i.next();
//						Synset synonym = synonymNode.getSynset();
//						Word[] words = synonym.getWords();
//						for (Word w: words){
//							str.append(w.getLemma());
//						}
//					}
				}
			}
		}
			
			tokenStream.end();
			tokenStream.close();


			stream.setReader(new StringReader(str.toString()));
			TokenStream newTokenStream = new PorterStemFilter(tokenStream);
			newTokenStream.reset();
			CharTermAttribute newToken = newTokenStream.addAttribute(CharTermAttribute.class);
			while (newTokenStream.incrementToken()){
				String term = newToken.toString();
				queryIndex.put(term,1d);
			}
			newTokenStream.end();
			newTokenStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		
		} catch (JWNLException e) {
			// TODO Auto-generated catch block
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
				queryIndex.put(token, 1*tokenInfo.getIdf());

				for (Map.Entry<Integer, TokenOccurrence> tokenOccEntry: tokenInfo.getOccMap().entrySet()){
					int docId = tokenOccEntry.getKey();
					int tokenCount = tokenOccEntry.getValue().getCount();
					double idf = tokenInfo.getIdf();
					DocInfo docInfo = index.docInfoList.get(docId);

					if (searchResult.containsKey(docInfo)){
						double score = searchResult.get(docInfo);
						double newScore = score + (queryIndex.get(token)*(tokenCount*idf));
						searchResult.put(docInfo, newScore);
					} else {
						searchResult.put(docInfo, queryIndex.get(token)*tokenCount*idf);
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
			double dotProduct = entry.getValue();
			double denominator = docInfo.getLength()*queryVectorLength;
			double score = dotProduct/denominator;
			String snippet = "description";
			result.add(new Document(score,docInfo.getUrl(),snippet));
		}

		// sort the result list
		Collections.sort(result);
		return result;
	}
}
