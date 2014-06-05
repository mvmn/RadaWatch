package x.mvmn.radawatch;

import java.awt.BorderLayout;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

public class TitlesTree extends JPanel {
	private static final long serialVersionUID = -3566611786628177595L;

	public TitlesTree(Map<String, Object> titlesMap) {
		super(new BorderLayout());
		DefaultMutableTreeNode rootnode = new DefaultMutableTreeNode("Всі");
		mapToNodes(rootnode, titlesMap);
		this.add(new JTree(rootnode), BorderLayout.CENTER);
	}

	@SuppressWarnings("unchecked")
	public void mapToNodes(DefaultMutableTreeNode parentNode, Map<String, Object> map) {
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			if (entry.getValue() instanceof java.util.Map) {
				String nodeVal = String.format("%s [%s]", entry.getKey(), ((Map<String, Object>) entry.getValue()).get("__count"));
				DefaultMutableTreeNode newNode = new javax.swing.tree.DefaultMutableTreeNode(nodeVal);
				parentNode.add(newNode);
				mapToNodes(newNode, (Map<String, Object>) entry.getValue());
			}
		}
	}
}
