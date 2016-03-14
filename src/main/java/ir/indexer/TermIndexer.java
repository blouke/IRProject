package ir.indexer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
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

/**
 * This class will do the main functionality of indexing the documents.
 *
 * @author
 */
public class TermIndexer implements Serializable {

    private static final long serialVersionUID = 1L;
    public HashMap<Integer, DocInfo> docInfoList;
    public HashMap<String, TokenInfo> dictionary;
    public HashMap<Integer, TermOccurrence> docVectors;
    private int docId = 0;
    private transient InputStream inputStream;

    public TermIndexer(InputStream inputStream) {
        this.inputStream = inputStream;
        docInfoList = new HashMap<Integer, DocInfo>();
        dictionary = new HashMap<String, TokenInfo>();
        docVectors = new HashMap<Integer, TermOccurrence>();
    }

    /**
     * This method will create term vectors.Then will calculate term IDF
     * and Document length.
     */
    public void initialize() {
        try {
            PatternTokenizer tokenStream = new PatternTokenizer(Pattern.compile("(?s)(?<=URL::\\s).*?(?=Recno::)"), 0);
            tokenStream.setReader(new InputStreamReader(inputStream));
            tokenStream.reset();
            CharTermAttribute token = tokenStream.addAttribute(CharTermAttribute.class);
            String[] tokenString;
            String content;
            String url;
            while (tokenStream.incrementToken()) {
                tokenString = token.toString().split("\\R", 4);
                url = tokenString[0];
                url = url.replace("http://", "");
                content = tokenString[3];
                content = content.trim();
                String snippet = (content.length() > 100 ? content.substring(0, 100) + "..." : content.substring(0));
                if (content.length() > 0) {
                    docInfoList.put(docId, new DocInfo(docId, url, snippet));
                    // index each document
                    createIndex(content, docId);
                    docId += 1;
                }
            }
            tokenStream.end();
            tokenStream.close();

            // calculating IDF
            for (Map.Entry<String, TokenInfo> entry : dictionary.entrySet()) {
                TokenInfo tokenInfo = entry.getValue();
                tokenInfo.calculateIdf(docId);

            }

            // calculate document length 
            for (Map.Entry<String, TokenInfo> dictionaryEntry : dictionary.entrySet()) {

                double idf = dictionaryEntry.getValue().getIdf();
                for (Map.Entry<Integer, TokenOccurrence> tokenOccEntry : dictionaryEntry.getValue().getOccMap().entrySet()) {
                    int tokenCount = tokenOccEntry.getValue().getCount();
                    DocInfo docInfo = docInfoList.get(tokenOccEntry.getKey());
                    double length = docInfo.getLength() + Math.pow(tokenCount * idf, 2);
                    docInfo.setLength(length);
                }
            }

            for (Map.Entry<Integer, DocInfo> doc : docInfoList.entrySet()) {
                double newLength = Math.sqrt(doc.getValue().getLength());
                doc.getValue().setLength(newLength);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Utility method called by initialize() to do stop word removal, stemming and term index for each document.
     */
    public void createIndex(String content, int docId) {
        try {
            StandardTokenizer stream = new StandardTokenizer();
            stream.setReader(new StringReader(content));
            TokenStream tokenStream = new StopFilter(stream, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
            tokenStream = new PorterStemFilter(tokenStream);
            tokenStream.reset();
            CharTermAttribute token = tokenStream.addAttribute(CharTermAttribute.class);
            while (tokenStream.incrementToken()) {
                String term = token.toString();
                TokenInfo tokenInfo = dictionary.get(term);
                if (tokenInfo != null) {
                    tokenInfo.addTokenOccurrence(docId, 1);
                } else {
                    TokenInfo newTokenInfo = new TokenInfo();
                    newTokenInfo.addTokenOccurrence(docId, 1);
                    dictionary.put(term, newTokenInfo);
                }

                // update DocVectors
                if (docVectors.containsKey(docId)) {
                    docVectors.get(docId).addTermOcc(term);
                } else {
                    docVectors.put(docId, new TermOccurrence(term));
                }
            }
            tokenStream.end();
            tokenStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
