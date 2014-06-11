package x.mvmn.radawatch.service.analyze;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TitlesAnalyzisHelper {

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

	@SuppressWarnings("unchecked")
	protected static void flattenMap(final Map<String, Object> pp, final String ppkey, final Map<String, Object> parent) {
		for (Map.Entry<String, Object> entry : parent.entrySet())
			if (entry.getValue() instanceof Map) {
				flattenMap(parent, entry.getKey(), (Map<String, Object>) entry.getValue());
			}
		if (pp != null) {
			Map<String, Object> node = null;
			String nodeKey = null;
			for (Map.Entry<String, Object> entry : parent.entrySet())
				if (entry.getValue() instanceof Map) {
					node = (Map<String, Object>) entry.getValue();
					nodeKey = entry.getKey();
				}
			if (node != null && node.get("__count").equals(parent.get("__count"))) {
				pp.remove(ppkey);
				pp.put(ppkey + " " + nodeKey, node);
			}
		}
	}

	public static Map<String, Object> mapTitles(final List<String> titles) {
		final Map<String, Object> result = new ConcurrentHashMap<String, Object>();
		for (String title : titles) {
			Map<String, Object> map = result;

			for (String val : title.split(" ")) {
				map = mapWords(map, val);
			}
		}
		flattenMap(null, null, result);
		return result;
	}
}
