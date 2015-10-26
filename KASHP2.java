// @author Karan


import java.util.*;
import java.io.*; 

class KASHP2{

	public static void main(String [] args) throws IOException {
		//the "throws" statement is for reading from a named file
		Scanner sc = new Scanner(System.in); // to read from System.in
		//Scanner sc = new Scanner(new File("p2d1in.txt"));
		// switch the comments above to read from file before submitting 
		
		TwoThreeTree tree = new TwoThreeTree();
		String line = sc.nextLine();

		while(!(line.split(" ")[0].equals("E"))){	//E ends the program

			String[] input = line.split(" ");

			//insert 								I num
			if(input[0].equals("I")){
				int k = Integer.parseInt(input[1]);
				boolean inserted = tree.insert(k);
				if(inserted) System.out.println("Inserted " + k);
				else System.out.println("Didn't insert " + k);
			}
			//delete 								D num
			else if(input[0].equals("D")){
				int k = Integer.parseInt(input[1]);
				boolean deleted = tree.remove(k);
				if(deleted) System.out.println("Deleted " + k);
				else System.out.println("Didn't delete " + k);
			}
			//search 								S num
			else if(input[0].equals("S")){
				int k = Integer.parseInt(input[1]);
				boolean found = tree.search(k);
				if(found) System.out.println("Found " + k);
				else System.out.println("Didn't find " + k);
			}
			//print leaves in order by key value 	K
			else if(input[0].equals("K")){
				tree.keyOrderList();
				System.out.println();
			}
			//print bfs of tree 				 	B
			else if(input[0].equals("B")){
				tree.bfsList();
				System.out.println();
			}
			//print height of tree 					H
			else if(input[0].equals("H")){
				System.out.println(tree.height());
			}
			//print total number of keys			M
			else if(input[0].equals("M")){
				System.out.println(tree.numberOfKeys());
			}

			line = sc.nextLine();
		}
	}
}

class TwoThreeTree {

	Node root;

	TwoThreeTree(){
	}

	boolean insert(int key){
		if(root==null){		//first insertion into empty tree
			int[] rootKeys = {key, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};
			Node[] rootChildren = new Node[4];
			rootChildren[0] = new Node(key);

			root = new Node(rootKeys, rootChildren);
			return true;
		}

		int prevNumberNodes = numberOfKeys();

		insert(root, key);

		if(numberOfKeys() == prevNumberNodes)	//no insertion was made
			return false;
		else									//node was inserted
			return true;
	}

	private Node insert(Node n, int key){
		if(n==null)			//insert at last level
			return new Node(key);

		if(n.isLeafNode()){	//reached the last level
			if(n.key == key)	//already in tree, return null
				return null;
			else
				return new Node(key);	//insert a new leaf node
		}

		else{
			Node newChild = null;	//if not null by the end of this, we have to insert the newChild into this tree

			int i;	//index of subtree to which newChild will be inserted

			//search the left and middle subtrees
			for(i=0; i<=1; i++)
				if(n.children[i]==null || key < n.keys[i+1]){	//if subtree is empty or key is less than (i+1)'th key, insert node
					newChild = insert(n.children[i], key);
					break;
				}
				else{
					if(key == n.keys[i]){
						return null;
					}
				}	

			//search the right subtree
			if(key > n.keys[2]){
				i=2;
				newChild = insert(n.children[i], key);
			}
			
			//if n has a new child...
			if(newChild!=null){
				//if the new child is less than the current i'th child (or there is no i'th child)
				if(n.children[i]==null || newChild.key < n.children[i].key){	
					n.shiftRight(i);						//shift all of the children to the right of i
					n.children[i] = newChild;				//to make room for the new child
				}
				//if the new child is greater than the current i'th child
				else{									
					n.shiftRight(i+1);						//shift all of the children to the right of i+1
					n.children[i+1] = newChild;				//to make room for the new child
				}

				//every time we insert a new node
				//we update all keys
				n.updateKeys();
			}

			// if we have more than 3 children, we must split and send the new node to the parent
			if(n.children[3]!=null)
				return split(n);
			//otherwise, we can just return null
			else
				return null;
		}
	}

	int findMinKey(Node n){
		if(n.isLeafNode)return n.key;
		else			return findMinKey(n.children[0]);
	}

	Node split(Node n){
		int[] siblingKeys = {n.keys[2], n.keys[3], Integer.MAX_VALUE, Integer.MAX_VALUE};
		Node[] siblingChildren = new Node[4];
		siblingChildren[0] = n.children[2];
		siblingChildren[1] = n.children[3];
		Node sibling = new Node(siblingKeys, siblingChildren);
		n.keys[2] = Integer.MAX_VALUE;
		n.keys[3] = Integer.MAX_VALUE;
		n.children[2] = null;
		n.children[3] = null;
		if(n!=root)				//if n is not the root, we can split like normal
			return sibling;
		else{						//if n is root, split it and make both it and its sibling the children of a new root
			int[] newRootKeys = {n.keys[0], sibling.keys[0], Integer.MAX_VALUE, Integer.MAX_VALUE};
			Node[] newRootChildren = new Node[4];
			newRootChildren[0] = n;					//potential trouble area? n = root, root is being replaced by newRoot, which has n as a child
			newRootChildren[1] = sibling;
			Node newRoot = new Node(newRootKeys, newRootChildren);
			root = newRoot;
		}
		return null;
	}

	boolean search(int key){
		if(root==null)	return false;	//empty tree

		return root.search(key);
	}
	
	boolean remove(int key){
		if(root==null){		//cannot delete empty tree;
			return false;
		}

		int prevNumberNodes = numberOfKeys();

		remove(root, key);

		if(numberOfKeys() == prevNumberNodes)	//no delete was made
			return false;
		else									//node was deleted
			return true;
	}

	//Recursive
	//if return true, child was deleted and tree needs to be balanced
	//otherwise, return false
	private boolean remove(Node n, int key){
		if(n==null)			//key not found
			return false;

		if(n.children[0].isLeafNode()){	//reached the penultimate level
			for(int i=0; i<n.children.length; i++){
				if(n.children[i]!=null && n.children[i].key==key){

					n.children[i] = null;
					n.shiftLeft(i);

					//if root is empty, delete it
					if(n==root && n.children[0]==null){
						root=null;
					}
					n.updateKeys();

					return true;			//a node was deleted
				}
			}
			n.updateKeys();
			return false;					//no node was deleted
		}

		//tree nodes
		else{
			boolean nodeWasDeleted = false;	//was a node deleted? Needed to rebalance tree
			int i;	//index of subtree of deletion

			//search the left and middle subtrees
			for(i=0; i<=1; i++)
				if(key < n.keys[i+1]){	//if key is less than (i+1)'th key, continue down the tree!
					nodeWasDeleted = remove(n.children[i], key);
					break;
				}

			//search the right subtree
			if(key >= n.keys[2]){
				i=2;
				nodeWasDeleted = remove(n.children[i], key);
			}

			n.updateKeys();
			
			//if a node was deleted and we only have one child left...
			if(nodeWasDeleted){
				//Case 2: Borrow: child node has 2 children, sibling has three
				//Case 3: Donate: child node has 2 children, sibling has two, parent has three
				//Case 4: Give Up: child node has 2 children, sibling has two, parent has two. 
					//Donate, then give the problem to the parent to fix

				if(n.children[0].children[1]==null){		//leftmost child has only one child
					//Borrow
					if(n.children[1].children[2]!=null){							//right sibling has three children
						n.children[0].children[1] = n.children[1].children[0];
						n.children[1].children[0] = null;
						n.children[1].shiftLeft(0);

						n.updateKeys();
						for(int x=0; x<n.children.length;x++)
							if(n.children[x]!=null)
								n.children[x].updateKeys();
						nodeWasDeleted = false;
					}
					
					//Donate
					else if(n.children[1].children[2]==null){//right sibling has two children
						n.children[1].shiftRight(0);
						n.children[1].children[0] = n.children[0].children[0];

						n.children[0] = n.children[1];
						n.children[1] = null;
						n.shiftLeft(1);

						n.updateKeys();
						for(int x=0; x<n.children.length;x++)
							if(n.children[x]!=null)
								n.children[x].updateKeys();

						//Give Up
						if(n.children[1]==null){//parent has only one child
							if(n==root){
								n.children[1].shiftRight(0);
								n.children[1].children[0] = n.children[0].children[0];

								//since the middle child now contains all keys, it is the new root
								root = n.children[1];
								
								n.updateKeys();
								for(int x=0; x<n.children.length;x++)
									if(n.children[x]!=null)
										n.children[x].updateKeys();
								nodeWasDeleted = false;
							}
							else{
								n.updateKeys();
							}
						}
						else nodeWasDeleted = true;
					}
				}
				else if(n.children[1].children[1]==null){	//midle child has only one child
					//Borrow (from left)
					if(n.children[0].children[2]!=null){								//left sibling has three children
						n.children[1].shiftRight(0);
						n.children[1].children[0] = n.children[0].children[2];
						n.children[0].children[2] = null;

						n.updateKeys();
						for(int x=0; x<n.children.length;x++)
							if(n.children[x]!=null)
								n.children[x].updateKeys();
						nodeWasDeleted = false;
					}
					//Borrow (from right)
					else if(n.children[2]!=null && n.children[2].children[2]!=null){	//right sibling exists and has three children
						n.children[1].children[1] = n.children[2].children[0];
						n.children[2].children[0] = null;
						n.children[2].shiftLeft(0);

						n.updateKeys();
						for(int x=0; x<n.children.length;x++)
							if(n.children[x]!=null)
								n.children[x].updateKeys();
						nodeWasDeleted = false;
					}
					//Donate (to left)
					else if(n.children[0].children[2]==null){	//left sibling has two children
						n.children[0].children[2] = n.children[1].children[0];
						n.children[1] = null;
						n.shiftLeft(1);

						n.updateKeys();
						for(int x=0; x<n.children.length;x++)
							if(n.children[x]!=null)
								n.children[x].updateKeys();

						//Give Up
						if(n.children[1]==null){	//parent has only one child
							if(n==root){
								//since the left child now contains all keys, it is the new root
								root = n.children[0];
								root.updateKeys();
								nodeWasDeleted = false;
							}
							else{
								n.updateKeys();
							}
						}
						else nodeWasDeleted = false;
					}
				}
				else if(n.children[2]!=null && n.children[2].children[1]==null){	//rightmost child has only one child
					//Borrow
					if(n.children[1].children[2]!=null){	//left sibling has three children
						n.children[2].shiftRight(0);
						n.children[2].children[0] = n.children[1].children[2];
						n.children[1].children[2] = null;

						n.updateKeys();
						for(int x=0; x<n.children.length;x++)
							if(n.children[x]!=null)
								n.children[x].updateKeys();
						nodeWasDeleted = false;
					}
					//Donate
					else{									//left sibling has two children
						n.children[1].children[2] = n.children[2].children[0];
						n.children[2] = null;

						n.updateKeys();
						for(int x=0; x<n.children.length;x++)
							if(n.children[x]!=null)
								n.children[x].updateKeys();
						nodeWasDeleted = false;
					}
				}
			}
			n.updateKeys();
			return nodeWasDeleted;
		}
	}

	void keyOrderList(){
		keyOrderList(root);
	}
	
	private void keyOrderList(Node n){
		if(n==null)	return;

		//only print leaf nodes
		if(n.isLeafNode())	n.print();	
		else for(int c=0; c<n.children.length; c++)	//this is not a C++ pun
			keyOrderList(n.children[c]);
	}
	
	void bfsList(){
		if(root == null){
			System.out.print("Empty tree");
			return;
		}

		int prev = 0;	//used to keep track of where to insert new line

		Node n = root;
		n.print();
		n.prev = 0;

		Queue<Node> q = new LinkedList<Node>();

		//print every tree node
		while(!n.isLeafNode()){
			//add the previous ndoe's children to the queue
			for(int c=0; c<n.children.length; c++){
				if(n.children[c]!=null){
					q.offer(n.children[c]);
					n.children[c].prev = prev + 1;
				}
			}

			//go to the next node
			n = q.poll();

			//printi (and new line if we are at a new line)
			if(n.prev > prev)
				System.out.println();
			n.print();
			prev = n.prev;
		}

		//print the leaf nodes
		while(q.peek()!=null){
			n = q.poll();
			n.print();
		}
	}
	
	int numberOfKeys(){
		return numberOfKeys(root);
	}

	private int numberOfKeys(Node n){
		if(n==null)			return 0;
		if(n.isLeafNode)	return 1;

		return numberOfKeys(n.children[0]) + numberOfKeys(n.children[1]) + numberOfKeys(n.children[2]);
	}

	int height(){
		int h = 0;
		Node n = root;
		Node next = n.children[0];
		while(!next.isLeafNode()){
			h++;
			n = next;
			next = n.children[0];
		}
		h++;	//one more increment, for the leaf node level
		return h;
	}
}

//key 0 is smallest child in left subtree (INVISIBLE)
//key 1 is smallest child in middle subtree
//key 2 is smallest child in right subtree
class Node{
	//shared variables
	boolean isLeafNode;
	int prev;
	
	//Tree Node variables
	int keys[];			//4 slots. The first three are used, though the first is hidden. the last is for splits
	Node children[];	//4 slots. The first three are used. The last is for splits

	//Leaf Node variables
	int key = Integer.MAX_VALUE;


	//tree constructor
	//nonempty k, c represents split tree node
	//otherwise k, n = null
	Node(int[] k, Node[] c){
		isLeafNode = false;
		keys = k;
		children = c;
		key = k[0];
	}

	//leaf constructor
	Node(int k){
		isLeafNode = true;
		key = k;
	}


	//methods

	void updateKeys(){
		for(int k=0; k < keys.length; k++)
			if(children[k]!=null)
				keys[k] = children[k].findMinKey();
			else
				keys[k] = Integer.MAX_VALUE;
	}

	//shifts children right
	//called before an insert at the i'th position
	void shiftRight(int i){
		for(int j=children.length-1; j>i; j--)
			children[j] = children[j-1];
	}

	//shifts children left
	//called after a deletion at the i'th position
	void shiftLeft(int i){
		for(int j=i; j<children.length-1; j++)
			children[j] = children[j+1];
		children[children.length-1] = null;
	}

	boolean isLeafNode(){
		return isLeafNode;
	}

	int findMinKey(){
		if(isLeafNode)
			return key;
		else
			return children[0].findMinKey();
	}

	boolean search(int k){
		//leaf node
		if(isLeafNode){
			if(k == key)
				return true;	//found it!
			else
				return false;	//key not found
		}

		//tree node
		else{
			//if smaller than the smallest key, it will not be in the tree. Return null
			if(k < keys[0]){
				return children[0].search(k);
			}

			//Search the left and middle subtree
			for(int i=1; i<=2; i++){	//check the middle and right key
				if(children[i-1]==null){		//if no subtree to the left of the key, return null
					return false;
				}
				if(k < keys[i])				//if less than key[i], it might be in the i-1th subtree
					return children[i-1].search(k);
			}

			//Larger or equal to the right key. Search the right subtree
			if(children[2]==null){		//if no subtree to the left of the key, return null
				return false;
			}
			else
				return children[2].search(k);
		}
	}

	void print(){
		if(!isLeafNode){
			if(children[1]==null)
				System.out.print("(-,-) ");
			else if(children[2]==null)
				System.out.print("(" + keys[1] + ",-) ");
			else
				System.out.print("(" + keys[1] + "," + keys[2] + ") ");
		}
		else	
			System.out.print(key + " ");
	}
}