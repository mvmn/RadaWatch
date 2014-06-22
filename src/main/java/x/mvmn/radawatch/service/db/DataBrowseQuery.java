package x.mvmn.radawatch.service.db;

import java.sql.Date;

import x.mvmn.lang.date.DatesHelper;

public class DataBrowseQuery {
	private final String searchPhrase;
	private final Integer offset;
	private final Integer count;
	private final Date fromDate;
	private final Date toDate;

	public DataBrowseQuery(String searchPhrase, Integer offset, Integer count, java.util.Date fromDate, java.util.Date toDate) {
		this(searchPhrase, offset, count, DatesHelper.utilDateToSqlDate(fromDate), DatesHelper.utilDateToSqlDate(toDate));
	}

	public DataBrowseQuery(String searchPhrase, Integer offset, Integer count, Date fromDate, Date toDate) {
		super();
		this.searchPhrase = searchPhrase;
		this.offset = offset;
		this.count = count;
		this.fromDate = fromDate;
		this.toDate = toDate;
	}

	public String getSearchPhrase() {
		return searchPhrase;
	}

	public Integer getOffset() {
		return offset;
	}

	public Integer getCount() {
		return count;
	}

	public Date getFromDate() {
		return fromDate;
	}

	public Date getToDate() {
		return toDate;
	}

	public String generateWhereClause(final String titleColumnName, final String dateColumnName) {
		return generateWhereClause(titleColumnName, dateColumnName, true);
	}

	public String generateWhereClause(final String titleColumnName, final String dateColumnName, final boolean includeWhereKeyword) {
		String titleCondition = (searchPhrase != null && searchPhrase.trim().length() > 0 && !searchPhrase.trim().equals("%")) ? " " + titleColumnName
				+ " like '%" + searchPhrase.trim().replaceAll("'", "''") + "%' " : null;
		String dateConditions = generateDateClause(dateColumnName);
		return generateWhereFromClauses(includeWhereKeyword, titleCondition, dateConditions);
	}

	protected String generateWhereFromClauses(final boolean includeWhereKeyword, String... clauses) {
		String result = "";
		if (clauses != null && clauses.length > 0) {
			boolean someConditionsPresent = false;
			for (final String clause : clauses) {
				if (clause != null && clause.trim().length() > 0) {
					if (someConditionsPresent) {
						result += " AND ";
					} else if (includeWhereKeyword) {
						result = " WHERE ";
					}
					result += clause;
					someConditionsPresent = true;
				}
			}
		}
		return result;
	}

	protected String generateDateClause(final String dateColumnName) {
		String result = "";
		if (fromDate != null) {
			result += String.format(" %s>='%s'", dateColumnName, fromDate.toString());
			if (toDate != null) {
				result += " AND ";
			}
		}
		if (toDate != null) {
			result += String.format(" %s<'%s'", dateColumnName, toDate.toString());
		}
		return result;
	}

	public String generateLimitClause() {
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
}