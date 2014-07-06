package x.mvmn.radawatch.service.db.radavotes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import x.mvmn.radawatch.service.db.AbstractDataAggregationService;
import x.mvmn.radawatch.service.db.DataBaseConnectionService;

public class RadaIndividualVotesAggregationService extends AbstractDataAggregationService {

	public RadaIndividualVotesAggregationService(final DataBaseConnectionService dbService) {
		super(dbService);
	}

	private static final List<String> AVAILABLE_METRICS;
	private static final Map<String, String> METRICS_TO_SQL;
	static {
		List<String> availableMetrics = new ArrayList<String>();
		Map<String, String> metricsToSql = new HashMap<String, String>();
		availableMetrics.add("Total votes");
		metricsToSql.put("Total votes", "count(*)");
		availableMetrics.add("Absent");
		metricsToSql.put("Absent", "sum(voted=5)");
		availableMetrics.add("Present %");
		metricsToSql.put("Present %", "case count(*) when 0 then 0 else sum(voted!=5)*100/count(*) end case");
		availableMetrics.add("For-votes");
		metricsToSql.put("For-votes", "sum(voted=1)");
		availableMetrics.add("For-votes % of present");
		metricsToSql.put("For-votes % of present", "case (sum(voted!=5)) when 0 then 0 else sum(voted=1)*100/sum(voted!=5) end case");
		availableMetrics.add("Against-votes");
		metricsToSql.put("Against-votes", "sum(voted=2)");
		availableMetrics.add("Abstained");
		metricsToSql.put("Abstained", "sum(voted=3)");
		availableMetrics.add("Not voted");
		metricsToSql.put("Not voted", "sum(voted=2)");
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
		return " individualvote left join votesessionfaction on votesessionfaction.id = individualvote.votesessionfactionid left join votesession on votesession.id = votesessionfaction.votesessionid ";
	}

	@Override
	protected String getParentIdColumnName() {
		return " votesessionfactionid ";
	}

	@Override
	protected String getTitleColumnName() {
		return " name ";
	}

	@Override
	protected String getDateColumnName() {
		return " votedate ";
	}

	@Override
	protected String getAdditionalAggregations() {
		return " name ";
	}
}
