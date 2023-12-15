package enshud.s3.checker;
public class SemanticError extends Exception{
	String errorline;
	public SemanticError(String errorline) {
		this.errorline = errorline;
	}
	public void print(){
		System.err.println("Semantic error: line "+errorline);
	}
}