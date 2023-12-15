package enshud.s1.lexer;
public class Token{
    String code;
	String name;
	int id;
	public Token(String inputcode,String inputname,int inputid) {
		this.id = inputid;
		this.code = inputcode;
		this.name = inputname;
	}
}