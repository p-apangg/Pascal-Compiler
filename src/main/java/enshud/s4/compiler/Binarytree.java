package enshud.s4.compiler;
import java.util.Objects;
public class Binarytree{
	class Node{
		String varname;
		Node left,right;
		String type;
		int size;
		int startingindex;
		int startingnumber; //used only for array
		public Node(String name1,String type1,int size1,int startingindex1,int startingnumber1) {
			varname = name1;
			type = type1;
			size = size1;
			startingindex = startingindex1;
			startingnumber = startingnumber1;
			left = null;
			right = null;
		}
	}
	Node root;
	Binarytree(){
		root = null;
	}
	boolean insert(String name1,String type1,int size1,int startingindex1,int startingnumber1) {
		if(!Objects.equals(search(name1),null)) return false;
		//System.out.println("Add new data : "+name1);
		root = insert1(root,name1,type1,size1,startingindex1,startingnumber1);
		//printorder();
		if(root==null) return false;
		return true;
	}
	
	Node insert1(Node root,String name1,String type1,int size1,int startingindex1,int startingnumber1) {
		if(root==null) {
			root = new Node(name1,type1,size1,startingindex1,startingnumber1);
		}
		else {
			String compname = root.varname;
			if(name1.compareTo(compname)<0) {
				root.left = insert1(root.left,name1,type1,size1,startingindex1,startingnumber1);
			}else {
				root.right = insert1(root.right,name1,type1,size1,startingindex1,startingnumber1);
			}
		}
		return root;
	}
	Node search(String name1) {
		//System.err.println("Search data : "+name1);
		Node root1 = search1(root,name1);
		return root1;
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
	int count() {
		return count1(root);
	}
	int count1(Node root1) {
		if(root1!=null) {
			return count1(root1.left)+count1(root1.right)+1;
		}
		return 0;
	}
	void printorder() {
		printorder1(root);
		//System.err.println();
	}
	void printorder1(Node root1) {
		if(root1!=null) {
			printorder1(root1.left);
			//System.err.print(root1.varname+":");
			//System.err.print(root1.type+":");
			//System.err.print(root1.size+">\n");
			printorder1(root1.right);
		}
	}
	public static void main(String[] args) {
	    Binarytree tree = new Binarytree();
	    System.out.println("#"+tree.search("papang"));
	    tree.insert("cat","boolean",1,2,-1);
	    tree.insert("ant","int",2,3,-1);
	    tree.insert("dog","int",3,4,-1);	
	    tree.insert("boy","char",4,5,-1);  
	    tree.insert("cat","int",5,6,-1); 
	    System.out.println("#"+tree.search("cat"));
	    System.out.println("#"+tree.search("boy"));
	  }
}