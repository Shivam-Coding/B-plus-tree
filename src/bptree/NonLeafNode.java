package bptree;

/**
 * The {@code NonLeafNode} class implements non-leaf nodes in a B+-tree.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class NonLeafNode<K extends Comparable<K>> extends Node<K> {

	/**
	 * Constructs a {@code NonLeafNode}.
	 * 
	 * @param degree
	 *            the degree of the {@code NonLeafNode}.
	 */
	public NonLeafNode(int degree) {
		super(degree);
	}

	/**
	 * Copy-constructs a {@code NonLeafNode}.
	 * 
	 * @param node
	 *            the other {@code NonLeafNode} to copy from.
	 */
	public NonLeafNode(NonLeafNode<K> node) {
		super(node);
	}

	/**
	 * Inserts the specified key and {@code Node} after the specified child {@code Node}.
	 * 
	 * @param key
	 *            the key to insert.
	 * @param node
	 *            the {@code Node} to insert.
	 * @param child
	 *            the {@code Node} after which the key and the specified {@code Node} will be inserted.
	 */
	protected void insertAfter(K key, Node<K> node, Node<K> child) {
		int i = numberOfKeys;
		while (pointers[i] != child) {
			keys[i] = keys[i - 1];
			pointers[i + 1] = pointers[i];
			i--;
		}
		keys[i] = key;
		pointers[i + 1] = node;
		numberOfKeys++;
	}

	/**
	 * Returns the child {@code Node} at the specified index.
	 * 
	 * @param i
	 *            the index of the child {@code Node}.
	 * @return the child {@code Node} at the specified index.
	 */
	@SuppressWarnings("unchecked")
	public Node<K> child(int i) {
		return (Node<K>) pointers[i];
	}

	/**
	 * Returns the child {@code Node} that is responsible for the specified key.
	 * 
	 * @param key
	 *            a key.
	 * @return the child {@code Node} that is responsible for the specified key.
	 */
	@SuppressWarnings("unchecked")
	public Node<K> child(K key) {
		int i = findIndexGE(key); // find smallest i such that keys[i] >= key
		if (i < 0) { // if no i such that keys[i] >= key
			return (Node<K>) pointers[numberOfKeys];
		} else if (key.compareTo(keys[i]) == 0) {
			return (Node<K>) pointers[i + 1];
		} else {
			return (Node<K>) pointers[i];
		}
	}

	/**
	 * Copies the specified keys and their pointers of the specified {@code NonLeafNode} into this {@code NonLeafNode}.
	 * 
	 * @param node
	 *            a {@code NonLeafNode}.
	 * @param beginIndex
	 *            the beginning index of the keys, inclusive.
	 * @param endIndex
	 *            the ending index of the keys, exclusive.
	 */
	public void copy(NonLeafNode<K> node, int beginIndex, int endIndex) {
		super.copy(node, beginIndex, endIndex);
		this.pointers[numberOfKeys] = node.pointers[numberOfKeys + beginIndex];
	}

}
