package x.mvmn.radawatch.service.analyze;

import x.mvmn.lang.StringDisplay;
import x.mvmn.radawatch.gui.analyze.TitlesTree;
import x.mvmn.radawatch.model.Entity;

public class TitlesAnalyzisHelper {

	public static <T extends Entity> TitlesTree.AgregableGroupTreeNode<T> mapItemsByTitlesToTreeNodes(final Iterable<T> items, StringDisplay<T> display) {
		final TitlesTree.AgregableGroupTreeNode<T> rootNode = new TitlesTree.AgregableGroupTreeNode<T>(null, "Всі");
		for (T item : items) {
			TitlesTree.AgregableGroupTreeNode<T> currentNode = rootNode;
			String title = display.getStringDisplay(item);
			for (String val : title.split(" ")) {
				currentNode = currentNode.getOrCreateSubgroup(val);
			}
			currentNode.addLeaf(new TitlesTree.LeafTreeNode<T>(item, display));
		}

		rootNode.flatten();

		return rootNode;
	}
}
