package ir.query;

public class Document implements Comparable<Document>{
	private String url;
	private String snippet;
	private double score;
	
	public Document(double score, String url, String snippet){
		this.score = score;
		this.url = url;
		this.snippet = snippet;
	}

	public String getUrl() {
		return url;
	}

	public String getSnippet() {
		return snippet;
	}
	
	public double getScore(){
		return score;
	}

	@Override
	public int compareTo(Document d) {
		// TODO Auto-generated method stub
		return (this.score>d.score)?-1:(this.score<d.score)?1:0;
	}
	
	
}
