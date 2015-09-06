package x.mvmn.radawatch.gui.analyze;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import x.mvmn.radawatch.gui.ResultSetToTableModelConverter;
import x.mvmn.radawatch.model.radavotes.IndividualDeputyVoteData;
import x.mvmn.radawatch.service.analyze.DeputeesDissentAnalyzer;
import x.mvmn.radawatch.service.db.DataBaseConnectionService;
import x.mvmn.radawatch.service.db.DataBrowseQuery;
import x.mvmn.radawatch.swing.DefaultMouseListener;
import x.mvmn.radawatch.swing.SwingHelper;

public class DeputeesDissentPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 7240909984006821063L;
	protected final ExtFilterPanel filterPanel = new ExtFilterPanel(true, true);
	protected final JTable mainTable = new JTable();
	protected final JButton doQuery = new JButton("Run Query");
	protected final JLabel sliderVal = new JLabel("75");
	protected final JSlider sliderPercentage = new JSlider(JSlider.HORIZONTAL, 1, 100, 75);

	protected final MouseListener mainTableMouseListener;

	protected final DeputeesDissentAnalyzer<TableModel> analyzer;

	public DeputeesDissentPanel(final DataBaseConnectionService storageService) {
		super(new BorderLayout());
		this.analyzer = new DeputeesDissentAnalyzer<TableModel>(storageService, new ResultSetToTableModelConverter() {
			@Override
			protected Class<?> getColumnClass(final int columnIndex, final ResultSetMetaData meta, final String columnName) throws Exception {
				if (columnName.equalsIgnoreCase("VoteType")) {
					return String.class;
				} else {
					return super.getColumnClass(columnIndex, meta, columnName);
				}
			}

			@Override
			protected Object getValue(final int columnIndex, final ResultSet resultSet, final String columnName, final Class<?> columnClass) throws Exception {
				if (columnName.equalsIgnoreCase("VoteType")) {
					return IndividualDeputyVoteData.VoteType.valueById(resultSet.getString(columnIndex));
				} else {
					return super.getValue(columnIndex, resultSet, columnName, columnClass);
				}
			}
		});
		this.add(filterPanel, BorderLayout.NORTH);
		this.add(new JScrollPane(mainTable), BorderLayout.CENTER);
		final JPanel btnPanel = new JPanel(new BorderLayout());
		btnPanel.add(sliderPercentage, BorderLayout.CENTER);
		btnPanel.add(doQuery, BorderLayout.EAST);
		btnPanel.add(sliderVal, BorderLayout.WEST);
		this.add(btnPanel, BorderLayout.SOUTH);

		sliderPercentage.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				sliderVal.setText(String.valueOf(sliderPercentage.getValue()));
			}
		});

		doQuery.addActionListener(this);
		mainTableMouseListener = new DefaultMouseListener() {
			@Override
			public void mouseClicked(final MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2) {
					int row = mainTable.rowAtPoint(mouseEvent.getPoint());
					if (row > -1) {
						final String deputee = mainTable.getModel().getValueAt(row, 2).toString();
						displayDetails(deputee);
					}
				}
			}
		};
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		doQuery.setEnabled(false);
		mainTable.removeMouseListener(mainTableMouseListener);
		new Thread() {
			public void run() {
				try {
					final TableModel result = analyzer.queryForDeputeesByFactionsDissent(sliderPercentage.getValue(), filterPanel.generateDataBrowseQuery());
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							mainTable.setModel(result);
							mainTable.addMouseListener(mainTableMouseListener);
							mainTable.setRowSorter(new TableRowSorter<TableModel>(result));
							mainTable.invalidate();
							mainTable.revalidate();
						}
					});
				} catch (final Exception ex) {
					ex.printStackTrace();
					SwingHelper.reportError(true, DeputeesDissentPanel.this, ex);
				} finally {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							doQuery.setEnabled(true);
						}
					});
				}
			}
		}.start();
	}

	protected void displayDetails(final String deputee) {
		doQuery.setEnabled(false);
		new Thread() {
			public void run() {
				try {
					final DataBrowseQuery query = filterPanel.generateDataBrowseQuery(deputee);
					final TableModel result = analyzer.queryForDeputeeDissentingLaws(sliderPercentage.getValue(), query);
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							mainTable.setModel(result);
							mainTable.removeMouseListener(mainTableMouseListener);
							mainTable.setRowSorter(new TableRowSorter<TableModel>(result));
							mainTable.invalidate();
							mainTable.revalidate();
						}
					});
				} catch (final Exception ex) {
					ex.printStackTrace();
					SwingHelper.reportError(true, DeputeesDissentPanel.this, ex);
				} finally {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							doQuery.setEnabled(true);
						}
					});
				}
			}
		}.start();
	}
}
