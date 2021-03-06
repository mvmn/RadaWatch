package x.mvmn.radawatch.service.db.radavotes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import x.mvmn.radawatch.service.db.AbstractDataAggregationService;
import x.mvmn.radawatch.service.db.DataBaseConnectionService;

public class RadaVotesAggregationService extends AbstractDataAggregationService {

	private static String[] SEARCH_PHRASE_COLUMNS = new String[] { " votetitle " };

	public RadaVotesAggregationService(DataBaseConnectionService dbService) {
		super(dbService);
	}

	private static final List<String> AVAILABLE_METRICS;
	private static final Map<String, String> METRICS_TO_SQL;
	static {
		List<String> availableMetrics = new ArrayList<String>();
		Map<String, String> metricsToSql = new HashMap<String, String>();
		availableMetrics.add("Total votes");
		metricsToSql.put("Total votes", "count(*)");
		availableMetrics.add("Votes passed");
		metricsToSql.put("Votes passed", "sum(votepassed)");
		availableMetrics.add("Votes failed");
		metricsToSql.put("Votes failed", "sum(not votepassed)");
		availableMetrics.add("Average attendance");
		metricsToSql.put("Average attendance", "avg(total)");
		availableMetrics.add("Average attendance %");
		metricsToSql.put("Average attendance %", "avg((total*100)/450)");
		availableMetrics.add("Average for-votes");
		metricsToSql.put("Average for-votes", "avg(votedyes)");
		availableMetrics.add("Average for-votes % of present");
		metricsToSql.put("Average for-votes % of present", "avg((votedyes*100)/total)");
		availableMetrics.add("Average against-votes");
		metricsToSql.put("Average against-votes", "avg(votedno)");
		availableMetrics.add("Average abstained");
		metricsToSql.put("Average abstained", "avg(abstained)");
		availableMetrics.add("Average not voted");
		metricsToSql.put("Average not voted", "avg(skipped)");
		AVAILABLE_METRICS = Collections.unmodifiableList(availableMetrics);
		METRICS_TO_SQL = Collections.unmodifiableMap(metricsToSql);
	}

	@Override
	public List<String> getSupportedMetrics() {
		return AVAILABLE_METRICS;
	}

	@Override
	protected String metricNameToColumnDef(final String metricName) {
		return METRICS_TO_SQL.get(metricName);
	}

	@Override
	public boolean supportsDateFilter() {
		return true;
	}

	@Override
	public boolean supportsTitleFilter() {
		return true;
	}

	@Override
	protected String getParentIdColumnName() {
		return null;
	}

	@Override
	protected String getTableName() {
		return " votesession ";
	}

	@Override
	protected String[] getSearchPhraseColumnNames() {
		return SEARCH_PHRASE_COLUMNS;
	}

	@Override
	protected String getDateColumnName() {
		return " votedate ";
	}
}
