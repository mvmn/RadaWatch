package x.mvmn.radawatch.service.db;

import java.sql.Date;
import java.util.Arrays;

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

	public String generateWhereClause(final String[] searchPhraseColumnNames, final String dateColumnName) {
		return generateWhereClause(searchPhraseColumnNames, dateColumnName, true);
	}

	public String generateWhereClause(final String[] searchPhraseColumnNames, final String dateColumnName, String... additionalClauses) {
		return generateWhereClause(searchPhraseColumnNames, dateColumnName, true, additionalClauses);
	}

	public String generateWhereClause(final String[] searchPhraseColumnNames, final String dateColumnName, final boolean includeWhereKeyword,
			String... additionalClauses) {
		String titleCondition = null;
		if (searchPhraseColumnNames != null && searchPhrase != null && searchPhrase.trim().length() > 0 && !searchPhrase.trim().equals("%")) {
			final String searchPhraseEscaped = searchPhrase.replaceAll("'", "''");
			titleCondition = " (";
			for (final String searchPhraseColumnName : searchPhraseColumnNames) {
				for (final String searchPhraseToken : searchPhraseEscaped.split("\\|")) {
					titleCondition += " (" + searchPhraseColumnName + " like '%" + searchPhraseToken.trim() + "%') OR ";
				}
			}
			titleCondition += " (0=1)) "; // (x OR y OR ... OR n OR (0=1)) should be same as (x OR y OR ... OR n)
		}
		final String dateConditions = generateDateClause(dateColumnName);
		final String[] allClauses;
		if (additionalClauses != null && additionalClauses.length > 0) {
			allClauses = Arrays.copyOf(additionalClauses, additionalClauses.length + 2);
			allClauses[additionalClauses.length] = titleCondition;
			allClauses[additionalClauses.length + 1] = dateConditions;
		} else {
			allClauses = new String[] { titleCondition, dateConditions };
		}
		return generateWhereFromClauses(includeWhereKeyword, allClauses);
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
		if (dateColumnName != null) {
			if (fromDate != null) {
				result += String.format(" %s>='%s' ", dateColumnName, fromDate.toString());
				if (toDate != null) {
					result += " AND ";
				}
			}
			if (toDate != null) {
				result += String.format(" %s<'%s' ", dateColumnName, toDate.toString());
			}
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