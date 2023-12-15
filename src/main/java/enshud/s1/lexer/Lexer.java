package enshud.s1.lexer;
import java.util.ArrayList;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

public class Lexer {

	/**
	 * サンプルmainメソッド．
	 * 単体テストの対象ではないので自由に改変しても良い．
	 */
	public static void main(final String[] args) {
		// normalの確認
		new Lexer().run("data/pas/normal01.pas", "tmp/out1.ts");
		new Lexer().run("data/pas/normal02.pas", "tmp/out2.ts");
		new Lexer().run("data/pas/normal03.pas", "tmp/out3.ts");
		new Lexer().run("data/pas/pp.pas", "tmp/ppout.ts");
	}	
	/**
	 * TODO
	 * 
	 * 開発対象となるLexer実行メソッド．
	 * 以下の仕様を満たすこと．
	 * 
	 * 仕様:
	 * 第一引数で指定されたpasファイルを読み込み，トークン列に分割する．
	 * トークン列は第二引数で指定されたtsファイルに書き出すこと．
	 * 正常に処理が終了した場合は標準出力に"OK"を，
	 * 入力ファイルが見つからない場合は標準エラーに"File not found"と出力して終了すること．
	 * 
	 * @param inputFileName 入力pasファイル名
	 * @param outputFileName 出力tsファイル名
	 */
	public void run(final String inputFileName, final String outputFileName) {
		// TODO
		  int programstatus = 0; // 0=normal 1=comment ongoing -1= no ending "
	      try {
	    	  FileWriter myWriter = new FileWriter(outputFileName);
	    	    List<String> allLines = Files.readAllLines(Paths.get(inputFileName));
	    	    int linenumber = 1;
	    	    
				for (String line : allLines) {
					if(programstatus ==1) {
						line = '{'+line;
					}
					programstatus = processline(line,linenumber,myWriter);
					if(programstatus==-1) break;
					linenumber++;
				}
				myWriter.close();
				if(programstatus==0) System.out.println("OK");
				else {
					if(programstatus==1) System.out.println("Program Error: No ending } ");
					if(programstatus==-1) System.out.println("Program Error: No ending ' ");
				}
	      } catch (IOException e) {
	          System.err.println("File not found");
	      } 
	}
	public int processline(String line, int linenumber,FileWriter myWriter) {
		int currentletter = 0;
		while(currentletter<line.length()) {
			if (Character.isDigit(line.charAt(currentletter))) {
				currentletter = dodigit(currentletter,line,linenumber,myWriter);
			}
			else if (Character.isLetter(line.charAt(currentletter))) {
				currentletter = doidentifier(currentletter,line,linenumber,myWriter);
			}
			else if(Objects.equals(line.charAt(currentletter),'\'')) {
				currentletter = dostring(currentletter,line,linenumber,myWriter);
				if(currentletter==-1) return -1;
			}
			else if(Objects.equals(line.charAt(currentletter),'{')) {
				currentletter = docomment(currentletter,line);
				if(currentletter==-1) return 1;
			}
			else if(Objects.equals(line.charAt(currentletter),' ')) {
				currentletter ++;
			}
			else {
				currentletter = dosymbol(currentletter,line,linenumber,myWriter);
			}
		}
		return 0;
	}
	public void outputtofile(String sourcecode,String tokenname,int id,int linenumber,FileWriter myWriter) {
		String newline = sourcecode+"\t"+tokenname+"\t"+id+"\t"+linenumber+"\n";
		try {
			myWriter.write(newline);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public int dodigit(int currentletter, String line,int linenumber,FileWriter myWriter) {
		int nexttokenletter;
		for(nexttokenletter=currentletter;nexttokenletter<line.length()&&Character.isDigit(line.charAt(nexttokenletter));nexttokenletter++);
		outputtofile(line.substring(currentletter,nexttokenletter),"SCONSTANT",44,linenumber,myWriter);
		return nexttokenletter;
	}
	public int doidentifier(int currentletter, String line,int linenumber,FileWriter myWriter) {
		ArrayList<Token> token_identifier = new ArrayList<Token>();
		token_identifier.add(new Token("and","SAND",0));
		token_identifier.add(new Token("array","SARRAY",1));
		token_identifier.add(new Token("begin","SBEGIN",2));
		token_identifier.add(new Token("boolean","SBOOLEAN",3));
		token_identifier.add(new Token("char","SCHAR",4));
		token_identifier.add(new Token("div","SDIVD",5));
		token_identifier.add(new Token("do","SDO",6));
		token_identifier.add(new Token("else","SELSE",7));
		token_identifier.add(new Token("end","SEND",8));
		token_identifier.add(new Token("false","SFALSE",9));
		token_identifier.add(new Token("if","SIF",10));
		token_identifier.add(new Token("integer","SINTEGER",11));
		token_identifier.add(new Token("mod","SMOD",12));
		token_identifier.add(new Token("not","SNOT",13));
		token_identifier.add(new Token("of","SOF",14));
		token_identifier.add(new Token("or","SOR",15));
		token_identifier.add(new Token("procedure","SPROCEDURE",16));
		token_identifier.add(new Token("program","SPROGRAM",17));
		token_identifier.add(new Token("readln","SREADLN",18));
		token_identifier.add(new Token("then","STHEN",19));
		token_identifier.add(new Token("true","STRUE",20));
		token_identifier.add(new Token("var","SVAR",21));
		token_identifier.add(new Token("while","SWHILE",22));
		token_identifier.add(new Token("writeln","SWRITELN",23));
		int i;
		for(i=currentletter;i<line.length()&&(Character.isLetter(line.charAt(i))||Character.isDigit(line.charAt(i)));i++);
		for (Token k : token_identifier) {
			if(Objects.equals(line.substring(currentletter,i),k.code)) {
				outputtofile(line.substring(currentletter,i),k.name,k.id,linenumber,myWriter);
				return i;
			}
		}
		outputtofile(line.substring(currentletter,i),"SIDENTIFIER",43,linenumber,myWriter);
		return i;
	}
	public int dostring(int currentletter, String line,int linenumber,FileWriter myWriter) {
		int i;
		for(i=currentletter+1;i<line.length()&&!Objects.equals(line.charAt(i),'\'');i++);
		if(i==line.length()) return -1;
		i++;
		outputtofile(line.substring(currentletter,i),"SSTRING",45,linenumber,myWriter);
		return i;
	}
	public int docomment(int currentletter, String line) {
		int i;
		for(i=currentletter+1;i<line.length()&&!Objects.equals(line.charAt(i),'}');i++);
		if(i==line.length()) return -1;
		i++;
		return i;
	}
	public int dosymbol(int currentletter, String line,int linenumber,FileWriter myWriter) {
		ArrayList<Token> singlesymbol = new ArrayList<Token>();
		ArrayList<Token> doublesymbol = new ArrayList<Token>();
		singlesymbol.add(new Token("=","SEQUAL",24));
		doublesymbol.add(new Token("<>","SNOTEQUAL",25));
		singlesymbol.add(new Token("<","SLESS",26));
		doublesymbol.add(new Token("<=","SLESSEQUAL",27));
		doublesymbol.add(new Token(">=","SGREATEQUAL",28));
		singlesymbol.add(new Token(">","SGREAT",29));
		singlesymbol.add(new Token("+","SPLUS",30));
		singlesymbol.add(new Token("-","SMINUS",31));
		singlesymbol.add(new Token("*","SSTAR",32));
		singlesymbol.add(new Token("(","SLPAREN",33));
		singlesymbol.add(new Token(")","SRPAREN",34));
		singlesymbol.add(new Token("[","SLBRACKET",35));
		singlesymbol.add(new Token("]","SRBRACKET",36));
		singlesymbol.add(new Token(";","SSEMICOLON",37));
		singlesymbol.add(new Token(":","SCOLON",38));
		doublesymbol.add(new Token("..","SRANGE",39));
		doublesymbol.add(new Token(":=","SASSIGN",40));
		singlesymbol.add(new Token(",","SCOMMA",41));
		singlesymbol.add(new Token(".","SDOT",42));
		singlesymbol.add(new Token("/","SDIVD",5));
		int i;
		for(i=currentletter;i<line.length()&&!Character.isDigit(line.charAt(i))&&!Character.isLetter(line.charAt(i))&&!Objects.equals(line.charAt(i),' ')&&!Objects.equals(line.charAt(i),'\'');i++);
		int j=currentletter;
		while(j<i) {
			int found = 0;
			if(j+1<i) {
				for (Token k : doublesymbol) {
					if(Objects.equals(line.substring(j,j+2),k.code)) {
						outputtofile(line.substring(j,j+2),k.name,k.id,linenumber,myWriter);
						j+=2;
						found=1;
						break;
					}
				}
			}
			if(found==0) {
				for (Token k : singlesymbol) {
					if(Objects.equals(line.substring(j,j+1),k.code)) {
						outputtofile(line.substring(j,j+1),k.name,k.id,linenumber,myWriter);
						j++;
						found=1;
						break;
					}
				}
			}
			if(found==0) j++;
		}
		return i;
	}
}
