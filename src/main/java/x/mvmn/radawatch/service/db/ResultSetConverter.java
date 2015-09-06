package x.mvmn.radawatch.service.db;

import java.sql.ResultSet;

public interface ResultSetConverter<T> {

	public T convert(final ResultSet resultSet) throws Exception;
}
