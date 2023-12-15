package enshud.s3.checker;
import java.util.Objects;
public class Binarytree{
	class Node{
		String varname;
		Node left,right;
		String type;
		public Node(String name1,String type1) {
			varname = name1;
			type = type1;
			left = null;
			right = null;
		}
	}
	Node root;
	Binarytree(){
		root = null;
	}
	boolean insert(String name1,String type1) {
		if(!Objects.equals(search(name1),"")) return false;
		//System.out.println("Add new data : "+name1);
		root = insert1(root,name1,type1);
		printorder();
		if(root==null) return false;
		return true;
	}
	
	Node insert1(Node root,String name1,String type1) {
		if(root==null) {
			root = new Node(name1,type1);
		}
		else {
			String compname = root.varname;
			if(name1.compareTo(compname)<0) {
				root.left = insert1(root.left,name1,type1);
			}else {
				root.right = insert1(root.right,name1,type1);
			}
		}
		return root;
	}
	String search(String name1) {
		//System.err.println("Search data : "+name1);
		Node root1 = search1(root,name1);
		if(root1==null) {
			////System.out.println(name1+" dont exist");
			return "";
		}
		////System.out.println(name1+" exist");
		return root1.type;
	}
	Node search1(Node root1,String name1) {
		if(root1==null) {
			return null;
		}
		else {
			String compname = root1.varname;
			if(name1.compareTo(compname)==0) {
				return root1;
			}
			else if(name1.compareTo(compname)<0) {
				return search1(root1.left,name1);
			}else {
				return search1(root1.right,name1);
			}
		}
	}
	
	void printorder() {
		printorder1(root);
		//System.err.println();
	}
	void printorder1(Node root) {
		if(root!=null) {
			printorder1(root.left);
			//System.err.print(root.varname+":");
			//System.err.print(root.type+">");
			printorder1(root.right);
		}
	}
	public static void main(String[] args) {
	    Binarytree tree = new Binarytree();
	    //System.out.println("#"+tree.search("papang"));
	    tree.insert("cat","boolean");
	    tree.insert("ant","int");
	    tree.insert("dog","int");	
	    tree.insert("boy","char");  
	    tree.insert("cat","int"); 
	    //System.out.println("#"+tree.search("bee"));
	    //System.out.println("#"+tree.search("boy"));
	  }
}