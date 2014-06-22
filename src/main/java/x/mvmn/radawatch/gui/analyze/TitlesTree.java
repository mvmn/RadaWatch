package x.mvmn.radawatch.gui.analyze;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import x.mvmn.radawatch.service.analyze.TitlesAnalyzisHelper.TextNode;

public class TitlesTree extends JPanel {
	private static final long serialVersionUID = -3566611786628177595L;

	public TitlesTree(final Map<String, TextNode> topNodes) {
		super(new BorderLayout());
		DefaultMutableTreeNode rootnode = new DefaultMutableTreeNode("Всі");
		textNodesToTreeNodes(rootnode, topNodes);
		this.add(new JTree(rootnode), BorderLayout.CENTER);
	}

	public void textNodesToTreeNodes(DefaultMutableTreeNode parentNode, Map<String, TextNode> topNodes) {
		List<Map.Entry<String, TextNode>> entries = new ArrayList<Map.Entry<String, TextNode>>(topNodes.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<String, TextNode>>() {
			@Override
			public int compare(Entry<String, TextNode> textNodeEntry1, Entry<String, TextNode> textNodeEntry2) {
				return textNodeEntry2.getValue().getCount() - textNodeEntry1.getValue().getCount();
			}
		});
		for (Map.Entry<String, TextNode> entry : entries) {
			final TextNode textNode = entry.getValue();
			String nodeVal = String.format("%s [%s]", textNode.getValue(), textNode.getCount());
			DefaultMutableTreeNode newNode = new javax.swing.tree.DefaultMutableTreeNode(nodeVal);
			parentNode.add(newNode);
			textNodesToTreeNodes(newNode, textNode.getChildren());
		}
	}
}
