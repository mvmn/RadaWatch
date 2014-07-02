package x.mvmn.radawatch.gui.analyze;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.CategoryPlot;

import x.mvmn.radawatch.gui.ChartHelper;
import x.mvmn.radawatch.service.analyze.DeputeesStatsAnalyzer;
import x.mvmn.radawatch.service.analyze.DeputeesStatsAnalyzer.DeputyStats;

public class DeputeesStatsPanel extends JPanel {
	private static final long serialVersionUID = 8781930142316977237L;

	protected final DeputeesStatsAnalyzer analyzer;

	protected final JButton btnUpdate = new JButton("Refresh");
	protected final JTable tblResults = new JTable();
	protected final JTabbedPane resultsContainer = new JTabbedPane();
	protected final JLabel lblLoading = new JLabel("Loading data...", JLabel.CENTER);

	private static final String[] COLUMN_NAMES = new String[] { "Deputy name", "Factions", "Total votings records", "'For' votes", "'For' votes %",
			"'Against' votes", "'Against' votes %", "Abstained", "Abstained %", "Skipped", "Skipped %", "Absent", "Absent %" };

	public DeputeesStatsPanel(final DeputeesStatsAnalyzer analyzer) {
		super(new BorderLayout());
		this.analyzer = analyzer;

		btnUpdate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				update();
			}
		});

		this.add(btnUpdate, BorderLayout.SOUTH);
		// update();
	}

	public void update() {
		btnUpdate.setEnabled(false);
		this.remove(resultsContainer);
		if (resultsContainer.getTabCount() > 1) {
			resultsContainer.removeTabAt(1);
		}
		resultsContainer.addTab("Table", new JScrollPane(tblResults));
		this.add(lblLoading, BorderLayout.CENTER);
		this.invalidate();
		this.revalidate();
		this.repaint();
		new Thread() {
			public void run() {
				try {
					final List<DeputyStats> stats = analyzer.getStats();
					final TableModel dataModel = new TableModel() {
						private final CopyOnWriteArrayList<TableModelListener> listeners = new CopyOnWriteArrayList<TableModelListener>();

						@Override
						public void setValueAt(Object arg0, int arg1, int arg2) {
						}

						@Override
						public void removeTableModelListener(final TableModelListener listener) {
							listeners.remove(listener);
						}

						@Override
						public void addTableModelListener(final TableModelListener listener) {
							listeners.remove(listener);
						}

						@Override
						public boolean isCellEditable(int arg0, int arg1) {
							return false;
						}

						@Override
						public Object getValueAt(int row, int column) {
							final DeputyStats rowData = stats.get(row);
							if (column == 0) {
								return rowData.getName();
							} else {
								final Integer result;
								final int totalRecords = rowData.getTotalVotingRecords();
								switch (column) {
									default:
									case 1:
										result = rowData.getTotalFactions();
									break;
									case 2:
										result = totalRecords;
									break;
									case 3:
										result = rowData.getVotedFor();
									break;
									case 4:
										result = (int) (((double) rowData.getVotedFor()) * 100 / totalRecords);
									break;
									case 5:
										result = rowData.getVotedAgainst();
									break;
									case 6:
										result = (int) (((double) rowData.getVotedAgainst()) * 100 / totalRecords);
									break;
									case 7:
										result = rowData.getVoteAbstained();
									break;
									case 8:
										result = (int) (((double) rowData.getVoteAbstained()) * 100 / totalRecords);
									break;
									case 9:
										result = rowData.getVotesSkipped();
									break;
									case 10:
										result = (int) (((double) rowData.getVotesSkipped()) * 100 / totalRecords);
									break;
									case 11:
										result = rowData.getVotesAbsent();
									break;
									case 12:
										result = (int) (((double) rowData.getVotesAbsent()) * 100 / totalRecords);
									break;
								}
								return result;
							}
						}

						@Override
						public int getRowCount() {
							return stats.size();
						}

						@Override
						public String getColumnName(int index) {
							return COLUMN_NAMES[index];
						}

						@Override
						public int getColumnCount() {
							return COLUMN_NAMES.length;
						}

						@Override
						public Class<?> getColumnClass(int index) {
							return index < 1 ? String.class : Integer.class;
						}
					};
					final ChartPanel chartPanel = ChartHelper.fromTableModel(dataModel, "Deputy", "");
					ChartHelper.updateCategoryChartRenderParameters(chartPanel, DeputeesStatsPanel.this);
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							tblResults.setModel(dataModel);
							tblResults.setRowSorter(new TableRowSorter<TableModel>(dataModel));

							final CategoryPlot plot = (CategoryPlot) chartPanel.getChart().getPlot();
							final JPanel panel = new JPanel(new BorderLayout());
							panel.add(new JScrollPane(chartPanel), BorderLayout.CENTER);
							final JPanel checkboxesPanel = new JPanel(new GridLayout(plot.getDataset().getRowCount(), 1));
							panel.add(new JScrollPane(checkboxesPanel), BorderLayout.EAST);
							for (int datasetRowIndex = 0; datasetRowIndex < plot.getDataset().getRowCount(); datasetRowIndex++) {
								final JCheckBox checkBox = new JCheckBox(plot.getDataset().getRowKey(datasetRowIndex).toString());
								checkBox.setSelected(true);
								checkboxesPanel.add(checkBox);
								final int finalRowIndex = datasetRowIndex;
								checkBox.addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent evt) {
										plot.getRenderer().setSeriesVisible(finalRowIndex, checkBox.isSelected());
										ChartHelper.updateCategoryChartRenderParameters(chartPanel, DeputeesStatsPanel.this);
									}
								});
							}

							resultsContainer.addTab("Chart", panel);
						}
					});

				} catch (final Exception ex) {
					ex.printStackTrace();
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							JOptionPane.showMessageDialog(DeputeesStatsPanel.this, ex.getClass().getCanonicalName() + " " + ex.getMessage(), "Error occurred",
									JOptionPane.ERROR_MESSAGE);
						}
					});
				} finally {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							DeputeesStatsPanel.this.remove(lblLoading);
							DeputeesStatsPanel.this.add(resultsContainer, BorderLayout.CENTER);
							btnUpdate.setEnabled(true);
							DeputeesStatsPanel.this.invalidate();
							DeputeesStatsPanel.this.revalidate();
							DeputeesStatsPanel.this.repaint();
						}
					});
				}
			}
		}.start();
	}
}
