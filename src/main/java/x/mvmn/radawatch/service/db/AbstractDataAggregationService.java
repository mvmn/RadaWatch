package x.mvmn.radawatch.service.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.h2.util.JdbcUtils;

public abstract class AbstractDataAggregationService extends AbstractDBDataReadService implements DataAggregationService {

	// public static final String FIELDNAME_TOTAL_COUNT = "agg_totalcount";
	public static final String FIELDNAME_INTERVAL_HOUR = "ivl_hour";
	public static final String FIELDNAME_INTERVAL_DAY = "ivl_day";
	public static final String FIELDNAME_INTERVAL_WEEK = "ivl_week";
	public static final String FIELDNAME_INTERVAL_MONTH = "ivl_month";
	public static final String FIELDNAME_INTERVAL_QUARTER = "ivl_quarter";
	public static final String FIELDNAME_INTERVAL_YEAR = "ivl_year";

	public AbstractDataAggregationService(final DataBaseConnectionService dbService) {
		super(dbService);
	}

	protected abstract String metricNameToColumnDef(final String metricName);

	@Override
	public Map<String, Map<Date, Map<String, Integer>>> getAggregatedCounts(final List<String> metrics, final AggregationInterval aggregationInterval)
			throws Exception {
		return getAggregatedCounts(metrics, aggregationInterval, null);
	}

	@Override
	public Map<String, Map<Date, Map<String, Integer>>> getAggregatedCounts(final List<String> metrics, final AggregationInterval aggregationInterval,
			final DataBrowseQuery filters) throws Exception {
		return getAggregatedCounts(metrics, aggregationInterval, filters, -1);
	}

	@Override
	public Map<String, Map<Date, Map<String, Integer>>> getAggregatedCounts(final List<String> metrics, final AggregationInterval aggregationInterval,
			final DataBrowseQuery filters, final int parentItemDbId) throws Exception {
		if (metrics == null || metrics.size() < 1) {
			return null;
		}
		final Map<String, Map<Date, Map<String, Integer>>> result = new TreeMap<String, Map<Date, Map<String, Integer>>>();
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = dbService.getConnection();
			stmt = conn.createStatement();
			final String queryStr = buildQuery(metrics, aggregationInterval, filters, parentItemDbId);
			ResultSet resultSet = stmt.executeQuery(queryStr);
			while (resultSet.next()) {
				final Date date = figureOutDateFromIntervalParts(resultSet, aggregationInterval);
				final StringBuilder otherAggregations = new StringBuilder();
				for (final String aggregationField : getAdditionalAggregations().split(",")) {
					final String fieldName = aggregationField.trim();
					if (fieldName.length() > 0) {
						otherAggregations.append(fieldName).append("=").append(resultSet.getString(fieldName)).append("; ");
					}
				}
				String aggregationDisplayName = otherAggregations.toString().trim();
				if (aggregationDisplayName.length() > 0) {
					aggregationDisplayName = " " + aggregationDisplayName;
				}
				Map<Date, Map<String, Integer>> perDateMap = result.get(aggregationDisplayName);
				if (perDateMap == null) {
					perDateMap = new TreeMap<Date, Map<String, Integer>>();
					result.put(aggregationDisplayName, perDateMap);
				}
				final Map<String, Integer> metricsValues = new TreeMap<String, Integer>();
				perDateMap.put(date, metricsValues);
				for (final String metricName : metrics) {
					metricsValues.put(metricName, resultSet.getInt(sqlProperName(metricName)));
				}
				// metricsValues.put("", resultSet.getInt(FIELDNAME_TOTAL_COUNT));
			}
			resultSet.close();
		} finally {
			JdbcUtils.closeSilently(stmt);
			JdbcUtils.closeSilently(conn);
		}
		return result;
	}

	private Date figureOutDateFromIntervalParts(final ResultSet resultSet, final AggregationInterval aggregationInterval) throws Exception {
		Calendar calendar = Calendar.getInstance();
		calendar.setFirstDayOfWeek(Calendar.MONDAY);

		int year = -1;
		int month = -1;
		int day = -1;
		int hour = -1;
		int week = -1;
		int quarter = -1;
		switch (aggregationInterval) {
			case HOUR:
				hour = resultSet.getInt(FIELDNAME_INTERVAL_HOUR);
			case DAY:
				day = resultSet.getInt(FIELDNAME_INTERVAL_DAY);
			case WEEK:
				if (day == -1) {
					week = resultSet.getInt(FIELDNAME_INTERVAL_WEEK);
				}
			case MONTH:
				month = resultSet.getInt(FIELDNAME_INTERVAL_MONTH);
			case QUARTER:
				if (month == -1) {
					quarter = resultSet.getInt(FIELDNAME_INTERVAL_QUARTER);
					month = (quarter - 1) * 3 + 1;
				}
			default:
			case YEAR:
				year = resultSet.getInt(FIELDNAME_INTERVAL_YEAR);
		}

		calendar.set(Calendar.YEAR, year > -1 ? year : 0);
		calendar.set(Calendar.MONTH, month > -1 ? month - 1 : 0);
		calendar.set(Calendar.DAY_OF_MONTH, day > -1 ? day : 1);
		calendar.set(Calendar.HOUR_OF_DAY, hour > -1 ? hour : 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.getTime();
		if (week > -1) {
			calendar.set(Calendar.WEEK_OF_YEAR, week);
			calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		}

		return calendar.getTime();
	}

	protected String sqlProperName(final String name) {
		return name.replaceAll("[^A-Za-z0-9_]", "_").toLowerCase();
	}

	protected String buildQuery(final List<String> metrics, final AggregationInterval aggregationInterval, final DataBrowseQuery filters,
			final int parentItemDbId) {
		StringBuilder query = new StringBuilder("select ").append(getAdditionalAggregations());// count(*) as ").append(FIELDNAME_TOTAL_COUNT)

		if (metrics != null) {
			boolean first = getAdditionalAggregations().trim().length() == 0;
			for (final String metricName : metrics) {
				final String columnAggregationDef = metricNameToColumnDef(metricName);
				if (first) {
					first = false;
				} else {
					query.append(", ");
				}
				query.append(columnAggregationDef).append(" as ").append(sqlProperName(metricName));
			}
		}

		StringBuilder aggregationIntervalsDefs = new StringBuilder();
		StringBuilder aggregationIntervalsGroupingDefs = new StringBuilder();
		StringBuilder aggregationIntervalsOrder = new StringBuilder();
		switch (aggregationInterval) {
			case HOUR:
				aggregationIntervalsDefs.append(", HOUR(").append(getDateColumnName()).append(") as ").append(FIELDNAME_INTERVAL_HOUR);
				aggregationIntervalsGroupingDefs.append(", HOUR(").append(getDateColumnName()).append(")");
				aggregationIntervalsOrder.append(",").append(FIELDNAME_INTERVAL_HOUR);
			case DAY:
				aggregationIntervalsDefs.append(", DAY_OF_MONTH(").append(getDateColumnName()).append(") as ").append(FIELDNAME_INTERVAL_DAY);
				aggregationIntervalsGroupingDefs.append(", DAY_OF_MONTH(").append(getDateColumnName()).append(")");
				aggregationIntervalsOrder.append(",").append(FIELDNAME_INTERVAL_DAY);
			case WEEK:
				aggregationIntervalsDefs.append(", WEEK(").append(getDateColumnName()).append(") as ").append(FIELDNAME_INTERVAL_WEEK);
				aggregationIntervalsGroupingDefs.append(", WEEK(").append(getDateColumnName()).append(")");
				aggregationIntervalsOrder.append(",").append(FIELDNAME_INTERVAL_WEEK);
			case MONTH:
				aggregationIntervalsDefs.append(", MONTH(").append(getDateColumnName()).append(") as ").append(FIELDNAME_INTERVAL_MONTH);
				aggregationIntervalsGroupingDefs.append(", MONTH(").append(getDateColumnName()).append(")");
				aggregationIntervalsOrder.append(",").append(FIELDNAME_INTERVAL_MONTH);
			case QUARTER:
				aggregationIntervalsDefs.append(", QUARTER(").append(getDateColumnName()).append(") as ").append(FIELDNAME_INTERVAL_QUARTER);
				aggregationIntervalsGroupingDefs.append(", QUARTER(").append(getDateColumnName()).append(")");
				aggregationIntervalsOrder.append(",").append(FIELDNAME_INTERVAL_QUARTER);
			default:
			case YEAR:
				aggregationIntervalsDefs.append(", YEAR(").append(getDateColumnName()).append(") as ").append(FIELDNAME_INTERVAL_YEAR);
				aggregationIntervalsGroupingDefs.append(", YEAR(").append(getDateColumnName()).append(")");
				aggregationIntervalsOrder.append(",").append(FIELDNAME_INTERVAL_YEAR);
		}

		if (getAdditionalAggregations().trim().length() > 0) {
			aggregationIntervalsGroupingDefs.append(", ").append(getAdditionalAggregations());
		}

		query.append(aggregationIntervalsDefs.toString());

		query.append(" from ").append(getTableName()).append(" ");
		if (filters != null) {
			String additionalClauses;
			if (parentItemDbId > -1 && getParentIdColumnName() != null) {
				additionalClauses = getParentIdColumnName() + " = " + parentItemDbId;
			} else {
				additionalClauses = null;
			}
			if (filters != null) {
				query.append(filters.generateWhereClause(getTitleColumnName(), getDateColumnName(), additionalClauses)).append(" ");
			}
			query.append(" group by ").append(aggregationIntervalsGroupingDefs.toString().substring(1));
			query.append(" order by ").append(aggregationIntervalsOrder.toString().substring(1));
			if (filters != null) {
				query.append(filters.generateLimitClause());
			}
		}

		return query.toString();
	}

	public String getAdditionalAggregations() {
		return "";
	}
}
