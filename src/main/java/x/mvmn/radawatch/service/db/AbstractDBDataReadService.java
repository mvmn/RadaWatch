package x.mvmn.radawatch.service.db;

public abstract class AbstractDBDataReadService {

	protected final DataBaseConnectionService dbService;

	public AbstractDBDataReadService(final DataBaseConnectionService dbService) {
		this.dbService = dbService;
	}

	protected abstract String getTableName();

	protected String getIdColumnName() {
		return "id";
	}

	protected abstract String getParentIdColumnName();

	protected abstract String getTitleColumnName();

	protected abstract String getDateColumnName();

}