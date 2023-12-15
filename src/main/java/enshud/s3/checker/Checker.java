package enshud.s3.checker;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

import enshud.s3.checker.SemanticError;
import enshud.s3.checker.SyntaxError;
import enshud.s3.checker.Binarytree;

public class Checker {

	/**
	 * サンプルmainメソッド．
	 * 単体テストの対象ではないので自由に改変しても良い．
	 */
	List<String> allLines;
	Binarytree tree = new Binarytree();
	Binarytree procedure = new Binarytree();
	Stack<Binarytree> allvariable = new Stack<Binarytree>();
	String[] token;
	int currenttoken;
	public static void main(final String[] args) {
		// normalの確認
		new Checker().run("data/ts/normal14.ts");
		//new Checker().run("data/ts/normal18.ts");

		// synerrの確認
		//new Checker().run("data/ts/synerr08.ts");
		//new Checker().run("data/ts/synerr02.ts");

		// semerrの確認
		//new Checker().run("data/ts/semerr0.ts");
		//new Checker().run("data/ts/semerr07.ts");
	}

	/**
	 * TODO
	 * 
	 * 開発対象となるParser実行メソッド．
	 * 以下の仕様を満たすこと．
	 * 
	 * 仕様:
	 * 第一引数で指定されたtsファイルを読み込み，構文解析を行う．
	 * 構文が正しい場合は標準出力に"OK"を，正しくない場合は"Syntax error: line"という文字列とともに，
	 * 最初のエラーを見つけた行の番号を標準エラーに出力すること （例: "Syntax error: line 1"）．
	 * 入力ファイル内に複数のエラーが含まれる場合は，最初に見つけたエラーのみを出力すること．
	 * 入力ファイルが見つからない場合は標準エラーに"File not found"と出力して終了すること．
	 * 
	 * @param inputFileName 入力tsファイル名
	 */
	public	String searchallscope(String token) {
		String result = "";
		////System.out.println("**** Search all scope -Start- ****");
		Stack<Binarytree> tempstack = new Stack<Binarytree>();
		allvariable.push(tree);
		////System.out.println("stack size ="+allvariable.size());
		while(!allvariable.isEmpty()) {
			tree = allvariable.pop();
			tempstack.push(tree);
			result = tree.search(token);
			if(!Objects.equals(result,"")){
				break;
			}
		}
		while(!tempstack.isEmpty()) {
			tree = tempstack.pop();
			allvariable.push(tree);
		}
		////System.out.println("stack size ="+allvariable.size());
		tree = allvariable.pop();
		////System.out.println("**** Search all scope -End- ****");
		return result;
	}
	public Boolean isidentifier(String[] token) {
		//////System.out.println("=CHECK IDENTIFIER"+"=");
		if(Objects.equals(token[1],"SIDENTIFIER")) {
			return true;
		}
		return false;
	}
	public Boolean isnum(String[] token) {
		//////System.out.println("=CHECK CONSTANT"+"=");
		if(Objects.equals(token[1],"SCONSTANT")) {
			return true;
		}
		return false;
	}
	public String[] gettoken(int currenttoken) {
		//////System.out.println(allLines.get(currenttoken).split("\\s+")[0]);
		String[] temp = allLines.get(currenttoken).split("\\s+");
		String[] realanswer;
		realanswer = new String[4];
		realanswer[0]=temp[0];
		for(int i=1;i<temp.length-3;i++) {
			realanswer[0]+=temp[i];
		}
		realanswer[1]=temp[temp.length-3];
		realanswer[2]=temp[temp.length-2];
		realanswer[3]=temp[temp.length-1];
		return realanswer;
	}
	public void inctoken() throws SyntaxError,SemanticError{
		if(allLines.size()-1>currenttoken) currenttoken+=1;
		token = gettoken(currenttoken);
	}
	public void prog() throws SyntaxError,SemanticError {
		//System.out.println("PROGRAM>"+currenttoken+" "+token[0]+"|line"+token[3]);
		word("program");
		progname();
		word(";");
		block();
		tree = allvariable.pop();
		comp();
		//System.out.println("Errorhere");
		word(".");
	}
	public void word(String w) throws SyntaxError,SemanticError {
		//System.out.println("WORD>"+currenttoken+" CMP '"+w+"' '"+token[0]+"'|line"+token[3]);
		if(!Objects.equals(w,token[0])) throw new SyntaxError(token[3]);
		else inctoken();
	}
	public void progname()throws SyntaxError,SemanticError {
		//System.out.println("PROGRAMNAME>"+currenttoken+" "+token[0]+"|line"+token[3]);
		if(!isidentifier(token)) throw new SyntaxError(token[3]);
		else inctoken();
	}
	public void block() throws SyntaxError,SemanticError{
		//System.out.println("BLOCK>"+currenttoken+" "+token[0]+"|line"+token[3]);
		vardec();
		allvariable.push(tree);
		subdecg();
	}
	public void vardec()throws SyntaxError,SemanticError {
		//System.out.println("VARDEC>"+currenttoken+" "+token[0]+"|line"+token[3]);
		if(Objects.equals("var", token[0])) {
			word("var");
			vardecl();
		}
	}
	public void vardecl() throws SyntaxError,SemanticError{
		//System.out.println("VARDECL>"+currenttoken+" "+token[0]+"|line"+token[3]);
		String[] varname,vartype;
		int starttoken=currenttoken,endtoken;
		varl();
		endtoken = currenttoken-1;
		word(":");
		vartype = token;
		type();
		if(Objects.equals(vartype[0],"array")) vartype[0] = "array/"+gettoken(currenttoken-1)[0];
		for(int i=starttoken;i<=endtoken;i++) {
			varname = gettoken(i);
			if(Objects.equals(",", varname[0])) continue;
			if(!tree.insert(varname[0],vartype[0])) {
				//System.out.println("Error in VARDECL");
				throw new SemanticError(varname[3]);
			}
		}
		word(";");
		vardecl2();
	}
	public void vardecl2() throws SyntaxError,SemanticError{
		//System.out.println("VARDECL2>"+currenttoken+" "+token[0]+"|line"+token[3]);
		String[] varname,vartype;
		int starttoken=currenttoken,endtoken;
		if(!isidentifier(token)) return;
		varl();
		endtoken = currenttoken-1;
		word(":");
		vartype = token;
		type();
		if(Objects.equals(vartype[0],"array")) vartype[0] = "array/"+gettoken(currenttoken-1)[0];
		for(int i=starttoken;i<=endtoken;i++) {
			varname = gettoken(i);
			if(Objects.equals(",", varname[0])) continue;
			if(!tree.insert(varname[0],vartype[0])) {
				//System.out.println("Error in VARDECL2");
				throw new SemanticError(varname[3]);
			}
		}
		word(";");
		vardecl2();
	}
	public void varl() throws SyntaxError,SemanticError{
		//System.out.println("VARL>"+currenttoken+" "+token[0]+"|line"+token[3]);
		varname();
		varl2();
	}
	public void varl2() throws SyntaxError,SemanticError{
		//System.out.println("VARL2>"+currenttoken+" "+token[0]+"|line"+token[3]);
		if(!Objects.equals(",", token[0]))return;
		word(",");
		varname();
		varl2();
	}
	public void varname() throws SyntaxError,SemanticError{
		//System.out.println("VARNAME>"+currenttoken+" "+token[0]+"|line"+token[3]);
		if(!isidentifier(token)) throw new SyntaxError(token[3]);
		else inctoken();
	}
	public void type() throws SyntaxError,SemanticError{
		//System.out.println("TYPE>"+currenttoken+" "+token[0]+"|line"+token[3]);
		if(Objects.equals(token[0],"integer") || Objects.equals(token[0],"char") || Objects.equals(token[0],"boolean")) {
			ntype();
		}
		else if(Objects.equals(token[0],"array") ) {
			atype();
		}
		else throw new SyntaxError(token[3]);
	}
	public void ntype() throws SyntaxError,SemanticError{
		//System.out.println("NTYPE>"+currenttoken+" "+token[0]+"|line"+token[3]);
		if(Objects.equals(token[0],"integer") || Objects.equals(token[0],"char") || Objects.equals(token[0],"boolean")) {
			inctoken();
		}
		else throw new SyntaxError(token[3]);
	}
	public void atype() throws SyntaxError,SemanticError{
		//System.out.println("ATYPE>"+currenttoken+" "+token[0]+"|line"+token[3]);
		word("array");
		word("[");
		minnum();
		word("..");
		maxnum();
		word("]");
		word("of");
		ntype();
	}
	public void minnum()throws SyntaxError,SemanticError {
		//System.out.println("MINNUM>"+currenttoken+" "+token[0]+"|line"+token[3]);
		num();
	}
	public void maxnum()throws SyntaxError,SemanticError {
		//System.out.println("MAXNUM>"+currenttoken+" "+token[0]+"|line"+token[3]);
		num();
	}
	public void num()throws SyntaxError,SemanticError {
		//System.out.println("NUM>"+currenttoken+" "+token[0]+"|line"+token[3]);
		if(Objects.equals("+", token[0])||Objects.equals("-", token[0])) sym();
		if(isnum(token)) inctoken();
		else throw new SyntaxError(token[3]);
	}
	public void sym() throws SyntaxError,SemanticError{
		//System.out.println("SYM>"+currenttoken+" "+token[0]+"|line"+token[3]);
		if(Objects.equals(token[0],"+") || Objects.equals(token[0],"-")) {
			inctoken();
		}
		else throw new SyntaxError(token[3]);
	}
	public void subdecg()throws SyntaxError,SemanticError{
		//System.out.println("SUBDECG>"+currenttoken+" "+token[0]+"|line"+token[3]);
		if(!Objects.equals("procedure", token[0]))return;
		subdec();
		word(";");
		subdecg();
	}
	public void subdec() throws SyntaxError,SemanticError{
		//System.out.println("SUBDEC>"+currenttoken+" "+token[0]+"|line"+token[3]);
		subhead();
		tree = allvariable.pop();
		vardec();
		allvariable.push(tree);
		tree = allvariable.pop();
		comp();
	}
	public void subhead()throws SyntaxError,SemanticError {
		//System.out.println("SUBHEAD>"+currenttoken+" "+token[0]+"|line"+token[3]);
		word("procedure");
		String[] varname = token;
		procname();
		if(procedure.insert(varname[0], "procedure")==false) {
			//System.out.println("Error in SUBHEAD");
			throw new SemanticError(varname[3]);
		}
		tree = new Binarytree();
		tpara();
		allvariable.push(tree);
		word(";");
	}	
	public void procname() throws SyntaxError,SemanticError{
		//System.out.println("PROCNAME>"+currenttoken+" "+token[0]+"|line"+token[3]);
		if(isidentifier(token)) {
			inctoken();
		}
		else throw new SyntaxError(token[3]);
	}
	public void tpara() throws SyntaxError,SemanticError{
		//System.out.println("TPARA>"+currenttoken+" "+token[0]+"|line"+token[3]);
		if(!Objects.equals("(", token[0])) return;
		word("(");
		tparal();
		word(")");
	}
	public void tparal() throws SyntaxError,SemanticError{
		//System.out.println("TPARAL>"+currenttoken+" "+token[0]+"|line"+token[3]);
		String[] varname,vartype;
		int starttoken=currenttoken,endtoken;
		tparanamel();
		endtoken = currenttoken-1;
		word(":");
		vartype = token;
		ntype();
		for(int i=starttoken;i<=endtoken;i++) {
			varname = gettoken(i);
			if(Objects.equals(",", varname[0])) continue;
			if(!tree.insert(varname[0],vartype[0])) {
				//System.out.println("Error in TPARAL");
				throw new SemanticError(varname[3]);
			}
		}
		tparal2();
	}
	public void tparal2()throws SyntaxError,SemanticError {
		//System.out.println("TPARAL2>"+currenttoken+" "+token[0]+"|line"+token[3]);
		if(!Objects.equals(";", token[0])) return;
		String[] varname,vartype;
		int starttoken=currenttoken,endtoken;
		word(";");
		tparanamel();
		endtoken = currenttoken-1;
		word(":");
		vartype = token;
		ntype();
		for(int i=starttoken;i<=endtoken;i++) {
			varname = gettoken(i);
			if(Objects.equals(",", varname[0])) continue;
			if(!tree.insert(varname[0],vartype[0])) {
				//System.out.println("Error in TPARAL2");
				throw new SemanticError(varname[3]);
			}
		}
		tparal2();
	}
	public void tparanamel() throws SyntaxError,SemanticError{
		//System.out.println("TPARANAMEL>"+currenttoken+" "+token[0]+"|line"+token[3]);
		tparaname();
		tparanamel2();
	}
	public void tparanamel2() throws SyntaxError,SemanticError{
		//System.out.println("TPARANAMEL2>"+currenttoken+" "+token[0]+"|line"+token[3]);
		if(!Objects.equals(",", token[0])) return;
		word(",");
		tparaname();
		tparanamel2();
	}
	public void tparaname() throws SyntaxError,SemanticError{
		//System.out.println("TPARANAME>"+currenttoken+" "+token[0]+"|line"+token[3]);
		if(isidentifier(token)) {
			inctoken();
		}
		else throw new SyntaxError(token[3]);
	}
	public void comp() throws SyntaxError,SemanticError{
		//System.out.println("COMP>"+currenttoken+" "+token[0]+"|line"+token[3]);
		word("begin");
		statementg();
		word("end");
	}
	public void statementg() throws SyntaxError,SemanticError{
		//System.out.println("STATEMENTG>"+currenttoken+" "+token[0]+"|line"+token[3]);
		statement();
		word(";");
		statementg2();
	}
	public void statementg2() throws SyntaxError,SemanticError{
		//System.out.println("STATEMENTG2>"+currenttoken+" "+token[0]+"|line"+token[3]);
		if(Objects.equals(token[1],"SIF") || Objects.equals(token[1],"SIDENTIFIER") 
			|| Objects.equals(token[1],"SWHILE") || Objects.equals(token[1],"SWRITELN")
			|| Objects.equals(token[1],"SREADLN") ||Objects.equals(token[1],"SBEGIN"))
		{
				statement();
		}
		else return;
		word(";");
		statementg2();
	}
	public void statement() throws SyntaxError,SemanticError{
		//System.out.println("STATEMENT>"+currenttoken+" "+token[0]+"|line"+token[3]);
		if(Objects.equals(token[0],"if")) ifst();
		else if(Objects.equals(token[0],"while")) {
			whilest();
		}
		else fundast();
	}
	public void ifst() throws SyntaxError,SemanticError{
		//System.out.println("IFST>"+currenttoken+" "+token[0]+"|line"+token[3]);
		word("if");
		String type1 = equation();
		//System.out.println("IFST type:"+type1);
		if(!Objects.equals(type1, "boolean")) throw new SemanticError(gettoken(currenttoken-1)[3]);
		word("then");
		comp();
		elsest();
	}
	public void elsest() throws SyntaxError,SemanticError{
		//System.out.println("ELSEST>"+currenttoken+" "+token[0]+"|line"+token[3]);
		if(!Objects.equals("else", token[0])) return;
		word("else");
		comp();
	}
	public void whilest() throws SyntaxError,SemanticError{
		//System.out.println("WHILEST>"+currenttoken+" "+token[0]+"|line"+token[3]);
		word("while");
		equation();
		word("do");
		comp();
	}
	public void fundast() throws SyntaxError,SemanticError{
		//System.out.println("FUNDAST>"+currenttoken+" "+token[0]+"|line"+token[3]);
		if(Objects.equals(token[0],"readln")||Objects.equals(token[0],"writeln")) {
			inout();
		}
		else if(Objects.equals(token[0],"begin")) {
			comp();
		}
		else if(isidentifier(token)) {
			String[] tokenp1 = gettoken(currenttoken+1);
			//////System.out.print(")REPLACE or PROCEDURECALL"+currenttoken+"=");
			if(Objects.equals(tokenp1[0],":=") || Objects.equals(tokenp1[0],"[")) {
				if(Objects.equals(searchallscope(token[0]),"")) {
					//System.out.println("Error in FUNDAST");
					throw new SemanticError(token[3]);
				}
				replace();
			}
			else if(Objects.equals(tokenp1[0],"(")|| Objects.equals(tokenp1[0],";")){
				if(Objects.equals(procedure.search(token[0]),""))  {
					//System.out.println("Error in FUNDAST2");
					throw new SemanticError(token[3]);
				}
				procedurecall();
			}
			else throw new SyntaxError(token[3]);
		}
		else throw new SyntaxError(token[3]);
	}
	public void replace()throws SyntaxError,SemanticError {
		//System.out.println("REPLACE>"+currenttoken+" "+token[0]+"|line"+token[3]);
		String type1 = leftside();
		word(":=");
		String type2 = equation();
		if(!Objects.equals(type1, type2)) throw new SemanticError(gettoken(currenttoken-1)[3]);
	}
	public String leftside()throws SyntaxError,SemanticError {
		//System.out.println("LEFTSIDE>"+currenttoken+" "+token[0]+"|line"+token[3]);
		String type1 = variable();
		return type1;
	}
	public String variable()throws SyntaxError,SemanticError {
		//System.out.println("VARIABLE>"+currenttoken+" "+token[0]+"|line"+token[3]);
		String[] varname = token;
		String type1 = searchallscope(token[0]);
		if(Objects.equals(type1,"")) {
			//System.out.println("Error in VARIABLE");
			throw new SemanticError(token[3]);
		}
		varname();
		if(!Objects.equals("[", token[0])) return type1;
		word("[");
		String type2 = index();
		word("]");
		//System.out.println("VARIABLE"+type1+""+type2);
		if(!Objects.equals("integer", type2)) throw new SemanticError(gettoken(currenttoken-1)[3]);
		return type1.substring(6);
	}
	public String index() throws SyntaxError,SemanticError{
		//System.out.println("INDEX>"+currenttoken+" "+token[0]+"|line"+token[3]);
		String type1 = equation();
		return type1;
	}
	public void procedurecall()throws SyntaxError,SemanticError {
		//System.out.println("PROCEDURECALL>"+currenttoken+" "+token[0]+"|line"+token[3]);
		inctoken();
		if(!Objects.equals("(", token[0]))return;
		word("(");
		equationl();
		word(")");
	}
	public void equationl() throws SyntaxError,SemanticError{
		//System.out.println("EQUATIONL>"+currenttoken+" "+token[0]+"|line"+token[3]);
		equation();
		equationl2();
	}
	public void equationl2() throws SyntaxError,SemanticError{
		//System.out.println("EQUATIONL2>"+currenttoken+" "+token[0]+"|line"+token[3]);
		if(!Objects.equals(",", token[0]))return;
		word(",");
		equation();
		equationl2();
	}
	public String equation() throws SyntaxError,SemanticError{
		//System.out.println("EQUATION>"+currenttoken+" "+token[0]+"|line"+token[3]);
		String type1 = pureequation();
		if(!Objects.equals("=", token[0]) && !Objects.equals("<>", token[0])&&!Objects.equals("<", token[0])&&
				!Objects.equals(">", token[0]) && !Objects.equals("<=", token[0])&&!Objects.equals(">=", token[0])) return type1;
		relaop();
		String type2 = pureequation();
		//System.out.println(type1+" "+type2);
		if(!Objects.equals(type1, type2)) throw new SemanticError(gettoken(currenttoken-1)[3]);
		else return "boolean";
	}
	public String pureequation() throws SyntaxError,SemanticError{
		//System.out.println("PUREEQUATION>"+currenttoken+" "+token[0]+"|line"+token[3]);
		if(Objects.equals(token[0], "+") || Objects.equals(token[0], "-")) sym();
		String type1 = term();
		String type2 = pureequation2();
		//System.out.println("PUREEQUATION "+type1+" "+type2);
		if(Objects.equals(type2, "Null")) return type1;
		else if(!Objects.equals(type1, type2)) throw new SemanticError(gettoken(currenttoken)[3]);
		else return type1;
	}
	public String pureequation2() throws SyntaxError,SemanticError{
		//System.out.println("PUREEQUATION2>"+currenttoken+" "+token[0]+"|line"+token[3]);
		if(!Objects.equals(token[0], "+") && !Objects.equals(token[0], "-") && !Objects.equals(token[0], "or")) return "Null";
		addop();
		String type1 = term();
		String type2 = pureequation2();
		//System.out.println("PUREEQUATION2 "+type1+" "+type2);
		if(Objects.equals(type2, "Null")) return type1;
		else if(Objects.equals(type1, type2)) return type1;
		else throw new SemanticError(gettoken(currenttoken-1)[3]);
	}
	public String term() throws SyntaxError,SemanticError{
		//System.out.println("TERM>"+currenttoken+" "+token[0]+"|line"+token[3]);
		String type1 = factor();
		String type2 = term2();
		//System.out.println(type1+" "+type2);
		if(Objects.equals(type2, "Null")) return type1;
		else if(Objects.equals(type1, type2)) return type1;
		else throw new SemanticError(gettoken(currenttoken-1)[3]);
	}
	public String term2() throws SyntaxError,SemanticError{
		//System.out.println("TERM2>"+currenttoken+" "+token[0]+"|line"+token[3]);
		if(!Objects.equals(token[0], "*") && !Objects.equals(token[0], "/") && !Objects.equals(token[0], "div")
				&& !Objects.equals(token[0], "mod") && !Objects.equals(token[0], "and")) return "Null";
		multop();
		String type1 = factor();
		String type2 = term2();
		//System.out.println(type1+" "+type2);
		if(Objects.equals(type2, "Null")) return type1;
		else if(Objects.equals(type1, type2)) return type1;
		else throw new SemanticError(gettoken(currenttoken-1)[3]);
	}
	public String factor() throws SyntaxError,SemanticError{
		//System.out.println("FACTOR>"+currenttoken+" "+token[0]+"|line"+token[3]);
		if(Objects.equals(token[0],"false") ||Objects.equals(token[0],"true"))
		{
			inctoken();
			return "boolean";
		}
		else if(Objects.equals(token[1],"SCONSTANT"))
		{
			inctoken();
			return "integer";
		}
		else if(Objects.equals(token[1],"SSTRING"))
		{
			inctoken();
			return "char";
		}
		else if(Objects.equals(token[0],"not")) {
			//////System.out.println("*not factor*");
			inctoken();
			return factor();
		}
		else if(Objects.equals(token[0],"(")) {
			//////System.out.println("*(shiki)*");
			word("(");
			String type1 = equation();
			word(")");
			return type1;
		}
		else if(Objects.equals(token[1],"SIDENTIFIER")) {
			//////System.out.println("*hensuu*");
			////System.out.println(token[0]+" "+currenttoken);
			return variable();
		}
		else throw new SyntaxError(token[3]);
	}
	public void relaop() throws SyntaxError,SemanticError{
		//System.out.println("RELAOP>"+currenttoken+" "+token[0]+"|line"+token[3]);
		if(Objects.equals(token[0],"=")||Objects.equals(token[0],"<>")||Objects.equals(token[0],"<")||Objects.equals(token[0],"<=")||Objects.equals(token[0],">")||Objects.equals(token[0],">=")) {
			inctoken();
		}
		else throw new SyntaxError(token[3]);
	}
	public void addop() throws SyntaxError,SemanticError{
		//System.out.println("ADDOP>"+currenttoken+" "+token[0]+"|line"+token[3]);
		if(Objects.equals(token[0],"+")||Objects.equals(token[0],"-")||Objects.equals(token[0],"or")) {
			inctoken();
		}
		else throw new SyntaxError(token[3]);
	}
	public void multop() throws SyntaxError,SemanticError{
		//System.out.println("MULTOP>"+currenttoken+" "+token[0]+"|line"+token[3]);
		if(Objects.equals(token[0],"*")||Objects.equals(token[0],"/")||Objects.equals(token[0],"div")||Objects.equals(token[0],"mod")||Objects.equals(token[0],"and")) {
			inctoken();
		}
		else throw new SyntaxError(token[3]);
	}
	public void inout() throws SyntaxError,SemanticError{
		//System.out.println("INOUT>"+currenttoken+" "+token[0]+"|line"+token[3]);
		if(Objects.equals(token[0],"readln")) {
			inctoken();
			if(!Objects.equals("(", token[0])) return;
			word("(");
			variablel();
			word(")");
		}
		else if(Objects.equals(token[0],"writeln")) {
			inctoken();
			if(!Objects.equals("(", token[0])) return;
			word("(");
			equationl();
			word(")");
		}
		else throw new SyntaxError(token[3]);
	}
	public void variablel() throws SyntaxError,SemanticError{
		//System.out.println("VARIABLEL>"+currenttoken+" "+token[0]+"|line"+token[3]);
		variable();
		variablel2();
	}
	public void variablel2() throws SyntaxError,SemanticError{
		//System.out.println("VARIABLEL2>"+currenttoken+" "+token[0]+"|line"+token[3]);
		if(!Objects.equals(",", token[0])) return;
		word(",");
		variable();
		variablel2();
	}
	public void run(final String inputFileName) {
	    	try {
		    	allLines = Files.readAllLines(Paths.get(inputFileName));
		    	currenttoken = 0;
		    	token = gettoken(currenttoken);
		    	prog();
		    	System.out.println("OK");
		    } catch(SyntaxError e) {
		    	e.print();
		    } catch(SemanticError e) {
		    	e.print();
		    }
		    catch (IOException e) {
		        System.err.println("File not found");
		    } 
	    
	}
}

