package enshud.s2.parser;
import java.util.ArrayList;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

import enshud.s2.parser.Token;
import enshud.s2.parser.Result;

public class Parser {

	/**
	 * サンプルmainメソッド．
	 * 単体テストの対象ではないので自由に改変しても良い．
	 */
	public static void main(final String[] args) {
		// normalの確認
		new Parser().run("data/ts/normal01.ts");
		new Parser().run("data/ts/normal02.ts");
		new Parser().run("data/ts/normal03.ts");
		new Parser().run("data/ts/normal04.ts");
		new Parser().run("data/ts/normal05.ts");
		new Parser().run("data/ts/normal06.ts");
		new Parser().run("data/ts/normal07.ts");
		new Parser().run("data/ts/normal08.ts");
		new Parser().run("data/ts/normal09.ts");
		new Parser().run("data/ts/normal10.ts");
		new Parser().run("data/ts/normal11.ts");
		new Parser().run("data/ts/normal12.ts");
		new Parser().run("data/ts/normal13.ts");
		new Parser().run("data/ts/normal14.ts");
		new Parser().run("data/ts/normal15.ts");
		new Parser().run("data/ts/normal16.ts");
		new Parser().run("data/ts/normal17.ts");
		new Parser().run("data/ts/normal18.ts");
		new Parser().run("data/ts/normal19.ts");
		new Parser().run("data/ts/normal20.ts");
		//semerro1
		new Parser().run("data/ts/semerr01.ts");
		new Parser().run("data/ts/semerr02.ts");
		new Parser().run("data/ts/semerr03.ts");
		new Parser().run("data/ts/semerr04.ts");
		new Parser().run("data/ts/semerr05.ts");
		new Parser().run("data/ts/semerr06.ts");
		new Parser().run("data/ts/semerr07.ts");
		new Parser().run("data/ts/semerr08.ts");
		// synerrの確認
		new Parser().run("data/ts/synerr01.ts");
		new Parser().run("data/ts/synerr02.ts");
		new Parser().run("data/ts/synerr03.ts");
		new Parser().run("data/ts/synerr04.ts");
		new Parser().run("data/ts/synerr05.ts");
		new Parser().run("data/ts/synerr06.ts"); //13
		new Parser().run("data/ts/synerr07.ts"); //30
		new Parser().run("data/ts/synerr08.ts"); //31 
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
	public Boolean isidentifier(String[] token) {
		//System.out.println("=CHECK IDENTIFIER"+"=");
		if(Objects.equals(token[1],"SIDENTIFIER")) {
			return true;
		}
		return false;
	}
	public Boolean isnum(String[] token) {
		//System.out.println("=CHECK CONSTANT"+"=");
		if(Objects.equals(token[1],"SCONSTANT")) {
			return true;
		}
		return false;
	}
	public String[] gettoken(List<String> allLines,int currenttoken) {
		//System.out.println(allLines.get(currenttoken).split("\\s+")[0]);
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
	public void printans(int layer, List<String> allLines,Result answer) {
		String[] token=gettoken(allLines,answer.last);
		//System.out.println(token[0]+" errorline"+"="+answer.errline);
	}
	public Result prog(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")PROGRAM"+currenttoken+"=");
		Result answer = word(layer+1,allLines,currenttoken,"program");
		if(answer.errline!="") return answer;
		answer = progname(layer+1,allLines,answer.last);
		if(answer.errline!="") return answer;
		answer = word(layer+1,allLines,answer.last,";");
		if(answer.errline!="") return answer;
		answer = block(layer+1,allLines,answer.last);
		if(answer.errline!="") return answer;
		answer = comp(layer+1,allLines,answer.last);
		if(answer.errline!="") return answer;
		answer = word(layer+1,allLines,answer.last,".");
		return answer;
	}
	public Result word(int layer, List<String> allLines,int currenttoken,String w) {
		//System.out.print("="+layer+")WORD"+currenttoken+"=");
		String[] token=gettoken(allLines,currenttoken);
		if(Objects.equals(w,token[0])) {
			return new Result(currenttoken+1,"");
		}
		return new Result(currenttoken,token[3]);
	}
	public Result progname(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")PROGRAMNAME"+currenttoken+"=");
		String[] token=gettoken(allLines,currenttoken);
		if(isidentifier(token)) {
			return new Result(currenttoken+1,"");
		}
		return new Result(currenttoken,token[3]);
	}
	public Result block(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")BLOCK"+currenttoken+"=");
		Result answer = vardec(layer+1,allLines,currenttoken);
		if(answer.errline!="") return answer;
		answer = subdecg(layer+1,allLines,answer.last);
		return answer;
	}
	public Result vardec(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")VARDEC"+currenttoken+"=");
		Result answer = word(layer+1,allLines,currenttoken,"var");
		if(answer.errline!="") return new Result(currenttoken,"");
		answer = vardecl(layer+1,allLines,answer.last);
		return answer;
	}
	public Result vardecl(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")VARDECL"+currenttoken+"=");
		Result answer = varl(layer+1,allLines,currenttoken);
		if(answer.errline!="") return answer;
		answer = word(layer+1,allLines,answer.last,":");
		if(answer.errline!="") return answer;
		answer = type(layer+1,allLines,answer.last);
		if(answer.errline!="") return answer;
		answer = word(layer+1,allLines,answer.last,";");
		if(answer.errline!="") return answer;
		answer = vardecl2(layer+1,allLines,answer.last);
		return answer;
	}
	public Result vardecl2(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")VARDECL2"+currenttoken+"=");
		Result answer = varl(layer+1,allLines,currenttoken);
		if(answer.errline!="") return new Result(currenttoken,"");
		answer = word(layer+1,allLines,answer.last,":");
		if(answer.errline!="") return answer;
		answer = type(layer+1,allLines,answer.last);
		if(answer.errline!="") return answer;
		answer = word(layer+1,allLines,answer.last,";");
		if(answer.errline!="") return answer;
		answer = vardecl2(layer+1,allLines,answer.last);
		return answer;
	}
	public Result varl(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")VARL"+currenttoken+"=");
		Result answer = varname(layer+1,allLines,currenttoken);
		if(answer.errline!="") return answer;
		answer = varl2(layer+1,allLines,answer.last);
		return answer;
	}
	public Result varl2(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")VARL2"+currenttoken+"=");
		Result answer = word(layer+1,allLines,currenttoken,",");
		if(answer.errline!="") return new Result(currenttoken,"");
		answer = varname(layer+1,allLines,answer.last);
		if(answer.errline!="") return answer;
		answer = varl2(layer+1,allLines,answer.last);
		return answer;
	}
	public Result varname(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")VAR"+currenttoken+"=");
		String[] token=gettoken(allLines,currenttoken);
		if(isidentifier(token)) {
			return new Result(currenttoken+1,"");
		}
		return new Result(currenttoken,token[3]);
	}
	public Result type(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")TYPE"+currenttoken+"=");
		String[] token=gettoken(allLines,currenttoken);
		if(Objects.equals(token[0],"integer") || Objects.equals(token[0],"char") || Objects.equals(token[0],"boolean")) {
			return ntype(layer+1,allLines,currenttoken);
		}
		if(Objects.equals(token[0],"array") ) {
			return atype(layer+1,allLines,currenttoken);
		}
		return new Result(currenttoken,token[3]);
	}
	public Result ntype(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")NTYPE"+currenttoken+"=");
		String[] token=gettoken(allLines,currenttoken);
		if(Objects.equals(token[0],"integer") || Objects.equals(token[0],"char") || Objects.equals(token[0],"boolean")) {
			return new Result(currenttoken+1,"");
		}
		return new Result(currenttoken,token[3]);
	}
	public Result atype(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")ATYPE"+currenttoken+"=");
		Result answer = word(layer+1,allLines,currenttoken,"array");
		if(answer.errline!="") return answer;
		answer = word(layer+1,allLines,answer.last,"[");
		if(answer.errline!="") return answer;
		answer = minnum(layer+1,allLines,answer.last);
		if(answer.errline!="") return answer;
		answer = word(layer+1,allLines,answer.last,"..");
		if(answer.errline!="") return answer;
		answer = maxnum(layer+1,allLines,answer.last);
		if(answer.errline!="") return answer;
		answer = word(layer+1,allLines,answer.last,"]");
		if(answer.errline!="") return answer;
		answer = word(layer+1,allLines,answer.last,"of");
		if(answer.errline!="") return answer;
		answer = ntype(layer+1,allLines,answer.last);
		return answer;
	}
	public Result minnum(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")MINNUM"+currenttoken+"=");
		return num(layer+1,allLines,currenttoken);
	}
	public Result maxnum(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")MAXNUM"+currenttoken+"=");
		return num(layer+1,allLines,currenttoken);
	}
	public Result num(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")NUM"+currenttoken+"=");
		Result answer = sym(layer+1,allLines,currenttoken);
		if(answer.errline!="") answer = new Result(currenttoken,"");
		String[] token=gettoken(allLines,answer.last);
		//System.out.println("token="+token[0]);
		if(isnum(token)) return new Result(answer.last+1,"");
		return new Result(answer.last,token[3]);
	}
	public Result sym(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")SYM"+currenttoken+"=");
		String[] token=gettoken(allLines,currenttoken);
		if(Objects.equals(token[0],"+") || Objects.equals(token[0],"-")) {
			return new Result(currenttoken+1,"");
		}
		return new Result(currenttoken,token[3]);
	}
	public Result subdecg(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")SUBDECG"+currenttoken+"=");
		Result answer = subdec(layer+1,allLines,currenttoken);
		if(answer.errline!="") {
			String[] token=gettoken(allLines,answer.last);
			if(Objects.equals("begin", token[0])) { // wat dis again
				return new Result(currenttoken,"");
			}
			return answer;
		}
		answer = word(layer+1,allLines,answer.last,";");
		if(answer.errline!="") return answer;
		answer = subdecg(layer+1,allLines,answer.last);
		return answer;
	}
	public Result subdec(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")SUBDEC"+currenttoken+"=");
		Result answer = subhead(layer+1,allLines,currenttoken);
		if(answer.errline!="") return answer;
		answer = vardec(layer+1,allLines,answer.last);
		if(answer.errline!="") return answer;
		//System.out.println("###SUBDECanswer line:"+answer.last);
		answer = comp(layer+1,allLines,answer.last);
		return answer;
	}
	public Result subhead(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")SUBHEAD"+currenttoken+"=");
		Result answer = word(layer+1,allLines,currenttoken,"procedure");
		if(answer.errline!="") return answer;
		answer = procname(layer+1,allLines,answer.last);
		if(answer.errline!="") return answer;
		answer = tpara(layer+1,allLines,answer.last);
		if(answer.errline!="") return answer;
		answer = word(layer+1,allLines,answer.last,";");
		return answer;
	}	
	public Result procname(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")PROCNAME"+currenttoken+"=");
		String[] token=gettoken(allLines,currenttoken);
		if(isidentifier(token)) {
			return new Result(currenttoken+1,"");
		}
		return new Result(currenttoken,token[3]);
	}
	public Result tpara(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")TPARA"+currenttoken+"=");
		Result answer = word(layer+1,allLines,currenttoken,"(");
		if(answer.errline!="") return new Result(currenttoken,"");
		answer = tparal(layer+1,allLines,answer.last);
		if(answer.errline!="") return answer;
		answer = word(layer+1,allLines,answer.last,")");
		return answer;
	}
	public Result tparal(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")TPARAL"+currenttoken+"=");
		Result answer = tparanamel(layer+1,allLines,currenttoken);
		if(answer.errline!="") return answer;
		answer = word(layer+1,allLines,answer.last,":");
		if(answer.errline!="") return answer;
		answer = ntype(layer+1,allLines,answer.last);
		if(answer.errline!="") return answer;
		answer = tparal2(layer+1,allLines,answer.last);
		return answer;
	}
	public Result tparal2(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")TPARAL2"+currenttoken+"=");
		Result answer = word(layer+1,allLines,currenttoken,";");
		if(answer.errline!="") return new Result(currenttoken,"");
		answer = tparanamel(layer+1,allLines,answer.last);
		if(answer.errline!="") return answer;
		answer = word(layer+1,allLines,answer.last,":");
		if(answer.errline!="") return answer;
		answer = ntype(layer+1,allLines,answer.last);
		if(answer.errline!="") return answer;
		answer = tparal2(layer+1,allLines,answer.last);
		return answer;
	}
	public Result tparanamel(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")TPARANAMEL"+currenttoken+"=");
		Result answer = tparaname(layer+1,allLines,currenttoken);
		if(answer.errline!="") return answer;
		answer = tparanamel2(layer+1,allLines,answer.last);
		return answer;
	}
	public Result tparanamel2(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")TPARANAMEL2"+currenttoken+"=");
		Result answer = word(layer+1,allLines,currenttoken,",");
		if(answer.errline!="") return new Result(currenttoken,"");
		answer = tparaname(layer+1,allLines,answer.last);
		if(answer.errline!="") return answer;
		answer = tparanamel2(layer+1,allLines,answer.last);
		return answer;
	}
	public Result tparaname(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")TPARANAME"+currenttoken+"=");
		String[] token=gettoken(allLines,currenttoken);
		if(isidentifier(token)) {
			return new Result(currenttoken+1,"");
		}
		return new Result(currenttoken,token[3]);
	}
	public Result comp(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")COMP"+currenttoken+"=");
		Result answer = word(layer+1,allLines,currenttoken,"begin");
		if(answer.errline!="") return answer;
		answer = statementg(layer+1,allLines,answer.last);
		if(answer.errline!="") return answer;
		answer = word(layer+1,allLines,answer.last,"end");
		return answer;
	}
	public Result statementg(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")STATEMENTG"+currenttoken+"=");
		Result answer = statement(layer+1,allLines,currenttoken);
		if(answer.errline!="") return answer;
		answer = word(layer+1,allLines,answer.last,";");
		if(answer.errline!="") return answer;
		answer = statementg2(layer+1,allLines,answer.last);
		return answer;
	}
	public Result statementg2(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")STATEMENTG2"+currenttoken+"=");
		/*ERROR here : papang , write first()*/
		String[] token=gettoken(allLines,currenttoken);
		Result answer;
		if(Objects.equals(token[1],"SIF") || Objects.equals(token[1],"SIDENTIFIER") 
			|| Objects.equals(token[1],"SWHILE") || Objects.equals(token[1],"SWRITELN")
			|| Objects.equals(token[1],"SREADLN") ||Objects.equals(token[1],"SBEGIN"))
		{
				answer = statement(layer+1,allLines,currenttoken);
				if(answer.errline!="") return answer;
		}
		else return new Result(currenttoken,"");
		//Result answer = statement(layer+1,allLines,currenttoken);
		if(answer.errline!="") return new Result(currenttoken,"");
		answer = word(layer+1,allLines,answer.last,";");
		if(answer.errline!="") return answer;
		answer = statementg2(layer+1,allLines,answer.last);
		return answer;
	}
	public Result statement(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")STATEMENT"+currenttoken+"=");
		String[] token=gettoken(allLines,currenttoken);
		if(Objects.equals(token[0],"if")) return ifst(layer+1,allLines,currenttoken);
		if(Objects.equals(token[0],"while")) {
			Result answer = whilest(layer+1,allLines,currenttoken);
			return answer;
		}
		Result answer = fundast(layer+1,allLines,currenttoken);
		return answer;
	}
	public Result ifst(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")IFST"+currenttoken+"=");
		Result answer = word(layer+1,allLines,currenttoken,"if");
		if(answer.errline!="") return answer;
		answer = equation(layer+1,allLines,answer.last);
		if(answer.errline!="") return answer;
		answer = word(layer+1,allLines,answer.last,"then");
		if(answer.errline!="") return answer;
		answer = comp(layer+1,allLines,answer.last);
		if(answer.errline!="") return answer;
		answer = elsest(layer+1,allLines,answer.last);
		return answer;
	}
	public Result elsest(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")ELSEST"+currenttoken+"=");
		Result answer = word(layer+1,allLines,currenttoken,"else");
		if(answer.errline!="") return new Result(currenttoken,"");
		answer = comp(layer+1,allLines,answer.last);
		return answer;
	}
	public Result whilest(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")WHILEST"+currenttoken+"=");
		Result answer = word(layer+1,allLines,currenttoken,"while");
		if(answer.errline!="") return answer;
		answer = equation(layer+1,allLines,answer.last);
		if(answer.errline!="") return answer;
		answer = word(layer+1,allLines,answer.last,"do");
		if(answer.errline!="") return answer;
		answer = comp(layer+1,allLines,answer.last);
		return answer;
	}
	public Result fundast(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")FUNDST"+currenttoken+"=");
		String[] token=gettoken(allLines,currenttoken);
		if(Objects.equals(token[0],"readln")||Objects.equals(token[0],"writeln")) {
			Result answer = inout(layer+1,allLines,currenttoken);
			return answer;
		}
		if(Objects.equals(token[0],"begin")) {
			Result answer = comp(layer+1,allLines,currenttoken);
			return answer;
		}
		if(isidentifier(token)) {
			currenttoken+=1;
			token=gettoken(allLines,currenttoken);
			//System.out.print("="+layer+")REPLACE or PROCEDURECALL"+currenttoken+"=");
			if(Objects.equals(token[0],":=") || Objects.equals(token[0],"[")) {
				Result answer = replace(layer+1,allLines,currenttoken);
				return answer;
			}
			else {
				Result answer = procedurecall(layer+1,allLines,currenttoken);
				return answer;
			}
		}
		return new Result(currenttoken,token[3]);
	}
	public Result replace(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")REPLACE"+currenttoken+"=");
		Result answer;
		String[] token=gettoken(allLines,currenttoken);
		if(Objects.equals(token[0],"[")) {
			answer = variablewindex(layer+1,allLines,currenttoken);
			//System.out.println("####REPanswer line:"+answer.errline+"token:"+answer.last);
			if(answer.errline!="") return answer;
			currenttoken = answer.last;
		}
		answer = word(layer+1,allLines,currenttoken,":=");
		//System.out.println("####REPanswer line:"+answer.errline+"token:"+answer.last);
		if(answer.errline!="") return answer;
		answer = equation(layer+1,allLines,answer.last);
		//System.out.println("####REPanswer line:"+answer.errline+"token:"+answer.last);
		return answer;
	}
	public Result variablewindex(int layer, List<String> allLines,int currenttoken) {
		//System.out.println("="+layer+")VARIABLEWINDEX"+currenttoken+"=");
		Result answer = word(layer+1,allLines,currenttoken,"[");
		if(answer.errline!="") return answer;
		answer = index(layer+1,allLines,answer.last);
		if(answer.errline!="") return answer;
		answer = word(layer+1,allLines,answer.last,"]");
		return answer;
	}
	public Result variable(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")VARIABLE"+currenttoken+"=");
		Result answer = varname(layer+1,allLines,currenttoken);
		if(answer.errline!="") return answer;
		answer = word(layer+1,allLines,answer.last,"[");
		if(answer.errline!="") return new Result(currenttoken+1,"");
		answer = index(layer+1,allLines,answer.last);
		if(answer.errline!="") return answer;
		answer = word(layer+1,allLines,answer.last,"]");
		return answer;
	}
	public Result index(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")INDEX"+currenttoken+"=");
		Result answer = equation(layer+1,allLines,currenttoken);
		return answer;
	}
	public Result procedurecall(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")PROCEDURECALL"+currenttoken+"=");
		Result answer = word(layer+1,allLines,currenttoken,"(");
		if(answer.errline!="") return new Result(currenttoken,"");
		answer = equationl(layer+1,allLines,answer.last);
		if(answer.errline!="") return answer;
		answer = word(layer+1,allLines,answer.last,")");
		return answer;
	}
	public Result equationl(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")EQUATIONL"+currenttoken+"=");
		Result answer = equation(layer+1,allLines,currenttoken);
		if(answer.errline!="") return answer;
		answer = equationl2(layer+1,allLines,answer.last);
		return answer;
	}
	public Result equationl2(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")EQUATIONL2"+currenttoken+"=");
		Result answer = word(layer+1,allLines,currenttoken,",");
		if(answer.errline!="") return new Result(currenttoken,"");
		answer = equation(layer+1,allLines,answer.last);
		if(answer.errline!="") return answer;
		answer = equationl2(layer+1,allLines,answer.last);
		return answer;
	}
	public Result equation(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")EQUATION"+currenttoken+"=");
		Result answer = pureequation(layer+1,allLines,currenttoken);
		if(answer.errline!="") return answer;
		answer = relaop(layer+1,allLines,answer.last);
		if(answer.errline!="") return new Result(answer.last,"");
		answer = pureequation(layer+1,allLines,answer.last);
		return answer;
	}
	public Result pureequation(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")PUREEQUATION"+currenttoken+"=");
		Result answer = sym(layer+1,allLines,currenttoken);
		if(answer.errline!="") answer = new Result(currenttoken,"");
		answer = term(layer+1,allLines,answer.last);
		//System.out.println("###PUREEQanswer line:"+answer.errline+"token:"+answer.last);
		if(answer.errline!="") return answer;
		answer = pureequation2(layer+1,allLines,answer.last);
		return answer;
	}
	public Result pureequation2(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")PUREEQUATION2"+currenttoken+"=");
		Result answer = addop(layer+1,allLines,currenttoken);
		if(answer.errline!="") return new Result(currenttoken,"");
		answer = term(layer+1,allLines,answer.last);
		if(answer.errline!="") return answer;
		answer = pureequation2(layer+1,allLines,answer.last);
		return answer;
	}
	public Result term(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")TERM"+currenttoken+"=");
		Result answer = factor(layer+1,allLines,currenttoken);
		//System.out.println("###TERManswer line:"+answer.errline+"token:"+answer.last);
		if(answer.errline!="") return answer;
		answer = term2(layer+1,allLines,answer.last);
		return answer;
	}
	public Result term2(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")TERM2"+currenttoken+"=");
		Result answer = multop(layer+1,allLines,currenttoken);
		if(answer.errline!="") return new Result(currenttoken,"");
		answer = factor(layer+1,allLines,answer.last);
		if(answer.errline!="") return answer;
		answer = term2(layer+1,allLines,answer.last);
		return answer;
	}
	public Result factor(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")FACTOR"+currenttoken+"=");
		String[] token=gettoken(allLines,currenttoken);
		//System.out.println(token[0]+" "+token[1]+" "+token[2]+" "+token[3]);
		if(Objects.equals(token[0],"false") ||Objects.equals(token[0],"true")
				||Objects.equals(token[1],"SCONSTANT")||Objects.equals(token[1],"SSTRING"))
		{
			//System.out.println("*teisuu*");
			currenttoken+=1; // 定数
			return new Result(currenttoken,"");
		}
		else if(Objects.equals(token[0],"not")) {
			//System.out.println("*not factor*");
			currenttoken+=1;
			Result answer = factor(layer+1,allLines,currenttoken);
			return answer;
		}
		else if(Objects.equals(token[0],"(")) {
			//System.out.println("*(shiki)*");
			currenttoken+=1;
			Result answer = equation(layer+1,allLines,currenttoken);
			if(answer.errline!="") return answer;
			token= gettoken(allLines,answer.last);
			answer = word(layer+1,allLines,answer.last,")");
			return answer;
		}
		else if(Objects.equals(token[1],"SIDENTIFIER")) {
			//System.out.println("*hensuu*");
			currenttoken+=1;
			token=gettoken(allLines,currenttoken);
			if(Objects.equals(token[0],"[")) {
				Result answer = variablewindex(layer+1,allLines,currenttoken);
				return answer;
			}
			return new Result(currenttoken,"");
		}
		return new Result(currenttoken,token[3]);
	}
	public Result relaop(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")RELAOP"+currenttoken+"=");
		String[] token=gettoken(allLines,currenttoken);
		if(Objects.equals(token[0],"=")||Objects.equals(token[0],"<>")||Objects.equals(token[0],"<")||Objects.equals(token[0],"<=")||Objects.equals(token[0],">")||Objects.equals(token[0],">=")) {
			return new Result(currenttoken+1,"");
		}
		return new Result(currenttoken,token[3]);
	}
	public Result addop(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")ADDOP"+currenttoken+"=");
		String[] token=gettoken(allLines,currenttoken);
		if(Objects.equals(token[0],"+")||Objects.equals(token[0],"-")||Objects.equals(token[0],"or")) {
			return new Result(currenttoken+1,"");
		}
		return new Result(currenttoken,token[3]);
	}
	public Result multop(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")MULTOP"+currenttoken+"=");
		String[] token=gettoken(allLines,currenttoken);
		if(Objects.equals(token[0],"*")||Objects.equals(token[0],"/")||Objects.equals(token[0],"div")||Objects.equals(token[0],"mod")||Objects.equals(token[0],"and")) {
			return new Result(currenttoken+1,"");
		}
		return new Result(currenttoken,token[3]);
	}
	public Result inout(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")INOUT"+currenttoken+"=");
		int oldtoken = currenttoken;
		String[] token=gettoken(allLines,currenttoken);
		if(Objects.equals(token[0],"readln")) {
			currenttoken+=1;
			Result answer= word(layer+1,allLines,currenttoken,"(");
			if(answer.errline!="") return new Result(currenttoken,"");
			answer=variablel(layer+1,allLines,answer.last);
			if(answer.errline!="") return answer;
			return word(layer+1,allLines,answer.last,")");
		}
		else if(Objects.equals(token[0],"writeln")) {
			currenttoken+=1;
			Result answer= word(layer+1,allLines,currenttoken,"(");
			if(answer.errline!="") return new Result(currenttoken,"");
			answer=equationl(layer+1,allLines,answer.last);
			if(answer.errline!="") return answer;
			answer = word(layer+1,allLines,answer.last,")");
			return answer;
		}
		return new Result(oldtoken,token[3]);
	}
	public Result variablel(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")VARIABLEL"+currenttoken+"=");
		Result answer = variable(layer+1,allLines,currenttoken);
		if(answer.errline!="") return answer;
		answer = variablel2(layer+1,allLines,answer.last);
		return answer;
	}
	public Result variablel2(int layer, List<String> allLines,int currenttoken) {
		//System.out.print("="+layer+")VARIABLEL2"+currenttoken+"=");
		Result answer = word(layer+1,allLines,currenttoken,",");
		if(answer.errline!="") return new Result(currenttoken,"");
		answer = variable(layer+1,allLines,answer.last);
		if(answer.errline!="") return answer;
		answer = variablel2(layer+1,allLines,answer.last);
		return answer;
	}
	public void run(final String inputFileName) {
		// TODO
	    try {
	    	List<String> allLines = Files.readAllLines(Paths.get(inputFileName));
	    	int currenttoken = 0;
	    	Result answer = prog(0,allLines,currenttoken);
	    	if(answer.last==allLines.size() && answer.errline=="") {
	    		System.out.println("OK");
	    	}
	    	else {
	    		System.err.println("Syntax error: line "+answer.errline);
	    	}
	    } catch (IOException e) {
	        System.err.println("File not found");
	    } 
	}
}
