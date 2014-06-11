package x.mvmn.radawatch.service.analyze.radavotes;

import java.sql.Date;
import java.util.List;

import x.mvmn.radawatch.service.analyze.TitlesAnalyzer;
import x.mvmn.radawatch.service.db.DataBaseConnectionService;

public class VotingTitlesAnalyzer implements TitlesAnalyzer {
	protected final DataBaseConnectionService storageService;

	public VotingTitlesAnalyzer(final DataBaseConnectionService storageService) {
		this.storageService = storageService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see x.mvmn.radawatch.service.analyze.radavotes.TitlesAnalyzer#getTitles(java.util.Date, java.util.Date, java.lang.String)
	 */
	public List<String> getTitles(final java.util.Date fromDate, final java.util.Date toDate, final String titleFilter) throws Exception {
		return getTitles(utilDateToSqlDate(fromDate), utilDateToSqlDate(toDate), true, titleFilter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see x.mvmn.radawatch.service.analyze.radavotes.TitlesAnalyzer#getCount(java.sql.Date, java.sql.Date, java.lang.String)
	 */
	public int getCount(final Date fromDate, final Date toDate, final String titleFilter) throws Exception {
		return storageService.execSelectCount("select count(*) from votesession " + getQueryConditions(fromDate, toDate, null, null, titleFilter));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see x.mvmn.radawatch.service.analyze.radavotes.TitlesAnalyzer#getCount(java.lang.Integer, java.lang.Integer)
	 */
	public int getCount(final Integer offset, final Integer count) throws Exception {
		return storageService.execSelectCount("select count(*) from (select * from votesession " + generateLimitCondition(offset, count) + ")");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see x.mvmn.radawatch.service.analyze.radavotes.TitlesAnalyzer#getTitles(java.sql.Date, java.sql.Date, java.lang.String)
	 */
	public List<String> getTitles(final Date fromDate, final Date toDate, final String titleFilter) throws Exception {
		return getTitles(fromDate, toDate, true, titleFilter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see x.mvmn.radawatch.service.analyze.radavotes.TitlesAnalyzer#getTitles(java.sql.Date, java.sql.Date, boolean, java.lang.String)
	 */
	public List<String> getTitles(final Date fromDate, final Date toDate, final boolean appendDates, final String titleFilter) throws Exception {
		return storageService.execSelectOneColumn("select " + getQueryColumns() + getQueryConditions(fromDate, toDate, null, null, titleFilter));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see x.mvmn.radawatch.service.analyze.radavotes.TitlesAnalyzer#getTitles(java.lang.Integer, java.lang.Integer, java.lang.String)
	 */
	public List<String> getTitles(final Integer offset, final Integer count, final String titleFilter) throws Exception {
		return getTitles(offset, count, true, titleFilter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see x.mvmn.radawatch.service.analyze.radavotes.TitlesAnalyzer#getTitles(java.lang.Integer, java.lang.Integer, boolean, java.lang.String)
	 */
	public List<String> getTitles(final Integer offset, final Integer count, final boolean appendDates, final String titleFilter) throws Exception {
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
			result += " votedate<'" + toDate.toString() + "' ";
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
