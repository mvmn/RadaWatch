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
		List<Map.Entry<String, Object>> entries = new ArrayList<Map.Entry<String, Object>>(map.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<String, Object>>() {
			@Override
			public int compare(Entry<String, Object> m1, Entry<String, Object> m2) {
				int v1 = 0;
				int v2 = 0;
				if (m1.getValue() instanceof java.util.Map) {
					v1 = (Integer) ((Map<String, Object>) m1.getValue()).get("__count");
				}
				if (m2.getValue() instanceof java.util.Map) {
					v2 = (Integer) ((Map<String, Object>) m2.getValue()).get("__count");
				}
				return v2 - v1;
			}
		});

		for (Map.Entry<String, Object> entry : entries) {
			if (entry.getValue() instanceof java.util.Map) {
				String nodeVal = String.format("%s [%s]", entry.getKey(), ((Map<String, Object>) entry.getValue()).get("__count"));
				DefaultMutableTreeNode newNode = new javax.swing.tree.DefaultMutableTreeNode(nodeVal);
				parentNode.add(newNode);
				mapToNodes(newNode, (Map<String, Object>) entry.getValue());
			}
		}
	}
}
