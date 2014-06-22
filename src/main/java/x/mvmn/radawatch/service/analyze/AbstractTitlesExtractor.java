package x.mvmn.radawatch.service.analyze;

import java.util.List;

import x.mvmn.radawatch.service.db.DataBaseConnectionService;
import x.mvmn.radawatch.service.db.DataBrowseQuery;

public abstract class AbstractTitlesExtractor implements TitlesExtractor {

	protected final DataBaseConnectionService dbConnectionService;

	public AbstractTitlesExtractor(final DataBaseConnectionService dbConnectionService) {
		this.dbConnectionService = dbConnectionService;
	}

	protected abstract String getQueryColumns();

	protected abstract String getTableName();

	protected abstract String getTitleColumnName();

	protected abstract String getDateColumnName();

	@Override
	public List<String> getTitles(DataBrowseQuery query) throws Exception {
		return dbConnectionService.execSelectOneColumn("select " + getQueryColumns() + " from " + getTableName() + " "
				+ query.generateWhereClause(getTitleColumnName(), getDateColumnName()) + " " + query.generateLimitClause());
	}

	@Override
	public int getCount(DataBrowseQuery query) throws Exception {
		return dbConnectionService.execSelectCount("select count(*) from (select * from " + getTableName() + " "
				+ query.generateWhereClause(getTitleColumnName(), getDateColumnName()) + " " + query.generateLimitClause() + ")");
	}
}
