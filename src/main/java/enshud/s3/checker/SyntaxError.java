package enshud.s3.checker;
public class SyntaxError extends Exception{
	String errorline;
	public SyntaxError(String errorline) {
		this.errorline = errorline;
	}
	public void print(){
		System.err.println("Syntax error: line "+errorline);
	}
}