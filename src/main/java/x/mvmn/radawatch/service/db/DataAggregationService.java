package x.mvmn.radawatch.service.db;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface DataAggregationService {

	public static enum AggregationInterval {
		YEAR("yyyy"), QUARTER("yyyy-MM"), MONTH("yyyy-MM"), WEEK("yyyy-MM-dd"), DAY("yyyy-MM-dd"), HOUR("yyyy-MM-dd HH");

		private SimpleDateFormat dateFormat;

		AggregationInterval(final String dateFormat) {
			this.dateFormat = new SimpleDateFormat(dateFormat);
		}

		public DateFormat getDateFormat() {
			return dateFormat;
		}
	}

	public List<String> getSupportedMetrics();

	public Map<String, Map<Date, Map<String, Integer>>> getAggregatedCounts(List<String> metrics, AggregationInterval aggregationInterval) throws Exception;

	public Map<String, Map<Date, Map<String, Integer>>> getAggregatedCounts(List<String> metrics, AggregationInterval aggregationInterval,
			DataBrowseQuery filters) throws Exception;

	public Map<String, Map<Date, Map<String, Integer>>> getAggregatedCounts(List<String> metrics, AggregationInterval aggregationInterval,
			DataBrowseQuery filters, int parentItemDbId) throws Exception;

	public boolean supportsDateFilter();

	public boolean supportsTitleFilter();
}
