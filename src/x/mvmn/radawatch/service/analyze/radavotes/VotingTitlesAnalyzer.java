package x.mvmn.radawatch.service.analyze.radavotes;

import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import x.mvmn.radawatch.service.db.DataBaseConnectionService;

public class VotingTitlesAnalyzer {
	protected final DataBaseConnectionService storageService;

	public VotingTitlesAnalyzer(final DataBaseConnectionService storageService) {
		this.storageService = storageService;
	}

	public List<String> getVotingTitles(final java.util.Date fromDate, final java.util.Date toDate, final String titleFilter) throws Exception {
		return getVotingTitles(utilDateToSqlDate(fromDate), utilDateToSqlDate(toDate), true, titleFilter);
	}

	public int getCount(final Date fromDate, final Date toDate, final String titleFilter) throws Exception {
		return storageService.execSelectCount("select count(*) from votesession " + getQueryConditions(fromDate, toDate, null, null, titleFilter));
	}

	public int getCount(final Integer offset, final Integer count) throws Exception {
		return storageService.execSelectCount("select count(*) from (select * from votesession " + generateLimitCondition(offset, count) + ")");
	}

	public List<String> getVotingTitles(final Date fromDate, final Date toDate, final String titleFilter) throws Exception {
		return getVotingTitles(fromDate, toDate, true, titleFilter);
	}

	public List<String> getVotingTitles(final Date fromDate, final Date toDate, final boolean appendDates, final String titleFilter) throws Exception {
		return storageService.execSelectOneColumn("select " + getQueryColumns() + getQueryConditions(fromDate, toDate, null, null, titleFilter));
	}

	public List<String> getVotingTitles(final Integer offset, final Integer count, final String titleFilter) throws Exception {
		return getVotingTitles(offset, count, true, titleFilter);
	}

	public List<String> getVotingTitles(final Integer offset, final Integer count, final boolean appendDates, final String titleFilter) throws Exception {
		return storageService.execSelectOneColumn("select " + getQueryColumns() + getQueryConditions(null, null, offset, count, titleFilter));
	}

	protected String getQueryConditions(final Date fromDate, final Date toDate, final Integer offset, final Integer count, final String titleFilter) {
		return " from votesession " + generateWhereClause(fromDate, toDate, titleFilter) + " " + generateLimitCondition(offset, count);
	}

	protected String generateWhereClause(final Date fromDate, final Date toDate, final String titleFilter) {
		String whereCondition = "";
		String titleCondition = (titleFilter != null && titleFilter.trim().length() > 0 && !titleFilter.trim().equals("%")) ? " votetitle like '%"
				+ titleFilter.trim().replaceAll("'", "''") + "%' " : null;
		String dateConditions = generateDateConditions(fromDate, toDate);
		if (dateConditions != null && dateConditions.trim().length() > 0) {
			whereCondition = " WHERE " + dateConditions;
		}
		if (titleCondition != null && titleCondition.trim().length() > 0) {
			if (whereCondition.trim().length() > 0) {
				whereCondition += " AND ";
			} else {
				whereCondition = " WHERE ";
			}
			whereCondition += titleCondition;
		}
		return whereCondition;
	}

	protected String getQueryColumns() {
		return " concat(votetitle, '. /', votedate,'/', case votepassed when true then ' <прийнято>' else ' <НЕ прийнято>' end case, ' ', votedyes,'/',votedno,' (',total,')') ";
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

	protected Date utilDateToSqlDate(final java.util.Date utillDate) {
		final Date result;
		if (utillDate != null) {
			result = new Date(utillDate.getTime());
		} else {
			result = null;
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	protected Map<String, Object> getMap(final Map<String, Object> map, final String key) {
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
	protected void remap(final Map<String, Object> pp, final String ppkey, final Map<String, Object> parent) {
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

	public Map<String, Object> mapTitles(final List<String> titles) {
		final Map<String, Object> result = new ConcurrentHashMap<String, Object>();
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
