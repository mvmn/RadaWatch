package x.mvmn.radawatch.service.analyze;

import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import x.mvmn.radawatch.service.db.StorageService;

public class VotingTitlesAnalyzer {
	protected final StorageService storageService;

	public VotingTitlesAnalyzer(final StorageService storageService) {
		this.storageService = storageService;
	}

	public int getCount(final Date fromDate, final Date toDate) throws Exception {
		String whereCondition = "";
		String dateConditions = generateDateConditions(fromDate, toDate);
		if (dateConditions != null && dateConditions.trim().length() > 0) {
			whereCondition = " WHERE " + dateConditions;
		}
		return storageService.execSelectCount("select count(*) from votesession  " + whereCondition);
	}

	public int getCount(final Integer offset, final Integer count) throws Exception {
		return storageService.execSelectCount("select count(*) from (select * from votesession " + generateLimitCondition(offset, count) + ")");
	}

	public List<String> getVotingTitles(final Date fromDate, final Date toDate) throws Exception {
		return getVotingTitles(fromDate, toDate, true);
	}

	public List<String> getVotingTitles(final Date fromDate, final Date toDate, boolean appendDates) throws Exception {
		String whereCondition = "";
		String dateConditions = generateDateConditions(fromDate, toDate);
		if (dateConditions != null && dateConditions.trim().length() > 0) {
			whereCondition = " WHERE " + dateConditions;
		}

		return storageService.execSelectOneColumn("select " + getTitleColumn(appendDates) + " from votesession " + whereCondition);
	}

	public List<String> getVotingTitles(final Integer offset, final Integer count) throws Exception {
		return getVotingTitles(offset, count, true);
	}

	public List<String> getVotingTitles(final Integer offset, final Integer count, boolean appendDates) throws Exception {
		return storageService.execSelectOneColumn("select " + getTitleColumn(appendDates) + " from votesession " + generateLimitCondition(offset, count));
	}

	protected String getTitleColumn(boolean appendDate) {
		if (appendDate) {
			return " concat(votetitle, '. /', votedate,'/') ";
		} else {
			return " votetitle ";
		}
	}

	protected String generateDateConditions(final Date fromDate, final Date toDate) {
		String result = "";
		if (fromDate != null) {
			result += " votedate>='" + fromDate.toString() + "' ";
			if (toDate != null) {
				result += " AND ";
			}
		}
		if (toDate != null) {
			result += " votedate<='" + toDate.toString() + "' ";
		}
		return result;
	}

	protected String generateLimitCondition(final Integer offset, final Integer count) {
		String result = "";
		if (offset != null || count != null) {
			result += " LIMIT ";
			if (offset != null) {
				result += offset.toString();
				if (count != null) {
					result += ",";
				} else {
					result += ",-1";
				}
			}
			if (count != null) {
				result += count.toString();
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	protected Map<String, Object> getMap(Map<String, Object> map, String key) {
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
	protected void remap(Map<String, Object> pp, String ppkey, Map<String, Object> parent) {
		for (Map.Entry<String, Object> entry : parent.entrySet())
			if (entry.getValue() instanceof Map) {
				remap(parent, entry.getKey(), (Map<String, Object>) entry.getValue());
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

	public Map<String, Object> mapTitles(List<String> titles) {
		Map<String, Object> result = new ConcurrentHashMap<String, Object>();
		for (String title : titles) {
			Map<String, Object> map = result;

			for (String val : title.split(" ")) {
				map = getMap(map, val);
			}
		}
		remap(null, null, result);
		return result;
	}

	// public static void main(String args[]) throws Exception {
	// VotingTitlesAnalyzer vta = new VotingTitlesAnalyzer(new StorageService());
	// System.out.println(vta.getCount((Date) null, (Date) null));
	// System.out.println(vta.getCount((Integer) null, (Integer) null));
	// System.out.println(vta.getCount(new Date(114, 01, 01), (Date) null));
	// System.out.println(vta.getCount(null, new Date(114, 01, 01)));
	// System.out.println(vta.getCount(new Date(114, 01, 01), new Date(114, 02, 01)));
	// System.out.println(vta.getCount(2900, null));
	// System.out.println(vta.getCount(null, 10));
	// System.out.println(vta.getVotingTitles(2900, null));
	// System.out.println(vta.getVotingTitles(null, 10));
	// System.out.println(vta.getVotingTitles(null, new Date(113, 1, 1)));
	// System.out.println(vta.mapTitles(vta.getVotingTitles(null, 10)));
	// }
}
