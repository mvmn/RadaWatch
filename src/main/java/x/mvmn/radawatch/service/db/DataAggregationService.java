package x.mvmn.radawatch.service.db;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface DataAggregationService {

	public static enum AggregationInterval {
		YEAR, QUARTER, MONTH, WEEK, DAY, HOUR
	}

	public List<String> getAvailableMetrics();

	public Map<Date, Map<String, Integer>> getAggregatedCounts(List<String> metrics, AggregationInterval aggregationInterval) throws Exception;

	public Map<Date, Map<String, Integer>> getAggregatedCounts(List<String> metrics, AggregationInterval aggregationInterval, DataBrowseQuery filters)
			throws Exception;

	public Map<Date, Map<String, Integer>> getAggregatedCounts(List<String> metrics, AggregationInterval aggregationInterval, DataBrowseQuery filters,
			int parentItemDbId) throws Exception;

	public boolean supportsDateFilter();

	public boolean supportsTitleFilter();
}
