package x.mvmn.radawatch.service.analyze;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import x.mvmn.radawatch.model.Entity;

public class TitlesAnalyzisHelper {

	public static interface StringDisplay<T> {
		public String getStringDisplay(T item);
	}

	public static interface TreeNode<T extends Entity> {
		public boolean isLeaf();

		public List<TreeNode<T>> getChildren();

		public T getValue();
	}

	public static class LeafTreeNode<T extends Entity> implements TreeNode<T> {
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
		public List<TreeNode<T>> getChildren() {
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

	public static class AgregableGroupTreeNode<T extends Entity> implements TreeNode<T> {

		protected String key;
		protected List<TreeNode<T>> children = new ArrayList<TreeNode<T>>();
		protected Map<String, AgregableGroupTreeNode<T>> subgroupsMap = new HashMap<String, AgregableGroupTreeNode<T>>();
		protected int deepChildrenCount = 0;
		protected AgregableGroupTreeNode<T> parentGroup = null;

		public AgregableGroupTreeNode(final AgregableGroupTreeNode<T> parentGroup, final String groupName) {
			this.parentGroup = parentGroup;
			this.key = groupName;
		}

		@Override
		public boolean isLeaf() {
			return false;
		}

		@Override
		public List<TreeNode<T>> getChildren() {
			return children;
		}

		public void addLeaf(final LeafTreeNode<T> leafNode) {
			children.add(leafNode);
			increaseDeepChildrenCount();
		}

		public void increaseDeepChildrenCount() {
			this.deepChildrenCount++;
			if (this.parentGroup != null) {
				this.parentGroup.increaseDeepChildrenCount();
			}
		}

		public AgregableGroupTreeNode<T> getOrCreateSubgroup(final String key) {
			AgregableGroupTreeNode<T> treeNode = subgroupsMap.get(key);
			if (treeNode == null) {
				treeNode = new AgregableGroupTreeNode<T>(this, key);
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
				this.setKey(this.getKey() + " " + soleSubgroup.getKey());
			}
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String toString() {
			return key + " [" + deepChildrenCount + "]";
		}

		public int getDeepChildrenCount() {
			return deepChildrenCount;
		}

		@Override
		public T getValue() {
			return null;
		}
	}

	public static <T extends Entity> AgregableGroupTreeNode<T> mapTitlesToTreeNodes(final Iterable<T> items, StringDisplay<T> display) {
		final AgregableGroupTreeNode<T> rootNode = new AgregableGroupTreeNode<T>(null, "Всі");
		for (T item : items) {
			AgregableGroupTreeNode<T> currentNode = rootNode;
			String title = display.getStringDisplay(item);
			for (String val : title.split(" ")) {
				currentNode = currentNode.getOrCreateSubgroup(val);
			}
			currentNode.addLeaf(new LeafTreeNode<T>(item, display));
		}

		rootNode.flatten();

		return rootNode;
	}
}
