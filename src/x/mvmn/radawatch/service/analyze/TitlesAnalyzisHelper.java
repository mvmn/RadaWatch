package x.mvmn.radawatch.service.analyze;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TitlesAnalyzisHelper {

	public static class TextNode {
		final ConcurrentHashMap<String, TextNode> children = new ConcurrentHashMap<String, TextNode>();
		private int count = 1;
		private String value;

		public TextNode(final String value) {
			this.value = value;
		}

		public TextNode addOrIncrementChild(String value) {
			final String key = value.toLowerCase();
			TextNode result = children.get(key);
			if (result == null) {
				result = new TextNode(value);
				children.put(key, result);
			} else {
				result.setCount(result.getCount() + 1);
			}
			return result;
		}

		public int getCount() {
			return count;
		}

		public void setCount(int count) {
			this.count = count;
		}

		public String getValue() {
			return value;
		}

		public ConcurrentHashMap<String, TextNode> getChildren() {
			return children;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	@SuppressWarnings("unchecked")
	protected static Map<String, Object> mapWords(final Map<String, Object> map, final String key) {
		Map<String, Object> result = (Map<String, Object>) map.get(key);
		if (result == null) {
			result = new ConcurrentHashMap<String, Object>();
			map.put(key, result);
			result.put("__count", 1);
		} else {
			result.put("__count", 1 + (Integer) result.get("__count"));
		}
		return result;
	}

	public static TextNode mapTitlesToTreeNodes(final Iterable<String> titles) {
		final TextNode rootNode = new TextNode("Всі");
		for (String title : titles) {
			TextNode currentNode = rootNode;
			for (String val : title.split(" ")) {
				currentNode = currentNode.addOrIncrementChild(val);
			}
		}

		flattenTree(null, null, rootNode);
		return rootNode;
	}

	public static void flattenTree(final TextNode grandParentNode, final String parentNodeKey, final TextNode parentNode) {
		for (Map.Entry<String, TextNode> childEntry : parentNode.getChildren().entrySet()) {
			flattenTree(parentNode, childEntry.getKey(), childEntry.getValue());
		}
		if (grandParentNode != null && parentNode.getChildren().size() == 1) {
			final Map.Entry<String, TextNode> singleChildEntry = parentNode.getChildren().entrySet().iterator().next();
			final TextNode singleChild = singleChildEntry.getValue();
			final String newKey = parentNodeKey + " " + singleChildEntry.getKey();
			grandParentNode.getChildren().remove(parentNodeKey);
			singleChild.setValue(parentNode.getValue() + " " + singleChild.getValue());
			grandParentNode.getChildren().put(newKey, singleChild);
		}
	}
}
