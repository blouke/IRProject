package ir.query;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import ir.indexer.DocInfo;
import ir.indexer.TermIndexer;
import ir.indexer.TermOccurrence;
import ir.indexer.TokenInfo;
import ir.indexer.TokenOccurrence;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.dictionary.Dictionary;
 
/*
This class will process the query by doing the following: 
1.Remove stop words. 
2.Stemming the query words.
3.Expansion the quary by WordNet
4.Calculate the lenght of query vector and IDF (the IDF=1 for all query words)  
5.Measure the similarty
6.Generate the results
7.Expansion the query by the relevance feedback ( if feedback provided )
8.Measure the similarty 
9.Regenerate the results
*/
public class QueryProcessor {

    private TermIndexer index;
    private HashMap<String, Double> queryIndex;
    private Dictionary wordnet;

    public QueryProcessor(TermIndexer index) {
        this.index = index;
        queryIndex = new HashMap<String, Double>();
        try {
            JWNL.initialize(new FileInputStream(System.getProperty("jwnlProp")));
            wordnet = Dictionary.getInstance();
        } catch (FileNotFoundException | JWNLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
/**
 * This method will do stop words removal, stemming and expanding the query with the use of WordNet
 */
    public void processQuery(String query) {
        try {

            StandardTokenizer stream = new StandardTokenizer();
            stream.setReader(new StringReader(query));
            TokenStream tokenStream = new StopFilter(stream, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
            tokenStream.reset();
            CharTermAttribute token = tokenStream.addAttribute(CharTermAttribute.class);
            StringBuilder str = new StringBuilder();

            while (tokenStream.incrementToken()) {
                String term = token.toString();
                str.append(term + "\t");
                IndexWord indexWord = wordnet.lookupIndexWord(POS.NOUN, term);
                if (indexWord != null) {
                    Synset[] senses = indexWord.getSenses();
                    for (Synset synset : senses) {
                        Word[] words = synset.getWords();
                        for (Word word : words) {
                            str.append(word.getLemma() + "\t");
                        }
                    }
                }
            }

            tokenStream.end();
            tokenStream.close();

            stream.setReader(new StringReader(str.toString()));
            TokenStream newTokenStream = new PorterStemFilter(tokenStream);
            newTokenStream.reset();
            CharTermAttribute newToken = newTokenStream.addAttribute(CharTermAttribute.class);
            while (newTokenStream.incrementToken()) {
                String term = newToken.toString();
                queryIndex.put(term, 1d);
            }
            newTokenStream.end();
            newTokenStream.close();
        } catch (IOException | JWNLException e) {
            e.printStackTrace();
        }

    }
    
    /*
    First this method will calculate the TF-IDF for each term in the query as well as the actual indexed terms. 
    Second calculate the lenght of query vector and calculate the similarty score.last sort the results decreasingly and return it. 
    */

    public ArrayList<Document> generateResults() {

        HashMap<DocInfo, Double> searchResult = new HashMap<DocInfo, Double>();
        double queryVectorLength = 0d;

        for (Map.Entry<String, Double> dictionaryEntry : queryIndex.entrySet()) {
            String token = dictionaryEntry.getKey();

            if (index.dictionary.containsKey(token)) {
                TokenInfo tokenInfo = index.dictionary.get(token);
                queryIndex.put(token, 1 * tokenInfo.getIdf());

                for (Map.Entry<Integer, TokenOccurrence> tokenOccEntry : tokenInfo.getOccMap().entrySet()) {
                    int docId = tokenOccEntry.getKey();
                    int tokenCount = tokenOccEntry.getValue().getCount();
                    double idf = tokenInfo.getIdf();
                    DocInfo docInfo = index.docInfoList.get(docId);

                    if (searchResult.containsKey(docInfo)) {
                        double score = searchResult.get(docInfo);
                        double newScore = score + (queryIndex.get(token) * (tokenCount * idf));	// incrementing dot-product
                        searchResult.put(docInfo, newScore);
                    } else {
                        searchResult.put(docInfo, queryIndex.get(token) * tokenCount * idf);
                    }
                }
            }
        }

        // calculate the lenght of query vector
        for (Double l : queryIndex.values()) {
            queryVectorLength += Math.pow(l, 2);
        }
        queryVectorLength = Math.sqrt(queryVectorLength);

        // calculate similarity score using Cosine Similarity 
        ArrayList<Document> result = new ArrayList<Document>();
        for (Map.Entry<DocInfo, Double> entry : searchResult.entrySet()) {
            DocInfo docInfo = entry.getKey();
            int docId = docInfo.getId();
            double dotProduct = entry.getValue();
            double denominator = Math.sqrt(docInfo.getLength()) * queryVectorLength;
            double score = dotProduct / denominator;
            String snippet = docInfo.getSnippet();
            result.add(new Document(docId, score, docInfo.getUrl(), snippet));
        }

        // sort the result list
        Collections.sort(result);
        return result;
    }

    /*
    If the relevance feedback is provided this method will recalculate the results by implementing Rocchio method
    */
    public ArrayList<Document> generateUpdatedResults(String[] docIds, String[] relevance) {

        HashMap<String, Double> positiveFeedback = new HashMap<String, Double>();
        HashMap<String, Double> negativeFeedback = new HashMap<String, Double>();
        int numPositiveDoc = 0;
        int numNegativeDoc = 0;

        for (int i = 0; i < relevance.length; i++) {
            TermOccurrence docVec = index.docVectors.get(Integer.parseInt(docIds[i]));
            if (relevance[i].equals("1")) {
                numPositiveDoc++;
                for (Map.Entry<String, Double> termOcc : docVec.getTermOccMap().entrySet()) {
                    String term = termOcc.getKey();
                    Double count = termOcc.getValue();
                    if (positiveFeedback.containsKey(term)) {
                        positiveFeedback.put(term, positiveFeedback.get(term) + count);
                    } else {
                        positiveFeedback.put(term, count);
                    }
                }
            } else {
                numNegativeDoc++;
                for (Map.Entry<String, Double> termOcc : docVec.getTermOccMap().entrySet()) {
                    String term = termOcc.getKey();
                    Double count = termOcc.getValue();
                    if (negativeFeedback.containsKey(term)) {
                        negativeFeedback.put(term, negativeFeedback.get(term) + count);
                    } else {
                        negativeFeedback.put(term, count);
                    }
                }
            }
        }

        // Add beta and gamma values to positive and negative feedback vectors.
        double beta = 0.5d;
        for (Map.Entry<String, Double> entry : positiveFeedback.entrySet()) {
            positiveFeedback.put(entry.getKey(), entry.getValue() * (beta / numPositiveDoc));
        }

        double gamma = 0.1d;
        for (Map.Entry<String, Double> entry : negativeFeedback.entrySet()) {
            negativeFeedback.put(entry.getKey(), entry.getValue() * (gamma / numNegativeDoc));
        }

        // add positive and negative feedback to the original query
        for (Map.Entry<String, Double> entry : positiveFeedback.entrySet()) {
            String term = entry.getKey();
            Double wt = entry.getValue();
            if (queryIndex.containsKey(term)) {
                queryIndex.put(term, queryIndex.get(term) + wt);
            } else {
                queryIndex.put(term, wt);
            }
        }

        for (Map.Entry<String, Double> entry : negativeFeedback.entrySet()) {
            String term = entry.getKey();
            Double wt = entry.getValue();
            if (queryIndex.containsKey(term)) {
                double newWt = queryIndex.get(term) - wt;
                if (newWt < 0) {
                    newWt = 0;
                }
                queryIndex.put(term, newWt);
            }
        }

        return generateResults();
    }
}
