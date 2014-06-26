package x.mvmn.radawatch.service.db.presdecrees;

import java.util.Collections;
import java.util.List;

import x.mvmn.radawatch.service.db.AbstractDataAggregationService;
import x.mvmn.radawatch.service.db.DataBaseConnectionService;

public class PresidentialDecreesAggregationService extends AbstractDataAggregationService {

	public PresidentialDecreesAggregationService(final DataBaseConnectionService dbService) {
		super(dbService);
	}

	@Override
	public List<String> getSupportedMetrics() {
		return Collections.emptyList();
	}

	@Override
	protected String metricNameToColumnDef(String metricName) {
		return null;
	}

	@Override
	protected String getTableName() {
		return " presidentialdecree ";
	}

	@Override
	protected String getTitleColumnName() {
		return " title ";
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
