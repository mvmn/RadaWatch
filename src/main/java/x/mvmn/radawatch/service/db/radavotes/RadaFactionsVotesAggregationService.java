package x.mvmn.radawatch.service.db.radavotes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import x.mvmn.radawatch.service.db.AbstractDataAggregationService;
import x.mvmn.radawatch.service.db.DataBaseConnectionService;

public class RadaFactionsVotesAggregationService extends AbstractDataAggregationService {

	public RadaFactionsVotesAggregationService(final DataBaseConnectionService dbService) {
		super(dbService);
	}

	private static final List<String> AVAILABLE_METRICS;
	private static final Map<String, String> METRICS_TO_SQL;
	static {
		List<String> availableMetrics = new ArrayList<String>();
		Map<String, String> metricsToSql = new HashMap<String, String>();
		availableMetrics.add("Total votes");
		metricsToSql.put("Total votes", "count(*)");
		availableMetrics.add("Average total members");
		metricsToSql.put("Average total members", "avg(votesessionfaction.totalmembers)");
		availableMetrics.add("Average absent");
		metricsToSql.put("Average absent", "avg(votesessionfaction.absent)");
		availableMetrics.add("Average present %");
		metricsToSql
				.put("Average present %",
						"case avg(votesessionfaction.totalmembers) when 0 then 0 else avg(((votesessionfaction.totalmembers-votesessionfaction.absent)*100))/avg(votesessionfaction.totalmembers) end case");
		availableMetrics.add("Average for-votes");
		metricsToSql.put("Average for-votes", "avg(votesessionfaction.votedyes)");
		availableMetrics.add("Average for-votes % of present");
		metricsToSql
				.put("Average for-votes % of present",
						"case avg(votesessionfaction.totalmembers-votesessionfaction.absent) when 0 then 0 else avg((votesessionfaction.votedyes*100))/avg(votesessionfaction.totalmembers-votesessionfaction.absent) end case");
		availableMetrics.add("Average against-votes");
		metricsToSql.put("Average against-votes", "avg(votesessionfaction.votedno)");
		availableMetrics.add("Average abstained");
		metricsToSql.put("Average abstained", "avg(votesessionfaction.abstained)");
		availableMetrics.add("Average not voted");
		metricsToSql.put("Average not voted", "avg(votesessionfaction.skipped)");
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
	protected String getTableName() {
		return " votesessionfaction left join votesession on votesession.id = votesessionfaction.votesessionid ";
	}

	@Override
	public String getAdditionalAggregations() {
		return " title ";
	}

	@Override
	protected String getParentIdColumnName() {
		return " votesessionid ";
	}

	@Override
	protected String getTitleColumnName() {
		return " title ";
	}

	@Override
	protected String getDateColumnName() {
		return " votedate ";
	}

}
