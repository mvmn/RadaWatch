package x.mvmn.radawatch.service.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.TreeMap;
import java.util.List;
import java.util.Map;

import org.h2.util.JdbcUtils;

public abstract class AbstractDataAggregationService extends AbstractDBDataReadService implements DataAggregationService {

	public static final String FIELDNAME_TOTAL_COUNT = "agg_totalcount";
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
	public Map<Date, Map<String, Integer>> getAggregatedCounts(final List<String> metrics, final AggregationInterval aggregationInterval) throws Exception {
		return getAggregatedCounts(metrics, aggregationInterval, null);
	}

	@Override
	public Map<Date, Map<String, Integer>> getAggregatedCounts(final List<String> metrics, final AggregationInterval aggregationInterval,
			final DataBrowseQuery filters) throws Exception {
		return getAggregatedCounts(metrics, aggregationInterval, filters, -1);
	}

	@Override
	public Map<Date, Map<String, Integer>> getAggregatedCounts(final List<String> metrics, final AggregationInterval aggregationInterval,
			final DataBrowseQuery filters, final int parentItemDbId) throws Exception {
		Map<Date, Map<String, Integer>> result = new TreeMap<Date, Map<String, Integer>>();
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = dbService.getConnection();
			stmt = conn.createStatement();
			final String queryStr = buildQuery(metrics, aggregationInterval, filters, parentItemDbId);
			ResultSet resultSet = stmt.executeQuery(queryStr);
			while (resultSet.next()) {
				final Date date = figureOutDateFromIntervalParts(resultSet, aggregationInterval);
				final Map<String, Integer> metricsValues = new TreeMap<String, Integer>();
				result.put(date, metricsValues);
				for (final String metricName : getAvailableMetrics()) {
					metricsValues.put(metricName, resultSet.getInt(sqlProperName(metricName)));
				}
				metricsValues.put("", resultSet.getInt(FIELDNAME_TOTAL_COUNT));
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

		calendar.set(year > -1 ? year : 0, month > -1 ? (month - 1) : 0, day > -1 ? day : 1, hour > -1 ? hour : 0, 0, 0);
		if (week > -1) {
			calendar.set(Calendar.WEEK_OF_YEAR, week);
		}

		return calendar.getTime();
	}

	protected String sqlProperName(final String name) {
		return name.replaceAll("[^A-Za-z0-9_]", "_");
	}

	protected String buildQuery(final List<String> metrics, final AggregationInterval aggregationInterval, final DataBrowseQuery filters,
			final int parentItemDbId) {
		StringBuilder query = new StringBuilder("select count(*) as ").append(FIELDNAME_TOTAL_COUNT);

		if (metrics != null) {
			for (final String metricName : metrics) {
				final String columnConditionDef = metricNameToColumnDef(metricName);
				query.append(", count(").append(columnConditionDef).append(") as ").append(sqlProperName(metricName));
			}
		}

		StringBuilder aggregationIntervalsDefs = new StringBuilder();
		StringBuilder aggregationIntervalsGroupingDefs = new StringBuilder();
		switch (aggregationInterval) {
			case HOUR:
				aggregationIntervalsDefs.append(", HOUR(").append(getDateColumnName()).append(") as ").append(FIELDNAME_INTERVAL_HOUR);
				aggregationIntervalsGroupingDefs.append(", HOUR(").append(getDateColumnName()).append(")");
			case DAY:
				aggregationIntervalsDefs.append(", DAY_OF_MONTH(").append(getDateColumnName()).append(") as ").append(FIELDNAME_INTERVAL_DAY);
				aggregationIntervalsGroupingDefs.append(", DAY_OF_MONTH(").append(getDateColumnName()).append(")");
			case WEEK:
				aggregationIntervalsDefs.append(", WEEK(").append(getDateColumnName()).append(") as ").append(FIELDNAME_INTERVAL_WEEK);
				aggregationIntervalsGroupingDefs.append(", WEEK(").append(getDateColumnName()).append(")");
			case MONTH:
				aggregationIntervalsDefs.append(", MONTH(").append(getDateColumnName()).append(") as ").append(FIELDNAME_INTERVAL_MONTH);
				aggregationIntervalsGroupingDefs.append(", MONTH(").append(getDateColumnName()).append(")");
			case QUARTER:
				aggregationIntervalsDefs.append(", QUARTER(").append(getDateColumnName()).append(") as ").append(FIELDNAME_INTERVAL_QUARTER);
				aggregationIntervalsGroupingDefs.append(", QUARTER(").append(getDateColumnName()).append(")");
			default:
			case YEAR:
				aggregationIntervalsDefs.append(", YEAR(").append(getDateColumnName()).append(") as ").append(FIELDNAME_INTERVAL_YEAR);
				aggregationIntervalsGroupingDefs.append(", YEAR(").append(getDateColumnName()).append(")");
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

			if (filters != null) {
				query.append(filters.generateLimitClause());
			}
		}

		return query.toString();
	}
}
