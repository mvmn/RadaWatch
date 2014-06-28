package x.mvmn.radawatch.gui.analyze;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import x.mvmn.lang.StringDisplay;
import x.mvmn.radawatch.model.Entity;

public class TitlesTree<T extends Entity> extends JTree {
	public static interface TreeNode<T extends Entity> {
		public boolean isLeaf();

		public Collection<TreeNode<T>> getChildren();

		public T getValue();
	}

	public static class LeafTreeNode<T extends Entity> implements TitlesTree.TreeNode<T> {
		protected final T value;
		protected final StringDisplay<T> display;

		public LeafTreeNode(final T value, final StringDisplay<T> display) {
			this.value = value;
			this.display = display;
		}

		@Override
		public boolean isLeaf() {
			return true;
		}

		@Override
		public List<TitlesTree.TreeNode<T>> getChildren() {
			return Collections.emptyList();
		}

		public String toString() {
			return "DB ID: " + value.getDbId();
		}

		public String getDisplayName() {
			return display.getStringDisplay(value);
		}

		public T getValue() {
			return value;
		}
	}

	public static class AgregableGroupTreeNode<T extends Entity> implements TitlesTree.TreeNode<T> {
		protected Comparator<TitlesTree.TreeNode<T>> comparatorOfNodeGroupsByDeepChildCounts = new Comparator<TitlesTree.TreeNode<T>>() {
			@Override
			public int compare(final TreeNode<T> o1, final TreeNode<T> o2) {
				final int val1;
				final int val2;

				if (o1 != null && o1 instanceof AgregableGroupTreeNode) {
					final AgregableGroupTreeNode<T> g1 = (AgregableGroupTreeNode<T>) o1;
					val1 = g1.getDeepChildrenCount();
				} else {
					val1 = -1;
				}
				if (o2 != null && o2 instanceof AgregableGroupTreeNode) {
					final AgregableGroupTreeNode<T> g2 = (AgregableGroupTreeNode<T>) o2;
					val2 = g2.getDeepChildrenCount();
				} else {
					val2 = -1;
				}

				return val2 - val1;
			}
		};

		protected String key;
		protected String groupName;
		protected List<TitlesTree.TreeNode<T>> children = new ArrayList<TitlesTree.TreeNode<T>>();

		protected Map<String, AgregableGroupTreeNode<T>> subgroupsMap = new HashMap<String, AgregableGroupTreeNode<T>>();
		protected int deepChildrenCount = 0;
		protected AgregableGroupTreeNode<T> parentGroup = null;

		public AgregableGroupTreeNode(final AgregableGroupTreeNode<T> parentGroup, final String groupName) {
			this.parentGroup = parentGroup;
			this.groupName = groupName;
			this.key = groupName.toLowerCase();
		}

		@Override
		public boolean isLeaf() {
			return false;
		}

		@Override
		public List<TitlesTree.TreeNode<T>> getChildren() {
			return children;
		}

		public void addLeaf(final TitlesTree.LeafTreeNode<T> leafNode) {
			children.add(leafNode);
			Collections.sort(children, comparatorOfNodeGroupsByDeepChildCounts);
			increaseDeepChildrenCount();
		}

		public void increaseDeepChildrenCount() {
			this.deepChildrenCount++;
			if (this.parentGroup != null) {
				Collections.sort(this.parentGroup.children, comparatorOfNodeGroupsByDeepChildCounts);
				this.parentGroup.increaseDeepChildrenCount();
			}
		}

		public AgregableGroupTreeNode<T> getOrCreateSubgroup(final String groupName) {
			final String key = groupName.toLowerCase();
			AgregableGroupTreeNode<T> treeNode = subgroupsMap.get(key);
			if (treeNode == null) {
				treeNode = new AgregableGroupTreeNode<T>(this, groupName);
				subgroupsMap.put(key, treeNode);
				children.add(treeNode);
			}
			return treeNode;
		}

		public void flatten() {
			for (final AgregableGroupTreeNode<T> subgroup : subgroupsMap.values()) {
				subgroup.flatten();
			}
			if (this.children.size() == 1 && subgroupsMap.size() == 1) {
				final AgregableGroupTreeNode<T> soleSubgroup = subgroupsMap.values().iterator().next();
				this.children = soleSubgroup.getChildren();
				this.subgroupsMap = soleSubgroup.subgroupsMap;
				for (final AgregableGroupTreeNode<T> subgroup : subgroupsMap.values()) {
					subgroup.parentGroup = this;
				}
				this.setGroupName(this.getGroupName() + " " + soleSubgroup.getGroupName());
			}
		}

		public String toString() {
			return groupName + " [" + deepChildrenCount + "]";
		}

		public int getDeepChildrenCount() {
			return deepChildrenCount;
		}

		@Override
		public T getValue() {
			return null;
		}

		public String getGroupName() {
			return groupName;
		}

		public void setGroupName(final String groupName) {
			this.key = groupName.toLowerCase();
			this.groupName = groupName;
		}
	}

	private static final long serialVersionUID = -3566611786628177595L;

	public TitlesTree(final TitlesTree.TreeNode<T> rootTextNode) {
		super(new TreeModel() {

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
				TitlesTree.TreeNode<T> textNode = ((TitlesTree.TreeNode<T>) node);
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
				final TitlesTree.TreeNode<T> parentNode = ((TitlesTree.TreeNode<T>) parent);
				int index = 0;
				for (TitlesTree.TreeNode<T> childEntry : parentNode.getChildren()) {
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
				final TitlesTree.TreeNode<T> textNode = ((TitlesTree.TreeNode<T>) parent);
				return textNode.getChildren() != null ? textNode.getChildren().size() : 0;
			}

			@Override
			public Object getChild(Object parent, int index) {
				@SuppressWarnings("unchecked")
				final TitlesTree.TreeNode<T> parentNode = ((TitlesTree.TreeNode<T>) parent);
				TitlesTree.TreeNode<T> result = null;
				Iterator<TitlesTree.TreeNode<T>> iter = parentNode.getChildren().iterator();
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
	}
}
