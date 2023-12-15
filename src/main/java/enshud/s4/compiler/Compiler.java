package enshud.s4.compiler;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.SystemMenuBar;

import java.io.File;
import enshud.s4.compiler.SemanticError;
import enshud.s4.compiler.SyntaxError;
import enshud.s4.compiler.Binarytree.Node;
import enshud.casl.CaslSimulator;
import enshud.s4.compiler.Binarytree;

public class Compiler {

	/**
	 * サンプルmainメソッド．
	 * 単体テストの対象ではないので自由に改変しても良い．
	 */
	FileWriter myWriter;
	List<String> allLines;
	int lastmemoryused = -1;
	Binarytree tree = new Binarytree();
	Binarytree procedure = new Binarytree();
	Stack<Binarytree> allvariable = new Stack<Binarytree>();
	Binarytree treeprint = new Binarytree();
	List<String> constant =new ArrayList<>();
	String[] token;
	List<String> rpleftcommand = new ArrayList<>();
	List<String> subdeccommand = new ArrayList<>();
	int currenttoken;
	int countproc =0;
	int countequation = 0;
	int countifelse = 0,countloop=0;
	Stack<Integer> ifstack = new Stack<Integer>();
	Stack<Integer> loopstack = new Stack<Integer>();
	//check last command
	boolean iswriteln=false,isreadln=false,isreplace=false,
			issubdecg=false,isreplaceleft=false;
	public static void main(final String[] args) {
		// Compilerを実行してcasを生成する
		new Compiler().run("data/ts/normal10.ts", "tmp/out.cas");
		//new Compiler().run("data/ts/semerr08.ts", "tmp/out2.cas");
		// 上記casを，CASLアセンブラ & COMETシミュレータで実行する
		//CaslSimulator.appendLibcas("tmp/out1.cas");
		CaslSimulator.run("tmp/out.cas", "tmp/out04_done.ans");
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
	public	Node searchallscope(String token) {
		Node result=null;
		////System.out.println("**** Search all scope -Start- ****");
		Stack<Binarytree> tempstack = new Stack<Binarytree>();
		allvariable.push(tree);
		////System.out.println("stack size ="+allvariable.size());
		while(!allvariable.isEmpty()) {
			tree = allvariable.pop();
			tempstack.push(tree);
			result = tree.search(token);
			if(!Objects.equals(result,null)){
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
		//String[] temp = allLines.get(currenttoken).split("\\s+");
		String[] temp = allLines.get(currenttoken).split("\t");
		return temp;
	}
	public void inctoken() throws SyntaxError,SemanticError{
		if(allLines.size()-1>currenttoken) currenttoken+=1;
		token = gettoken(currenttoken);
	}
	public void prog() throws SyntaxError,SemanticError {
		//System.out.println("PROGRAM>"+currenttoken+" "+token[0]+"|line"+token[3]);
		write("CASL\tSTART\tBEGIN\n");
    	write("BEGIN\tLAD\tGR6,\t0\t;\n");
    	write("\tLAD\tGR7,\tLIBBUF\t;\n");
		word("program");
		progname();
		word(";");
		block();
		tree = allvariable.pop();
		comp();
		word(".");
    	write("\tRET\t\t\t;\n");
    	printsubdec() ;
    	//treeprint.printorder();
    	printvariable(treeprint.root);
    	printconstant();
    	write("LIBBUF\tDS\t256\t\t;\n");
    	write("\tEND\t\t\t;\n");
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
		issubdecg=true;
		subdecg();
		issubdecg=false;
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
		int[] typedata = type(); // size and array's first location
		if(Objects.equals(vartype[0],"array")) vartype[0] = "array/"+gettoken(currenttoken-1)[0];
		for(int i=starttoken;i<=endtoken;i++) {
			varname = gettoken(i);
			if(Objects.equals(",", varname[0])) continue;
			if(!tree.insert(varname[0],vartype[0],typedata[0],lastmemoryused+1,typedata[1])) {
				//System.out.println("Error in VARDECL");
				throw new SemanticError(varname[3]);
			}else {
				lastmemoryused +=typedata[0];
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
		int[] typedata = type();
		if(Objects.equals(vartype[0],"array")) vartype[0] = "array/"+gettoken(currenttoken-1)[0];
		for(int i=starttoken;i<=endtoken;i++) {
			varname = gettoken(i);
			if(Objects.equals(",", varname[0])) continue;
			if(!tree.insert(varname[0],vartype[0],typedata[0],lastmemoryused+1,typedata[1])) {
				//System.out.println("Error in VARDECL2");
				throw new SemanticError(varname[3]);
			}else {
				lastmemoryused+=typedata[0];
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
	public int[] type() throws SyntaxError,SemanticError{
		//System.out.println("TYPE>"+currenttoken+" "+token[0]+"|line"+token[3]);
		if(Objects.equals(token[0],"integer") || Objects.equals(token[0],"char")  || Objects.equals(token[0],"boolean")) {
			return new int[]{ ntype(),-1 };
		}
		else if(Objects.equals(token[0],"array") ) {
			return atype();
		}
		else throw new SyntaxError(token[3]);
	}
	public int ntype() throws SyntaxError,SemanticError{
		//System.out.println("NTYPE>"+currenttoken+" "+token[0]+"|line"+token[3]);
		if(Objects.equals(token[0],"char")  ||Objects.equals(token[0],"integer") || Objects.equals(token[0],"boolean")) {
			inctoken();
			return 1;
		}
		else throw new SyntaxError(token[3]);
	}
	public int[] atype() throws SyntaxError,SemanticError{
		//System.out.println("ATYPE>"+currenttoken+" "+token[0]+"|line"+token[3]);
		word("array");
		word("[");
		int minnum1 = minnum();
		word("..");
		int maxnum1 = maxnum();
		word("]");
		word("of");
		int size = ntype();
		return new int[]{size*(maxnum1-minnum1+1),minnum1};
	}
	public int minnum()throws SyntaxError,SemanticError {
		//System.out.println("MINNUM>"+currenttoken+" "+token[0]+"|line"+token[3]);
		return num();
	}
	public int maxnum()throws SyntaxError,SemanticError {
		//System.out.println("MAXNUM>"+currenttoken+" "+token[0]+"|line"+token[3]);
		return num();
	}
	public int num()throws SyntaxError,SemanticError {
		//System.out.println("NUM>"+currenttoken+" "+token[0]+"|line"+token[3]);
		int num = 1;
		if(Objects.equals("-", token[0])){
			num = -1;
		}
		boolean issym = false;
		if(Objects.equals("-", token[0])) {
			issym = true;
		}
		if(Objects.equals("+", token[0])||Objects.equals("-", token[0])) sym();
		if(isnum(token)) {
			num = num*Integer.parseInt(token[0]);
			if(issym==true) {
				write(String.format("\tPOP\tGR2\t\t;LINE:%s\n",token[3]));
				write(String.format("\tLD\tGR1,\t=0\t;LINE:%s\n",token[3]));
				write(String.format("\tSUBA\tGR1,\tGR2\t;LINE:%s\n",token[3]));
				write(String.format("\tPUSH\t0,\tGR1\t;LINE:%s\n",token[3]));	
			}
			issym=false;
			inctoken();
			return num;
		}
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
		write(String.format("\tRET\t\t\t;LINE:%s\n",token[3]));
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
		// name,type(String),size,startingindex,startingnumber
		if(procedure.insert(varname[0], "procedure",0,countproc,-1)==false) {
			//System.out.println("Error in SUBHEAD");
			throw new SemanticError(varname[3]);
		}
		write(String.format("PROC%d\tNOP\t\t\t;LINE:%s\n",countproc,token[3]));
		countproc+=1;
		int lastmemoryused1 = lastmemoryused; 
		tree = new Binarytree();
		tpara();
		allvariable.push(tree);
		int tparameter = tree.count();
		//set for everyone
		write(String.format("\tLD\tGR1,\tGR8\t;LINE:%s\n",token[3]));
		write(String.format("\tADDA\tGR1,\t=%d\t;LINE:%s\n",tparameter,token[3]));
		//loop for each variable
		for(int i=1;i<=tparameter;i++) {
			write(String.format("\tLD\tGR2,\t0,GR1\t;NOT SURE LINE:%s\n",token[3]));
			write(String.format("\tLD\tGR3,\t=%d\t;NOT SURE LINE:%s\n",lastmemoryused1+i,token[3]));
			write(String.format("\tST\tGR2,\tVAR,GR3\t;NOT SURE LINE:%s\n",token[3]));
			write(String.format("\tSUBA\tGR1,\t=1\t;NOT SURE LINE:%s\n",token[3]));
		}
		write(String.format("\tLD\tGR1,\t0,GR8\t;NOT SURE LINE:%s\n",token[3]));
		write(String.format("\tADDA\tGR8,\t=%d\t;NOT SURE LINE:%s\n",tparameter,token[3]));
		write(String.format("\tST\tGR1,\t0,GR8\t;NOT SURE LINE:%s\n",token[3]));
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
		int size = ntype();
		for(int i=starttoken;i<=endtoken;i++) {
			varname = gettoken(i);
			if(Objects.equals(",", varname[0])) continue;
			if(!tree.insert(varname[0],vartype[0],size,lastmemoryused+1,-1)) {
				//System.out.println("Error in TPARAL");
				throw new SemanticError(varname[3]);
			}else {
				lastmemoryused+=size;
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
		int size = ntype();
		for(int i=starttoken;i<=endtoken;i++) {
			varname = gettoken(i);
			if(Objects.equals(",", varname[0])) continue;
			if(!tree.insert(varname[0],vartype[0],size,lastmemoryused+1,-1)) {
				//System.out.println("Error in TPARAL2");
				throw new SemanticError(varname[3]);
			}else {
				System.out.println((lastmemoryused+1)+"TPARAL Add "+varname[0]+" "+size+"\n");
				lastmemoryused+=size;
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
		ifstack.push(countifelse);
		countifelse+=1;
		write(String.format("\tPOP\tGR1\t\t;LINE:%s\n",token[3]));
		write(String.format("\tCPA\tGR1,\t=#FFFF\t;LINE:%s\n",token[3]));
		write(String.format("\tJZE\tELSE%d\t\t;LINE:%s\n",ifstack.peek(),token[3]));
		if(!Objects.equals(type1, "boolean")) throw new SemanticError(gettoken(currenttoken-1)[3]);
		word("then");
		comp();
		if(Objects.equals("else", token[0])) {
			write(String.format("\tJUMP\tENDIF%d\t\t;LINE:%s\n",ifstack.peek(),token[3]));
			write(String.format("ELSE%d\tNOP\t\t\t;LINE:%s\n",ifstack.peek(),token[3]));
			elsest();
			write(String.format("ENDIF%d\tNOP\t\t\t;LINE:%s\n",ifstack.peek(),token[3]));
		}
		else {
			write(String.format("ELSE%d\tNOP\t\t\t;LINE:%s\n",ifstack.peek(),token[3]));
		}
		ifstack.pop();
	}
	public void elsest() throws SyntaxError,SemanticError{
		//System.out.println("ELSEST>"+currenttoken+" "+token[0]+"|line"+token[3]);
		word("else");
		comp();
	}
	public void whilest() throws SyntaxError,SemanticError{
		//System.out.println("WHILEST>"+currenttoken+" "+token[0]+"|line"+token[3]);
		write(String.format("LOOP%d\tNOP\t\t\t;LINE:%s\n",countloop,token[3]));
		word("while");
		equation();
		loopstack.push(countloop);
		countloop+=1;
		write(String.format("\tPOP\tGR1\t\t;LINE:%s\n",token[3]));
		write(String.format("\tCPL\tGR1,\t=#FFFF\t;LINE:%s\n",token[3]));
		write(String.format("\tJZE\tENDLP%d\t\t;LINE:%s\n",loopstack.peek(),token[3]));
		word("do");
		comp();
		write(String.format("\tJUMP\tLOOP%d\t\t;LINE:%s\n",loopstack.peek(),token[3]));
		write(String.format("ENDLP%d\tNOP\t\t\t;LINE:%s\n",loopstack.peek(),token[3]));
		loopstack.pop();
	}
	public void fundast() throws SyntaxError,SemanticError{
		String firsttoken = token[0];
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
			if(Objects.equals(tokenp1[0],"(")|| Objects.equals(tokenp1[0],";")){
				Node procnode = procedure.search(token[0]);
				if(Objects.equals(procnode,null))  {
					throw new SemanticError(token[3]);
				}
				procedurecall();
				write(String.format("\tCALL\tPROC%d\t\t;LINE:%s\n",procnode.startingindex,token[3]));
			}
			else{
				isreplace=true;
				if(Objects.equals(searchallscope(token[0]),null)) {
					//System.out.println("Error in FUNDAST");
					throw new SemanticError(token[3]);
				}
				replace();
			}
			//else throw new SyntaxError(token[3]);
		}
		else throw new SyntaxError(token[3]);
		if(isreplace==true) { // if not a[5]
			Node leftnode = searchallscope(firsttoken);
			if(Objects.equals(leftnode.type,"integer")||Objects.equals(leftnode.type,"boolean")||Objects.equals(leftnode.type,"char") ) {
				write(String.format("\tLD\tGR2,\t=%d\t;LINE:%s\n",leftnode.startingindex,token[3]));
				write(String.format("\tPOP\tGR1\t\t;LINE:%s\n",token[3]));
				write(String.format("\tST\tGR1,\tVAR,GR2\t;LINE:%s\n",token[3]));
			}
			else { 
				printrpleft();
				write(String.format("\tPOP\tGR2\t\t;LINE:%s\n",token[3])); //get number inside []
				write(String.format("\tADDA\tGR2,\t=%d\t;LINE:%s\n",leftnode.startingindex-leftnode.startingnumber,token[3]));
				write(String.format("\tPOP\tGR1\t\t;LINE:%s\n",token[3])); //get the right number
				write(String.format("\tST\tGR1,\tVAR,GR2\t;LINE:%s\n",token[3]));
			}
			isreplace=false;
		}
	}
	public void printrpleft() {
		for(String value: rpleftcommand) {
    		write(value);
    	}
		rpleftcommand = new ArrayList<>();
	}
	public void printsubdec() {
		for(String value: subdeccommand) {
    		write(value);
    	}
		subdeccommand = new ArrayList<>();
	}
	public void replace()throws SyntaxError,SemanticError {
		isreplaceleft = true;
		//System.out.println("REPLACE>"+currenttoken+" "+token[0]+"|line"+token[3]);
		String type1 = leftside();
		isreplaceleft = false;
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
		String type1 = "";
		Node result = searchallscope(token[0]);
		if(!Objects.equals(result,null)) type1=result.type;
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
		String varname = token[0];
		String type = equation();
		if(iswriteln==true) {
			if(Objects.equals(type,"integer") || Objects.equals(type,"boolean")) { //FIXME
				write(String.format("\tPOP\tGR2\t\t;LINE:%s\n",token[3])); // pop number in
				write(String.format("\tCALL\tWRTINT\t\t;LINE:%s\n",token[3]));
			}
			else if(Objects.equals(type,"string")) {
				write(String.format("\tPOP\tGR2\t\t;LINE:%s\n",token[3])); // pop words in
				write(String.format("\tPOP\tGR1\t\t;LINE:%s\n",token[3])); // pop lengths in
				write(String.format("\tCALL\tWRTSTR\t\t;LINE:%s\n",token[3]));
			}
			else if(Objects.equals(type,"char")) {
				write(String.format("\tPOP\tGR2\t\t;LINE:%s\n",token[3])); // pop words
				write(String.format("\tCALL\tWRTCH\t\t;LINE:%s\n",token[3]));
			}
		}
		equationl2();
	}
	public void equationl2() throws SyntaxError,SemanticError{
		//System.out.println("EQUATIONL2>"+currenttoken+" "+token[0]+"|line"+token[3]);
		if(!Objects.equals(",", token[0]))return;
		word(",");
		String type = equation();
		if(iswriteln==true) {
			if(Objects.equals(type,"integer") || Objects.equals(type,"boolean")) {
				write(String.format("\tPOP\tGR2\t\t;LINE:%s\n",token[3])); // pop number in
				write(String.format("\tCALL\tWRTINT\t\t;LINE:%s\n",token[3]));
			}
			else if(Objects.equals(type,"string")) {
				write(String.format("\tPOP\tGR2\t\t;LINE:%s\n",token[3])); // pop words in
				write(String.format("\tPOP\tGR1\t\t;LINE:%s\n",token[3])); // pop lengths in
				write(String.format("\tCALL\tWRTSTR\t\t;LINE:%s\n",token[3]));
			}
			else if(Objects.equals(type,"char")) {
				write(String.format("\tPOP\tGR2\t\t;LINE:%s\n",token[3])); // pop words
				write(String.format("\tCALL\tWRTCH\t\t;LINE:%s\n",token[3]));
			}
		}
		equationl2();
	}
	public String equation() throws SyntaxError,SemanticError{
		//System.out.println("EQUATION>"+currenttoken+" "+token[0]+"|line"+token[3]);
		String type1 = pureequation();
		if(!Objects.equals("=", token[0]) && !Objects.equals("<>", token[0])&&!Objects.equals("<", token[0])&&
				!Objects.equals(">", token[0]) && !Objects.equals("<=", token[0])&&!Objects.equals(">=", token[0])) return type1;
		String relaoperation = token[0];
		relaop();
		String type2 = pureequation();
		write(String.format("\tPOP\tGR2\t\t;LINE:%s\n",token[3]));
		write(String.format("\tPOP\tGR1\t\t;LINE:%s\n",token[3])); 
		write(String.format("\tCPA\tGR1,\tGR2\t;LINE:%s\n", token[3])); //compare 2 relationship
		if(Objects.equals("=",relaoperation)) {
			write(String.format("\tJZE\tTRUE%d\t\t;LINE:%s\n",countequation,token[3]));
		}else if(Objects.equals("<>",relaoperation)) {
			write(String.format("\tJNZ\tTRUE%d\t\t;LINE:%s\n",countequation,token[3]));
		}else if(Objects.equals("<",relaoperation)) {
			write(String.format("\tJMI\tTRUE%d\t\t;LINE:%s\n",countequation,token[3]));
		}else if(Objects.equals(">",relaoperation)) {
			write(String.format("\tJPL\tTRUE%d\t\t;LINE:%s\n",countequation,token[3]));
		}else if(Objects.equals("<=",relaoperation)) {
			write(String.format("\tJPL\tTRUE%d\t\t;LINE:%s\n",countequation,token[3]));
		}else if(Objects.equals(">=",relaoperation)) {
			write(String.format("\tJMI\tTRUE%d\t\t;LINE:%s\n",countequation,token[3]));
		}
		if(Objects.equals("<=",relaoperation) || Objects.equals(">=",relaoperation)) {
			write(String.format("\tLD\tGR1,\t=#0000\t;LINE:%s\n",token[3]));
			write(String.format("\tJUMP\tBOTH%d\t\t;LINE:%s\n",countequation,token[3]));
			write(String.format("TRUE%d\tLD\tGR1,\t=#FFFF\t;LINE:%s\n",countequation,token[3]));
		}else {
			write(String.format("\tLD\tGR1,\t=#FFFF\t;LINE:%s\n",token[3]));
			write(String.format("\tJUMP\tBOTH%d\t\t;LINE:%s\n",countequation,token[3]));
			write(String.format("TRUE%d\tLD\tGR1,\t=#0000\t;LINE:%s\n",countequation,token[3]));
		}
		write(String.format("BOTH%d\tPUSH\t0,\tGR1\t;LINE:%s\n",countequation,token[3]));
		countequation+=1;
		if(!Objects.equals(type1, type2)) throw new SemanticError(gettoken(currenttoken-1)[3]);
		else return "boolean";
	}
	public String pureequation() throws SyntaxError,SemanticError{
		//System.out.println("PUREEQUATION>"+currenttoken+" "+token[0]+"|line"+token[3]);
		boolean issym = false;
		if(Objects.equals(token[0], "-")) {
			issym = true;
		}
		if(Objects.equals(token[0], "+") || Objects.equals(token[0], "-")) sym();
		String type1 = term();
		if(issym==true) {
			write(String.format("\tPOP\tGR2\t\t;LINE:%s\n",token[3]));
			write(String.format("\tLD\tGR1,\t=0\t;LINE:%s\n",token[3]));
			write(String.format("\tSUBA\tGR1,\tGR2\t;LINE:%s\n",token[3]));
			write(String.format("\tPUSH\t0,\tGR1\t;LINE:%s\n",token[3]));	
		}
		issym=false;
		String type2 = pureequation2();
		if(Objects.equals(type2, "Null")) return type1;
		else if(!Objects.equals(type1, type2)) throw new SemanticError(gettoken(currenttoken)[3]);
		else return type1;
	}
	public String pureequation2() throws SyntaxError,SemanticError{
		//System.out.println("PUREEQUATION2>"+currenttoken+" "+token[0]+"|line"+token[3]);
		String status  = token[0];
		if(!Objects.equals(token[0], "+") && !Objects.equals(token[0], "-") && !Objects.equals(token[0], "or")) {
			return "Null";
		}
		addop();
		String type1 = term();
		write(String.format("\tPOP\tGR2\t\t;LINE:%s\n",token[3]));
		write(String.format("\tPOP\tGR1\t\t;LINE:%s\n",token[3]));
		if(Objects.equals(status, "+")) {
			write(String.format("\tADDA\tGR1,\tGR2\t;LINE:%s\n",token[3]));
		}else if(Objects.equals(status, "-")) {
			write(String.format("\tSUBA\tGR1,\tGR2\t;LINE:%s\n",token[3]));
		}else if(Objects.equals(status, "or")) {
			write(String.format("\tOR\tGR1,\tGR2\t;LINE:%s\n",token[3]));
		}
		write(String.format("\tPUSH\t0,\tGR1\t;LINE:%s\n",token[3]));
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
		String status = token[0];
		if(!Objects.equals(token[0], "*") && !Objects.equals(token[0], "/") && !Objects.equals(token[0], "div")&& !Objects.equals(token[0], "mod") && !Objects.equals(token[0], "and")) {
			return "Null";
		}
		multop();
		String type1 = factor();
		write(String.format("\tPOP\tGR2\t\t;LINE:%s\n",token[3]));
		write(String.format("\tPOP\tGR1\t\t;LINE:%s\n",token[3]));
		if(Objects.equals(status, "*")) {
			write(String.format("\tCALL\tMULT\t\t;LINE:%s\n",token[3]));	//get from G2
			write(String.format("\tPUSH\t0,\tGR2\t;LINE:%s\n",token[3]));
		}else if(Objects.equals(status, "/")) {
			write(String.format("\tCALL\tDIV\t\t;LINE:%s\n",token[3])); 	//get from GR2
			write(String.format("\tPUSH\t0,\tGR2\t;LINE:%s\n",token[3]));
		}else if(Objects.equals(status, "div")) {
			write(String.format("\tCALL\tDIV\t\t;LINE:%s\n",token[3])); 	//get from GR2
			write(String.format("\tPUSH\t0,\tGR2\t;LINE:%s\n",token[3]));
		}else if(Objects.equals(status, "mod")) {
			write(String.format("\tCALL\tDIV\t\t;LINE:%s\n",token[3])); //get from GR1
			write(String.format("\tPUSH\t0,\tGR1\t;LINE:%s\n",token[3]));
		}else if(Objects.equals(status, "and")) {
			write(String.format("\tAND\tGR1,\tGR2\t;LINE:%s\n",token[3]));	//get from G1 
			write(String.format("\tPUSH\t0,\tGR1\t;LINE:%s\n",token[3]));
		}
		String type2 = term2();
		if(Objects.equals(type2, "Null")) return type1;
		else if(Objects.equals(type1, type2)) return type1;
		else throw new SemanticError(gettoken(currenttoken-1)[3]);
	}
	public String factor() throws SyntaxError,SemanticError{
		//System.out.println("FACTOR>"+currenttoken+" "+token[0]+"|line"+token[3]);
		if(Objects.equals(token[0],"false") ||Objects.equals(token[0],"true"))
		{
			if(Objects.equals(token[0],"false")) {
				write(String.format("\tPUSH\t#FFFF\t\t;LINE:%s\n",token[3]));
			}else {
				write(String.format("\tPUSH\t#0000\t\t;LINE:%s\n",token[3]));
			}
			inctoken();
			return "boolean";
		}
		else if(Objects.equals(token[1],"SCONSTANT"))
		{
			write(String.format("\tPUSH\t%s\t\t;LINE:%s\n",token[0],token[3]));
			inctoken();
			return "integer";
		}
		else if(Objects.equals(token[1],"SSTRING"))
		{
			//System.out.println("SSTRING:"+token[0]+"\n");
			if(token[0].length()==3) {
				write(String.format("\tLD\tGR1,\t=%s\t;LINE:%s\n",token[0],token[3]));
				write(String.format("\tPUSH\t0,\tGR1\t;LINE:%s\n",token[3]));
				inctoken();
				return "char";
			}
			else {
				int isthere = -1;
				for (int i = 0; i < constant.size(); i++) {
					if(Objects.equals(token[0],constant.get(i))) {
						isthere = i;
						break;
					}
				}
				write(String.format("\tLD\tGR1,\t=%d\t;LINE:%s\n",token[0].length()-2,token[3]));
				write(String.format("\tPUSH\t0,\tGR1\t;LINE:%s\n",token[3]));
				if(isthere==-1) {
					constant.add(token[0]);
					write(String.format("\tLAD\tGR2,\tCHAR%d\t;LINE:%s\n",constant.size()-1,token[3]));
				}
				else {
					write(String.format("\tLAD\tGR2,\tCHAR%d\t;LINE:%s\n",isthere,token[3]));
				}
				write(String.format("\tPUSH\t0,\tGR2\t;LINE:%s\n",token[3]));
				inctoken();
				return "string";
			}
		}
		else if(Objects.equals(token[0],"not")) {
			//////System.out.println("*not factor*");
			inctoken();
			String type = factor();
			write(String.format("\tPOP\tGR1\t\t;LINE:%s\n",token[3]));
			write(String.format("\tXOR\tGR1,\t=#FFFF\t;LINE:%s\n",token[3]));
			write(String.format("\tPUSH\t0,\tGR1\t;LINE:%s\n",token[3]));
			return type;
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
			String varname = token[0];
			String type = variable();
			Node leftnode = searchallscope(varname);
			if(Objects.equals(leftnode.type,"integer")||Objects.equals(leftnode.type,"boolean")||Objects.equals(leftnode.type,"char")) {
				write(String.format("\tLD\tGR2,\t=%d\t;LINE:%s\n",searchallscope(varname).startingindex,token[3]));
				write(String.format("\tLD\tGR1,\tVAR,GR2\t;LINE:%s\n",token[3]));
				write(String.format("\tPUSH\t0,\tGR1\t;LINE:%s\n",token[3]));
			}
			else { 
				write(String.format("\tPOP\tGR2\t\t;LINE:%s\n",token[3])); //get the index out
				write(String.format("\tADDA\tGR2,\t=%d\t;LINE:%s\n",leftnode.startingindex-leftnode.startingnumber,token[3]));
				write(String.format("\tLD\tGR1,\tVAR,GR2\t;LINE:%s\n",token[3]));
				write(String.format("\tPUSH\t0,\tGR1\t;LINE:%s\n",token[3]));
			}
			return type;
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
			isreadln = true;
			inctoken();
			if(!Objects.equals("(", token[0])) return;
			word("(");
			variablel();
			word(")");
			isreadln= false;
		}
		else if(Objects.equals(token[0],"writeln")) {
			iswriteln=true;
			inctoken();
			if(!Objects.equals("(", token[0])) return;
			word("(");
			equationl();
			word(")");
			write(String.format("\tCALL\tWRTLN\t\t;LINE:%s\n",token[3]));
			iswriteln = false;
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
	public void printvariable(Node root){
			write(String.format("VAR\tDS\t%d\t\t;\n",lastmemoryused+1));
	}
	public void write(String a) {
		try {
			if(isreplaceleft==true) {
				rpleftcommand.add(a);
			}
			else if(issubdecg==true) {
				subdeccommand.add(a);
			}
			else {
				myWriter.write(a);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void printconstant() {
		for (int i = 0; i < constant.size(); i++) {
            write(String.format("CHAR%d\tDC\t%s;\n",i,constant.get(i)));
        }
	}
	public void addlib(final String inputFileName) {
    	List<String> allLines1;
		try {
			allLines1 = Files.readAllLines(Paths.get(inputFileName));
	    	for(String value: allLines1) {
	    		write(value+"\n");
	    	}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
	}
	public void run(final String inputFileName, final String outputFileName) {
	    	boolean done = false;
			try {
	    		myWriter = new FileWriter(outputFileName);
		    	allLines = Files.readAllLines(Paths.get(inputFileName));
		    	currenttoken = 0;
		    	token = gettoken(currenttoken);
		    	prog();
		    	done = true;
		    } catch(SyntaxError e) {
		    	e.print();
		    } catch(SemanticError e) {
		    	e.print();
		    }
		    catch (IOException e) {
		    	System.err.println("File not found");
		    }    
			try {
		    	if(done == false) {
						myWriter.close();
			    		File file = new File(outputFileName);
			    		file.delete();
		    	}
		    	else {
		    		addlib("data/cas/lib.cas");
		    		myWriter.close();
		    	}
			}
			catch (IOException e) {
				System.err.println("File not found");
			}
	}
}

