package util;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;

import bptree.BPlusTree;
import bptree.Node;
import bptree.NonLeafNode;
import bptree.LeafNode;

/**
 * A BPlusTreeVisualizer can display a collection of BPlusTrees, one at a time.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 * 
 */
public class BPlusTreeVisualizer extends util.ZoomView implements java.awt.event.KeyListener {

	/**
	 * Automatically generated serial version ID.
	 */
	private static final long serialVersionUID = -3741562432319457809L;

	/**
	 * A collection of BPlusTrees to visualize.
	 */
	protected java.util.Vector<util.Pair<BPlusTree<Integer, String>, String>> trees;

	/**
	 * The position of the tree shown in the collection of trees.
	 */
	protected int currentFrame = 0;

	/**
	 * The width on the display for each key.
	 */
	protected static int keyWidth = 50;

	/**
	 * The height on the display for each key.
	 */
	protected static int keyHeight = 20;

	/**
	 * Constructs a BPlusTreeVisualizer.
	 * 
	 * @param trees
	 *            a collection of BPlusTrees to visualize.
	 */
	public BPlusTreeVisualizer(java.util.Vector<util.Pair<BPlusTree<Integer, String>, String>> trees) {
		this.trees = trees;
		addKeyListener(this); // This class has its own key listeners.
		setFocusable(true); // Allow panel to get focus
		zoomGraphics.setMinXY(-keyWidth, -2 * keyHeight);
	}

	/**
	 * Draws the specified node on the screen.
	 * 
	 * @param node
	 *            the node to display.
	 * @param level
	 *            the level of the tree.
	 * @param leafNodes
	 *            the number of known leaf nodes.
	 * @param degree
	 *            the degree of the tree.
	 * @return the number of known leaf nodes and the location of the node on the screen.
	 */
	@SuppressWarnings("unchecked")
	protected util.Pair<Integer, Integer> draw(Node<Integer> node, int level, int leafNodes, int degree) {
		if (node == null)
			return null;
		Integer[] childrenPos = null;
		int x = (leafNodes) * keyWidth * (degree);
		int y = (level - 1) * 2 * keyHeight;
		if (node instanceof NonLeafNode) {
			int minX = Integer.MAX_VALUE;
			int maxX = Integer.MIN_VALUE;
			childrenPos = new Integer[degree];
			for (int i = 0; i < degree; i++) {
				Node<Integer> child = ((NonLeafNode<Integer>) node).child(i);
				if (child instanceof Node) {
					util.Pair<Integer, Integer> widthPos = draw((Node<Integer>) child, level + 1, leafNodes, degree);
					leafNodes = widthPos.getFirst();
					childrenPos[i] = widthPos.getSecond();
					if (child instanceof LeafNode)
						leafNodes++;
					minX = Math.min(minX, childrenPos[i]);
					maxX = Math.max(maxX, childrenPos[i]);
				}
			}
			x = (minX + maxX) / 2;
		} else { // if leaf node
			if (((LeafNode<Integer, String>) node).successor() != null) // if there is a next leaf node
				zoomGraphics.drawLine(x + keyWidth * (degree - 1), y + keyHeight / 2, x + keyWidth * (degree) - 3, y
						+ keyHeight / 2);
		}
		zoomGraphics.setColor(Color.WHITE);
		zoomGraphics.fillRect(x - 3, y, keyWidth * (degree - 1) + 6, keyHeight);
		for (int i = 0; i < degree; i++) {
			zoomGraphics.setColor(Color.GRAY);
			zoomGraphics.fillRect(x + i * keyWidth - 3, y, 6, keyHeight); // draw a separator between two keys
			zoomGraphics.setColor(Color.BLACK);
			if (childrenPos != null && childrenPos[i] != null) { // draw a line to the child
				zoomGraphics.drawLine(x + i * keyWidth, y + keyHeight - 4,
						childrenPos[i] + keyWidth * (degree - 1) / 2, level * 2 * keyHeight);
			}
			if (i < degree - 1 && node.key(i) != null)
				zoomGraphics.drawStrings(new String[] { node.key(i).toString() }, x + i * keyWidth + 4, y,
						keyWidth - 6, keyHeight);
		}
		zoomGraphics.drawRect(x - 3, y, keyWidth * (degree - 1) + 6, keyHeight);
		return new util.Pair<Integer, Integer>(leafNodes, x);
	}

	@Override
	public void draw() {
		try {
			BPlusTree<Integer, String> tree = trees.elementAt(currentFrame).getFirst();
			zoomGraphics.setColor(Color.BLACK);
			draw(tree.root(), 1, 0, tree.degree());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void draw(java.awt.Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		super.draw(g);
		g.drawString("" + currentFrame + " : " + trees.elementAt(currentFrame).getSecond(), 10, g.getFontMetrics()
				.getHeight());
	}

	/**
	 * The main program.
	 * 
	 * @param args
	 *            the String arguments
	 * @throws Exception
	 *             if an error occurs.
	 */
	public static void main(String[] args) throws Exception {
		startVisualizer(3, "input.txt");
	}

	public static void startVisualizer(int degree, String inputFile) throws IOException {
		BPlusTree<Integer, String> tree = new BPlusTree<Integer, String>(degree);
		java.util.Vector<util.Pair<BPlusTree<Integer, String>, String>> trees = new java.util.Vector<util.Pair<BPlusTree<Integer, String>, String>>();
		java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(inputFile));
		String line = "";
		try {
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
				String[] command = line.split(" ");
				if (command[0].equals("insert")) {
					tree.insert(Integer.parseInt(command[1]), null);
				} else if (command[0].equals("delete")) {
					tree.delete(Integer.parseInt(command[1]), null);
				}
				trees.add(new util.Pair<BPlusTree<Integer, String>, String>(new BPlusTree<Integer, String>(tree), line));
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("last input line: " + line);
		} finally {
			reader.close();
		}
		JFrame frame = new JFrame("B+Tree Visualizer (degree: " + degree + ", input: " + inputFile + ")");
		JPanel panel = new BPlusTreeVisualizer(trees);
		frame.getContentPane().add(panel);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setVisible(true);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		switch (key) {
		case KeyEvent.VK_LEFT:
			if (e.isControlDown()) {
				zoomGraphics.moveBy(keyWidth, 0);
			} else {
				currentFrame = Math.max(0, currentFrame - 1);
			}
			repaint();
			break;
		case KeyEvent.VK_RIGHT:
			if (e.isControlDown()) {
				zoomGraphics.moveBy(-keyWidth, 0);
			} else {
				currentFrame = Math.min(trees.size() - 1, currentFrame + 1);
			}
			repaint();
			break;
		case KeyEvent.VK_UP: // zoom in
			if (e.isControlDown()) {
				zoomGraphics.moveBy(0, keyHeight);
			} else {
				zoomGraphics.changeScale(1.2);
			}
			repaint();
			break;
		case KeyEvent.VK_DOWN:
			if (e.isControlDown()) {
				zoomGraphics.moveBy(0, -keyHeight);
			} else {
				zoomGraphics.changeScale(1 / 1.2);
			}
			repaint();
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

}
