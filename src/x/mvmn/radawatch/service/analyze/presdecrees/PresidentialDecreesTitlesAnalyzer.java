package x.mvmn.radawatch.service.analyze.presdecrees;

import java.sql.Date;
import java.util.List;

import x.mvmn.radawatch.service.analyze.TitlesAnalyzer;
import x.mvmn.radawatch.service.db.DataBaseConnectionService;

public class PresidentialDecreesTitlesAnalyzer implements TitlesAnalyzer {
	protected final DataBaseConnectionService storageService;

	public PresidentialDecreesTitlesAnalyzer(final DataBaseConnectionService storageService) {
		this.storageService = storageService;
	}

	public List<String> getTitles(final java.util.Date fromDate, final java.util.Date toDate, final String titleFilter) throws Exception {
		return getTitles(utilDateToSqlDate(fromDate), utilDateToSqlDate(toDate), true, titleFilter);
	}

	public int getCount(final Date fromDate, final Date toDate, final String titleFilter) throws Exception {
		return storageService.execSelectCount("select count(*) from presidentialdecree " + getQueryConditions(fromDate, toDate, null, null, titleFilter));
	}

	public int getCount(final Integer offset, final Integer count) throws Exception {
		return storageService.execSelectCount("select count(*) from (select * from presidentialdecree " + generateLimitCondition(offset, count) + ")");
	}

	public List<String> getTitles(final Date fromDate, final Date toDate, final String titleFilter) throws Exception {
		return getTitles(fromDate, toDate, true, titleFilter);
	}

	public List<String> getTitles(final Date fromDate, final Date toDate, final boolean appendDates, final String titleFilter) throws Exception {
		return storageService.execSelectOneColumn("select " + getQueryColumns() + getQueryConditions(fromDate, toDate, null, null, titleFilter));
	}

	public List<String> getTitles(final Integer offset, final Integer count, final String titleFilter) throws Exception {
		return getTitles(offset, count, true, titleFilter);
	}

	public List<String> getTitles(final Integer offset, final Integer count, final boolean appendDates, final String titleFilter) throws Exception {
		return storageService.execSelectOneColumn("select " + getQueryColumns() + getQueryConditions(null, null, offset, count, titleFilter));
	}

	protected String getQueryConditions(final Date fromDate, final Date toDate, final Integer offset, final Integer count, final String titleFilter) {
		return " from presidentialdecree " + generateWhereClause(fromDate, toDate, titleFilter) + " " + generateLimitCondition(offset, count);
	}

	protected String generateWhereClause(final Date fromDate, final Date toDate, final String titleFilter) {
		String whereCondition = "";
		String titleCondition = (titleFilter != null && titleFilter.trim().length() > 0 && !titleFilter.trim().equals("%")) ? " title like '%"
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
		return " concat(decreetype,': ', title, '. /', reldate,'/ ', numcode) ";
	}

	protected String generateDateConditions(final Date fromDate, final Date toDate) {
		String result = "";
		if (fromDate != null) {
			result += " reldate>='" + fromDate.toString() + "' ";
			if (toDate != null) {
				result += " AND ";
			}
		}
		if (toDate != null) {
			result += " reldate<'" + toDate.toString() + "' ";
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
}
