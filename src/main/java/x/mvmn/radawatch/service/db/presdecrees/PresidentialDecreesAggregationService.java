package x.mvmn.radawatch.service.db.presdecrees;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import x.mvmn.radawatch.service.db.AbstractDataAggregationService;
import x.mvmn.radawatch.service.db.DataBaseConnectionService;

public class PresidentialDecreesAggregationService extends AbstractDataAggregationService {

	public PresidentialDecreesAggregationService(final DataBaseConnectionService dbService) {
		super(dbService);
	}

	private static String[] SEARCH_PHRASE_COLUMNS = new String[] { " title ", " completetext " };

	private static final List<String> SUPPORTED_METRICS;
	static {
		List<String> supportedMetrics = new ArrayList<String>(1);
		supportedMetrics.add("Count");
		SUPPORTED_METRICS = Collections.unmodifiableList(supportedMetrics);
	}

	@Override
	public List<String> getSupportedMetrics() {
		return SUPPORTED_METRICS;
	}

	@Override
	protected String metricNameToColumnDef(String metricName) {
		return " count(*) ";
	}

	@Override
	protected String getTableName() {
		return " presidentialdecree ";
	}

	@Override
	protected String[] getSearchPhraseColumnNames() {
		return SEARCH_PHRASE_COLUMNS;
	}

	@Override
	protected String getDateColumnName() {
		return " reldate ";
	}

	@Override
	protected String getParentIdColumnName() {
		return null;
	}

	@Override
	public boolean supportsDateFilter() {
		return true;
	}

	@Override
	public boolean supportsTitleFilter() {
		return true;
	}

}
