package bptree;

/**
 * The {@code LeafNode} class implements leaf nodes in a B+-tree. {@code LeafNode}s are chained so each {@code LeafNode}
 * except the last {@code LeafNode} has a successor.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class LeafNode<K extends Comparable<K>, V> extends Node<K> {

	/**
	 * Constructs a {@code LeafNode}.
	 * 
	 * @param degree
	 *            the degree of the {@code LeafNode}.
	 */
	public LeafNode(int degree) {
		super(degree);
	}

	/**
	 * Copy-constructs a {@code LeafNode}.
	 * 
	 * @param node
	 *            the other {@code LeafNode} to copy from.
	 */
	public LeafNode(LeafNode<K, V> node) {
		super(node);
	}

	/**
	 * Inserts the specified key and value assuming that this {@code LeafNode} has room for them.
	 * 
	 * @param key
	 *            the key to insert.
	 * @param value
	 *            the value to insert.
	 */
	public void insert(K key, V value) {
		if (numberOfKeys == 0 || key.compareTo(keys[0]) < 0) {
			insert(key, value, 0);
		} else {
			int i = findIndexL(key);
			
			insert(key, value, i + 1);
		}
	}

	/**
	 * Returns the successor of this {@code LeafNode}.
	 * 
	 * @return the successor of this {@code LeafNode}.
	 */
	@SuppressWarnings("unchecked")
	public LeafNode<K, V> successor() {
		return (LeafNode<K, V>) pointers[pointers.length - 1];
	}

	/**
	 * Sets the successor of this {@code LeafNode}.
	 * 
	 * @param successor
	 *            the new successor of this {@code LeafNode}.
	 * @return the previous successor of this {@code LeafNode}.
	 */
	public LeafNode<K, V> setSuccessor(LeafNode<K, V> successor) {
		@SuppressWarnings("unchecked")
		LeafNode<K, V> s = (LeafNode<K, V>) pointers[pointers.length - 1];
		pointers[pointers.length - 1] = successor;
		return s;
	}
}
