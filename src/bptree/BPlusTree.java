package bptree;

import java.awt.print.Printable;

import com.sun.org.apache.bcel.internal.generic.SWAP;

/**
 * The {@code BPlusTree} class implements B+-trees. Each {@code BPlusTree} stores its elements in the main memory (not
 * on disks) for simplicity.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 * 
 * @param <K>
 *            the type of keys
 * @param <V>
 *            the type of values
 */
public class BPlusTree<K extends Comparable<K>, V> {

	/**
	 * The maximum number of pointers that each {@code Node} of this {@code BPlusTree} can have.
	 */
	protected int degree;

	/**
	 * The root node of this {@code BPlusTree}.
	 */
	protected Node<K> root;

	/**
	 * Constructs a {@code BPlusTree}.
	 * 
	 * @param degree
	 *            the maximum number of pointers that each {@code Node} of this {@code BPlusTree} can have.
	 */
	public BPlusTree(int degree) {
		this.degree = degree;
	}

	/**
	 * Copy-constructs a {@code BPlusTree}.
	 * 
	 * @param tree
	 *            another {@code BPlusTree} to copy from.
	 */
	@SuppressWarnings("unchecked")
	public BPlusTree(BPlusTree<K, V> tree) {
		this.degree = tree.degree;
		if (tree.root instanceof LeafNode)
			this.root = new LeafNode<K, V>((LeafNode<K, V>) tree.root);
		else
			this.root = new NonLeafNode<K>((NonLeafNode<K>) tree.root);
	}

	/**
	 * Returns the degree of this {@code BPlusTree}.
	 * 
	 * @return the degree of this {@code BPlusTree}.
	 */
	public int degree() {
		return degree;
	}

	/**
	 * Returns the root {@code Node} of this {@code BPlusTree}.
	 * 
	 * @return the root {@code Node} of this {@code BPlusTree}.
	 */
	public Node<K> root() {
		return root;
	}

	/**
	 * Finds the {@code LeafNode} in this {@code BPlusTree} that must be responsible for the specified key.
	 * 
	 * @param key
	 *            the search key.
	 * @return the {@code LeafNode} in this {@code BPlusTree} that must be responsible for the specified key.
	 */
	@SuppressWarnings("unchecked")
	public LeafNode<K, V> find(K key) {
		Node<K> c = root;
		while (c instanceof NonLeafNode) {
			c = ((NonLeafNode<K>) c).child(key);
		}
		return (LeafNode<K, V>) c;
	}

	/**
	 * Finds the parent {@code Node} of the specified {@code Node}.
	 * 
	 * @param node
	 *            a {@code Node}.
	 * @return the parent {@code Node} of the specified {@code Node}; {@code null} if the parent cannot be found.
	 */
	public NonLeafNode<K> findParent(Node<K> node) {
		Node<K> p = root;
		while (p != null) {
			K key = node.firstKey();
			Node<K> c = ((NonLeafNode<K>) p).child(key);
			if (c == node) { // if found the parent of the node.
				return (NonLeafNode<K>) p;
			}
			p = c;
		}
		return null;
	}

	/**
	 * Inserts the specified key and the value into this {@code BPlusTree}.
	 * 
	 * @param key
	 *            the key to insert.
	 * @param value
	 *            the value to insert.
	 */
	public void insert(K key, V value) {
		LeafNode<K, V> leaf; // the leaf node where insertion will occur
		if (root == null) { // if the root is null
			leaf = new LeafNode<K, V>(degree);
			root = leaf;
		} else { // if root is not null
			leaf = find(key);
		}
		if (leaf.hasRoom()) { // if the leaf node has room for the new entry
			leaf.insert(key, value);
		} else { // if split is required
			LeafNode<K, V> t = new LeafNode<K, V>(degree + 1); // create a temporary leaf node
			t.copy(leaf, 0, leaf.numberOfKeys());// copy everything to the temporary node
			t.insert(key, value); // insert the key and value to the temporary node
			LeafNode<K, V> nLeaf = new LeafNode<K, V>(degree); // create a new leaf node
			nLeaf.setSuccessor(leaf.successor()); // chaining
			leaf.clear(); // clear the leaf node
			leaf.setSuccessor(nLeaf); // chaining from leaf to nLeaf
			int m = (int) Math.ceil(degree / 2.0); // compute the split point
			leaf.copy(t, 0, m); // put the first half into leaf
			nLeaf.copy(t, m, t.numberOfKeys()); // put the second half to nLeaf
			insertInParent(leaf, nLeaf.firstKey(), nLeaf); // use the first key of nLeaf as the separator.
		}
	}

	/**
	 * Inserts pointers to the specified {@code Node}s into an appropriate parent {@code Node}.
	 * 
	 * @param n
	 *            a {@code Node}.
	 * @param key
	 *            the key that splits the {@code Node}s
	 * @param nn
	 *            a new {@code Node}.
	 */
	void insertInParent(Node<K> n, K key, Node<K> nn) {
		if (n == root) { // if the root was split
			root = new NonLeafNode<K>(degree); // create a new node
			root.insert(key, n, 0); // make the new root point to the nodes.
			root.pointers[1] = nn;
			return;
		}
		NonLeafNode<K> p = findParent(n);
		if (p.hasRoom()) {
			p.insertAfter(key, nn, n); // insert key and nn right after n
		} else { // if split is required
			NonLeafNode<K> t = new NonLeafNode<K>(degree + 1); // crate a temporary node
			t.copy(p, 0, p.numberOfKeys()); // copy everything of p to the temporary node
			t.insertAfter(key, nn, n); // insert key and nn after n
			p.clear(); // clear p
			int m = (int) Math.ceil(degree / 2.0); // compute the split point
			p.copy(t, 0, m - 1);
			NonLeafNode<K> np = new NonLeafNode<K>(degree); // create a new node
			np.copy(t, m, t.numberOfKeys()); // put the second half to np
			insertInParent(p, t.keys[m - 1], np); // use the middle key as the separator
		}
	}

	/**
	 * Deletes the specified key and the value from this {@code BPlusTree}.
	 * 
	 * @param key
	 *            the key to delete.
	 * @param value
	 *            the value to delete.
	 */
	public void delete(K key, V value) {
		// please implement the body of this method so that we can remove key-value pairs from the tree (refer to page
		// 498 in the text book).
		Node<K> leaf;
		leaf = find(key);
		
		deleteEntry(leaf, value, key);
	}

	public void deleteEntry(Node<K> n, V value, K key) {
		
		int count = 0;
//		try{
			Node<K> par = null;
			if(!n.equals(root)){
				
				if((findParent(n))!= null)
				{
					par = findParent(n);
				}	
			}
			
		int i = n.findIndexGE(key);
		if(i!= -1){
		n.keys[i] = null;
//		n.pointers[i] = null;
		n.numberOfKeys-= 1;
		rearrrange(n);
//		System.out.println(" no of keys: "+n.numberOfKeys());
		if(n.equals(root)){
//			System.out.println("in the root  ");
			Node<K> temp = null;
			for(int j= 0;j<degree;j++){
				if(n.pointers[j]!= null){
					count++;
					temp = (Node<K>) n.pointers[j];
				}
			}
			if(count==1){
				root = temp;
			}else{
				for(int z =0; z< degree-1;z++){
					if(root.keys[z]==null){
						try{
						root.keys[z]=root.keys[z+1];
						root.keys[z+1]=null;
						}catch(ArrayIndexOutOfBoundsException a){}
					}
					if(root.pointers[z]==null){
						root.pointers[z]=root.pointers[z+1];
						root.pointers[z+1]=null;
					}
				}
				n = root;
			}
		}else
		{
			
			if(n.numberOfKeys < ((degree-1) / 2.0)){	
				Node<K> n1 = null;
				Boolean predecessor = true;
				K key1 = null;
				for(int j = 0;j<degree;j++){
					if(par.pointers[j].equals(n)){		
						if(j==0 || j < par.numberOfKeys){
							n1 =  (Node<K>) par.pointers[j+1];
							predecessor = true;
							key1 = par.key(j);
							break;							
						}else{
								n1 =  (Node<K>) par.pointers[j-1];
								key1 = par.keys[j-1];
								predecessor = false;
								
							break;
						}
					}
				}
				
				if(n.numberOfKeys + n1.numberOfKeys <= degree-1){
					if(predecessor){
						Node<K> t = n1;
						n1 = n;
						n = t;
					}
					if(n instanceof LeafNode){
						int z = n1.numberOfKeys;
						for(int j = 0;j< n.numberOfKeys;j++){
							n1.keys[z] = n.keys[j];
							n1.numberOfKeys++;
							n1.pointers[z] = n.pointers[j];
							z++;
						}
						n1.pointers[degree-1] = n.pointers[degree-1];
						
					}else{
						int z = n1.numberOfKeys;
						n1.keys[z] = key1;
						n1.numberOfKeys++;
						z++;
						int j;
						for(j = 0;j< n.numberOfKeys;j++){
							n1.keys[z] = n.keys[j];
							n1.numberOfKeys++;
							n1.pointers[z] = n.pointers[j];
							z++;
						}
						n1.pointers[z]=n.pointers[j];
					}
//					Node<K> pa = findParent(n);
					par.pointers[par.findIndexGE(key1)+1]= null;
				
				  deleteEntry(par, value, key1);
						 
				}else{
					if(!predecessor){
						if(n instanceof NonLeafNode ){
							int m = n1.numberOfKeys;
							n.pointers[n.numberOfKeys+1] = n.pointers[n.numberOfKeys];
							for(int j = n.numberOfKeys;j>0;j--){
								 n.keys[j] = n.keys[j-1];
								 n.pointers[j] = n.pointers[j-1];
							}
							n.keys[0] = key1;
							n.pointers[0]= n1.pointers[m];
							n.numberOfKeys++;
							K km = n1.keys[m-1];
							n1.keys[m-1] = null;
							n1.pointers[m] = null;
							n1.numberOfKeys--;
//							Node<K> p = findParent(n);
							par.keys[par.findIndexGE(key1)] = km;
						}else{
							int m = n1.numberOfKeys-1;
							n.pointers[n.numberOfKeys+1] = n.pointers[n.numberOfKeys];
							for(int j = n.numberOfKeys;j>0;j--){
								 n.keys[j] = n.keys[j-1];
								 n.pointers[j] = n.pointers[j-1];
							}
							n.keys[0] = n1.keys[m];
							n.pointers[0]= n1.pointers[m];
							n.numberOfKeys++;
							K km = n1.keys[m];
							n1.keys[m] = null;
							n1.pointers[m] = null;
							n1.numberOfKeys--;
//							Node<K> p = findParent(n);
							par.keys[par.findIndexGE(key1)] = km;
						}
					}else{
						Node<K> t = n;
						n = n1;
						n1 = t;
						if(n instanceof NonLeafNode ){
							int m = n1.numberOfKeys;
							n.pointers[n.numberOfKeys+1] = n.pointers[n.numberOfKeys];
							for(int j = n.numberOfKeys;j>0;j--){
								 n.keys[j] = n.keys[j-1];
								 n.pointers[j] = n.pointers[j-1];
							}
							n.keys[0] = key1;
							n.pointers[0]= n1.pointers[m];
							n.numberOfKeys++;
							K km = n1.keys[m-1];
							n1.keys[m-1] = null;
							n1.pointers[m] = null;
							n1.numberOfKeys--;
//							Node<K> p = findParent(n);
							par.keys[par.findIndexGE(key1)] = km;
						}else{
							int m = n1.numberOfKeys-1;
							n.pointers[n.numberOfKeys+1] = n.pointers[n.numberOfKeys];
							for(int j = n.numberOfKeys;j>0;j--){
								 n.keys[j] = n.keys[j-1];
								 n.pointers[j] = n.pointers[j-1];
							}
							n.keys[0] = n1.keys[m];
							n.pointers[0]= n1.pointers[m];
							n.numberOfKeys++;
							K km = n1.keys[m];
							n1.keys[m] = null;
							n1.pointers[m] = null;
							n1.numberOfKeys--;
//							Node<K> p = findParent(n);
							par.keys[par.findIndexGE(key1)] = km;
						}
						
					}
				}
			}else{
				rearrrange(n);
			}
			
		}
		
			}
			
//		}catch(NullPointerException p){
//			p.printStackTrace();
//			}
	}
	public void rearrrange(Node<K> n){
		if(n instanceof LeafNode){
			for(int z =0; z< degree-1;z++){
				if(n.keys[z]==null){
					try{
					n.keys[z]=n.keys[z+1];
					n.keys[z+1]=null;
					}catch(ArrayIndexOutOfBoundsException a){}
				}
			}
			}else{
				for(int z =0; z< degree-1;z++){
					if(n.keys[z]==null){
						try{
						n.keys[z]=n.keys[z+1];
						n.keys[z+1]=null;
						}catch(ArrayIndexOutOfBoundsException a){}
					}
					if(n.pointers[z]==null){
						n.pointers[z]=n.pointers[z+1];
						n.pointers[z+1]=null;
					}
				}
			}
	}
}
