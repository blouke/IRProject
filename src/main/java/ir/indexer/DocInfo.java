package ir.indexer;

public class DocInfo {
	
	private int id;
	private String url;
	private double length;
	
	public DocInfo(int id, String url) {
		this.id = id;
		this.url = url;
		length = 0.0;
	}
	
	public void setLength(double length){
		this.length = length;
	}
	
	public double getLength(){
		return length;
	}
	
	public String getUrl(){
		return url;
	}
}