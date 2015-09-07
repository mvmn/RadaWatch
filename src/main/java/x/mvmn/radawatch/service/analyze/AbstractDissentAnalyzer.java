package x.mvmn.radawatch.service.analyze;

import x.mvmn.radawatch.service.db.AbstractQueryProcessor;
import x.mvmn.radawatch.service.db.DataBaseConnectionService;
import x.mvmn.radawatch.service.db.ResultSetConverter;

public abstract class AbstractDissentAnalyzer<T> extends AbstractQueryProcessor {

	protected final DataBaseConnectionService dbService;
	protected final ResultSetConverter<T> converter;

	public AbstractDissentAnalyzer(final DataBaseConnectionService dbService, final ResultSetConverter<T> converter) {
		super();
		this.dbService = dbService;
		this.converter = converter;
	}

	@Override
	protected DataBaseConnectionService getDBService() {
		return dbService;
	}
}
