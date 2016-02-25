package ir.query;

public class Document {
	private String url;
	private String snippet;
	
	public Document(String url, String snippet){
		this.url = url;
		this.snippet = snippet;
	}

	public String getUrl() {
		return url;
	}

	public String getSnippet() {
		return snippet;
	}
	
	
}
