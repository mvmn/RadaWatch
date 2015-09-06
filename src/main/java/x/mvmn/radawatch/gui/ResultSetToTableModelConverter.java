package x.mvmn.radawatch.gui;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import x.mvmn.radawatch.service.db.ResultSetConverter;

public class ResultSetToTableModelConverter implements ResultSetConverter<TableModel> {

	protected String getColumnName(final int columnIndex, final ResultSetMetaData meta) throws Exception {
		return meta.getColumnLabel(columnIndex);
	}

	protected Class<?> getColumnClass(final int columnIndex, final ResultSetMetaData meta, final String columnName) throws Exception {
		return Class.forName(meta.getColumnClassName(columnIndex));
	}

	protected Object getValue(final int columnIndex, final ResultSet resultSet, final String columnName, final Class<?> columnClass) throws Exception {
		return resultSet.getObject(columnIndex);
	}

	@Override
	public TableModel convert(final ResultSet resultSet) throws Exception {
		final ResultSetMetaData meta = resultSet.getMetaData();
		final int columnCount = meta.getColumnCount();
		final String columnNames[] = new String[columnCount];
		final Class<?> columnClasses[] = new Class<?>[columnCount];
		for (int i = 1; i <= columnCount; i++) {
			columnNames[i - 1] = getColumnName(i, meta);
			columnClasses[i - 1] = getColumnClass(i, meta, columnNames[i - 1]);
		}

		final List<Object[]> values = new ArrayList<Object[]>();
		while (resultSet.next()) {
			final Object[] vals = new Object[columnCount];
			for (int i = 1; i <= columnCount; i++) {
				vals[i - 1] = getValue(i, resultSet, columnNames[i - 1], columnClasses[i - 1]);
			}
			values.add(vals);
		}

		final TableModel result = new AbstractTableModel() {
			private static final long serialVersionUID = 5228102893910454894L;

			@Override
			public String getColumnName(final int columnIndex) {
				return columnNames[columnIndex];
			}

			@Override
			public Class<?> getColumnClass(final int columnIndex) {
				return columnClasses[columnIndex];
			}

			@Override
			public Object getValueAt(final int rowIndex, final int columnIndex) {
				return values.get(rowIndex)[columnIndex];
			}

			@Override
			public int getRowCount() {
				return values.size();
			}

			@Override
			public int getColumnCount() {
				return columnCount;
			}
		};
		return result;
	}
}
