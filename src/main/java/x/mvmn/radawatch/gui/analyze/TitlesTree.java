package x.mvmn.radawatch.gui.analyze;

import java.awt.BorderLayout;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import x.mvmn.radawatch.model.Entity;
import x.mvmn.radawatch.service.analyze.TitlesAnalyzisHelper.TreeNode;

public class TitlesTree<T extends Entity> extends JPanel {
	private static final long serialVersionUID = -3566611786628177595L;

	protected final JTree treeComponent;

	public TitlesTree(final TreeNode<T> rootTextNode) {
		super(new BorderLayout());
		treeComponent = new JTree(new TreeModel() {

			private final Set<TreeModelListener> modelListeners = Collections.newSetFromMap(new ConcurrentHashMap<TreeModelListener, Boolean>());

			@Override
			public void valueForPathChanged(TreePath path, Object newValue) {
				for (TreeModelListener listener : modelListeners) {
					listener.treeNodesChanged(new TreeModelEvent(newValue, path));
				}
			}

			@Override
			public void removeTreeModelListener(TreeModelListener l) {
				modelListeners.remove(l);
			}

			@Override
			public boolean isLeaf(Object node) {
				@SuppressWarnings("unchecked")
				TreeNode<T> textNode = ((TreeNode<T>) node);
				return !(textNode.getChildren() != null && textNode.getChildren().size() > 0);
			}

			@Override
			public Object getRoot() {
				return rootTextNode;
			}

			@Override
			public int getIndexOfChild(Object parent, Object child) {
				int result = -1;
				@SuppressWarnings("unchecked")
				final TreeNode<T> parentNode = ((TreeNode<T>) parent);
				int index = 0;
				for (TreeNode<T> childEntry : parentNode.getChildren()) {
					if (childEntry == child) {
						result = index;
						break;
					}
					index++;
				}

				return result;
			}

			@Override
			public int getChildCount(Object parent) {
				@SuppressWarnings("unchecked")
				final TreeNode<T> textNode = ((TreeNode<T>) parent);
				return textNode.getChildren() != null ? textNode.getChildren().size() : 0;
			}

			@Override
			public Object getChild(Object parent, int index) {
				@SuppressWarnings("unchecked")
				final TreeNode<T> parentNode = ((TreeNode<T>) parent);
				TreeNode<T> result = null;
				Iterator<TreeNode<T>> iter = parentNode.getChildren().iterator();
				for (int i = 0; i < index; i++) {
					iter.next();
				}
				result = iter.next();
				return result;
			}

			@Override
			public void addTreeModelListener(TreeModelListener l) {
				modelListeners.add(l);
			}
		});
		this.add(treeComponent, BorderLayout.CENTER);
	}

	public JTree getTreeComponent() {
		return treeComponent;
	}
}
