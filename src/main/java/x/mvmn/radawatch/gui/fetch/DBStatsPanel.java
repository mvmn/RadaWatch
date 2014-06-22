package x.mvmn.radawatch.gui.fetch;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import x.mvmn.lang.tuple.TupleOfTwo;
import x.mvmn.radawatch.model.Entity;
import x.mvmn.radawatch.service.db.DataStorageService;

public class DBStatsPanel<T extends Entity> extends JPanel {

	private static final long serialVersionUID = -4868155195547333955L;

	private static final String[] COL_NAMES = new String[] { "DB Table", "Rows count" };
	protected final DataStorageService<T> storageService;

	protected volatile List<TupleOfTwo<String, String>> currentData = Collections.emptyList();

	protected final JButton btnRefresh = new JButton("Refresh");
	protected final JTable statsTable = new JTable(new AbstractTableModel() {

		private static final long serialVersionUID = -4829570475666950617L;

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return COL_NAMES[columnIndex];
		}

		@Override
		public int getRowCount() {
			return DBStatsPanel.this.getCurrentData().size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			TupleOfTwo<String, String> item = DBStatsPanel.this.getCurrentData().get(rowIndex);
			return columnIndex > 0 ? item.getSecond() : item.getFirst();
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}
	});

	public DBStatsPanel(final DataStorageService<T> storageService) {
		super(new BorderLayout());
		this.setPreferredSize(new Dimension(300, 600));
		this.storageService = storageService;

		btnRefresh.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent actEvent) {
				btnRefresh.setEnabled(false);
				new Thread() {
					public void run() {
						DBStatsPanel.this.refresh();
					}
				}.start();
			}
		});

		this.add(new JScrollPane(statsTable), BorderLayout.CENTER);
		JLabel topLabel = new JLabel("DB stats", JLabel.CENTER);
		topLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		this.add(topLabel, BorderLayout.NORTH);
		this.add(btnRefresh, BorderLayout.SOUTH);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				DBStatsPanel.this.refresh();
			}
		});
	}

	protected List<TupleOfTwo<String, String>> getCurrentData() {
		return currentData;
	}

	public void refresh() {
		try {
			List<TupleOfTwo<String, String>> statsData = new ArrayList<TupleOfTwo<String, String>>();
			Map<String, String> stats = storageService.getStats();
			for (Map.Entry<String, String> statsEntry : stats.entrySet()) {
				statsData.add(new TupleOfTwo<String, String>(statsEntry.getKey(), statsEntry.getValue()));
			}
			Collections.sort(statsData, new Comparator<TupleOfTwo<String, String>>() {
				@Override
				public int compare(TupleOfTwo<String, String> o1, TupleOfTwo<String, String> o2) {
					return o1.getFirst().compareTo(o2.getFirst());
				}
			});
			this.currentData = statsData;
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					btnRefresh.setEnabled(true);
					AbstractTableModel tableModel = (AbstractTableModel) statsTable.getModel();
					tableModel.fireTableStructureChanged();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
