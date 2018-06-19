package bptree;

/**
 * The {@code Node} class implements nodes that constitute a B+-tree.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public abstract class Node<K extends Comparable<K>> {

	/**
	 * The number of keys that this {@code Node} currently maintains.
	 */
	protected int numberOfKeys;

	/**
	 * The keys that this {@code Node} maintains.
	 */
	protected K[] keys;

	/**
	 * The pointers that this {@code Node} maintains.
	 */
	protected Object[] pointers;

	/**
	 * Constructs a {@code Node}.
	 * 
	 * @param degree
	 *            the degree of the {@code Node}.
	 */
	@SuppressWarnings("unchecked")
	public Node(int degree) {
		numberOfKeys = 0;
		keys = (K[]) new Comparable[degree - 1];
		pointers = new Object[degree];
	}

	/**
	 * Copy-constructs a {@code Node}.
	 * 
	 * @param node
	 *            the other {@code Node} to copy from.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Node(Node<K> node) {
		this.numberOfKeys = node.numberOfKeys;
		keys = (K[]) new Comparable[node.keys.length];
		System.arraycopy(node.keys, 0, keys, 0, node.keys.length);
		pointers = new Object[node.pointers.length];
		for (int i = 0; i < node.pointers.length; i++) {
			Object pointer = node.pointers[i];
			if (pointer instanceof LeafNode)
				pointers[i] = new LeafNode((LeafNode) pointer); // copy construct the node.
			else if (pointer instanceof NonLeafNode)
				pointers[i] = new NonLeafNode((NonLeafNode) pointer); // copy construct the node.
			else
				pointers[i] = pointer;
		}
	}

	/**
	 * Returns the number of keys in this {@code Node}.
	 * 
	 * @return the number of keys in this {@code Node}.
	 */
	public int numberOfKeys() {
		return this.numberOfKeys;
	}

	/**
	 * Returns the key at the specified index.
	 * 
	 * @param i
	 *            the index of the key.
	 * @return the key at the specified index.
	 */
	public K key(int i) {
		return keys[i];
	}

	/**
	 * Returns the first key of this {@code Node}.
	 * 
	 * @return the first key of this {@code Node}.
	 */
	public K firstKey() {
		return keys[0];
	}

	/**
	 * Clears this {@code Node}.
	 */
	public void clear() {
		numberOfKeys = 0;
		for (int i = 0; i < keys.length; i++)
			keys[i] = null;
		for (int i = 0; i < pointers.length; i++)
			pointers[i] = null;
	}

	/**
	 * Determines whether or not this {@code Node} has room for a new entry.
	 * 
	 * @return {@code true} if this {@code Node} has room for a new {@code Node}; {@code false} otherwise.
	 */
	public boolean hasRoom() {
		return numberOfKeys < keys.length;
	}

	/**
	 * Returns the first index i such that keys[i] >= the given key.
	 * 
	 * @param key
	 *            a key.
	 * @return the first index i such that keys[i] >= the given key; -1 if there is no such i.
	 */
	protected int findIndexGE(K key) {
		for (int i = 0; i < numberOfKeys; i++) {
			if (keys[i].compareTo(key) >= 0)
				return i;
		}
		return -1;
	}

	/**
	 * Returns the largest index i such that keys[i] < the given key.
	 * 
	 * @param key
	 *            a key.
	 * @return the largest index i such that keys[i] < the given key; -1 if there is no such i.
	 */
	protected int findIndexL(K key) {
		for (int i = numberOfKeys - 1; i >= 0; i--) {
			if (keys[i].compareTo(key) < 0)
				return i;
		}
		return -1;
	}

	/**
	 * Inserts the specified key and object at the specified location.
	 * 
	 * @param key
	 *            the key to insert.
	 * @param object
	 *            the object to insert.
	 * @param pos
	 *            the insertion position
	 */
	protected void insert(K key, Object object, int pos) {
		for (int i = numberOfKeys; i > pos; i--) {
			keys[i] = keys[i - 1];
			pointers[i] = pointers[i - 1];
		}
		keys[pos] = key;
		pointers[pos] = object;
		numberOfKeys++;
	}

	/**
	 * Copies the specified keys and their pointers of the specified {@code Node} into this {@code Node}.
	 * 
	 * @param node
	 *            a {@code Node}.
	 * @param beginIndex
	 *            the beginning index of the keys, inclusive.
	 * @param endIndex
	 *            the ending index of the keys, exclusive.
	 */
	public void copy(Node<K> node, int beginIndex, int endIndex) {
		numberOfKeys = 0;
		for (; numberOfKeys < endIndex - beginIndex; numberOfKeys++) {
			this.keys[numberOfKeys] = node.keys[numberOfKeys + beginIndex];
			this.pointers[numberOfKeys] = node.pointers[numberOfKeys + beginIndex];
		}
	}

}
