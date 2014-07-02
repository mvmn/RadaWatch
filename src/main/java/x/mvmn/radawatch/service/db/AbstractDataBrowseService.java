package x.mvmn.radawatch.service.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.h2.util.JdbcUtils;

import x.mvmn.lang.collections.CollectionsHelper;
import x.mvmn.radawatch.model.Entity;

public abstract class AbstractDataBrowseService<T extends Entity> extends AbstractDBDataReadService implements DataBrowseService<T> {

	public AbstractDataBrowseService(final DataBaseConnectionService dbService) {
		super(dbService);
	}

	protected abstract String getShortColumnsList();

	protected abstract T resultSetRowToEntity(final ResultSet resultSet) throws Exception;

	@Override
	public boolean supportsDateFilter() {
		return getDateColumnName() != null;
	}

	@Override
	public boolean supportsTitleFilter() {
		return getTitleColumnName() != null;
	}

	@Override
	public T fetchItem(int itemDbId) throws Exception {
		return queryForItem("select * from " + getTableName() + " where " + getIdColumnName() + " = " + itemDbId);
	}

	@Override
	public int countItems(int parentItemDbId, DataBrowseQuery query) throws Exception {
		return dbService.execSelectOneInt("select count(*) from (select * from "
				+ getTableName()
				+ " "
				+ query.generateWhereClause(getTitleColumnName(), getDateColumnName(), parentItemDbId > -1 ? getParentIdColumnName() + "=" + parentItemDbId
						: null) + " " + query.generateLimitClause() + ")");
	}

	@Override
	public List<T> fetchItems(int parentItemDbId, DataBrowseQuery query) throws Exception {
		return queryForItems("select "
				+ getShortColumnsList()
				+ " from "
				+ getTableName()
				+ " "
				+ query.generateWhereClause(getTitleColumnName(), getDateColumnName(), parentItemDbId > -1 ? getParentIdColumnName() + "=" + parentItemDbId
						: null) + " ORDER BY " + getDateColumnName() + " DESC " + query.generateLimitClause());
	}

	@Override
	public List<T> fetchItems(int parentItemDbId, DataBrowseQuery query, boolean fetchFullData) throws Exception {
		return queryForItems("select * from "
				+ getTableName()
				+ " "
				+ query.generateWhereClause(getTitleColumnName(), getDateColumnName(), parentItemDbId > -1 ? getParentIdColumnName() + "=" + parentItemDbId
						: null) + " ORDER BY " + getIdColumnName() + " DESC " + query.generateLimitClause());
	}

	protected T queryForItem(final String query) throws Exception {
		return CollectionsHelper.getFirst(queryForItems(query));
	}

	protected List<T> queryForItems(final String query) throws Exception {
		List<T> result = new ArrayList<T>();
		Connection connection = null;
		Statement statement = null;
		try {
			connection = dbService.getConnection();
			statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				result.add(resultSetRowToEntity(resultSet));
			}
			JdbcUtils.closeSilently(resultSet);
		} finally {
			JdbcUtils.closeSilently(statement);
			JdbcUtils.closeSilently(connection);
		}
		return result;
	}
}
