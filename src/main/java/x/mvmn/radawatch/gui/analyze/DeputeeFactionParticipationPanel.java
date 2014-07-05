package x.mvmn.radawatch.gui.analyze;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import x.mvmn.radawatch.service.analyze.DeputeesFactionsParticipationAnalyzer;
import x.mvmn.radawatch.service.analyze.DeputeesFactionsParticipationAnalyzer.DeputyFactionParticipation;

public class DeputeeFactionParticipationPanel extends JPanel {
	private static final long serialVersionUID = -5945222223886199546L;

	protected final DeputeesFactionsParticipationAnalyzer analyzer;

	protected final JButton btnUpdate = new JButton("Refresh");
	protected final JTable tblResults = new JTable();
	protected final JScrollPane resultsContainer = new JScrollPane(tblResults);
	protected final JTextArea txaLoadingLog = new JTextArea();
	protected final JScrollPane txaLoadingLogContainer = new JScrollPane(txaLoadingLog);

	private static final String[] COLUMN_NAMES = new String[] { "Deputy", "Faction", "From", "To" };

	public DeputeeFactionParticipationPanel(final DeputeesFactionsParticipationAnalyzer analyzer) {
		super(new BorderLayout());
		this.analyzer = analyzer;
		btnUpdate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				update();
			}
		});

		this.add(btnUpdate, BorderLayout.SOUTH);
	}

	public void update() {
		btnUpdate.setEnabled(false);
		this.remove(resultsContainer);
		txaLoadingLog.setText("Loading data...\n");
		this.add(txaLoadingLogContainer, BorderLayout.CENTER);
		this.invalidate();
		this.revalidate();
		this.repaint();
		new Thread() {
			public void run() {
				try {
					final List<DeputyFactionParticipation> results = analyzer.getData(txaLoadingLog);
					final TableModel tableModel = new TableModel() {

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
						public Object getValueAt(final int row, final int col) {
							final DeputyFactionParticipation dfp = results.get(row);
							final String result;
							switch (col) {
								default:
								case 0:
									result = dfp.getDeputy();
								break;
								case 1:
									result = dfp.getFaction();
								break;
								case 2:
									result = dfp.getStartDate() != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm").format(dfp.getStartDate()) : "--";
								break;
								case 3:
									result = dfp.getEndDate() != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm").format(dfp.getEndDate()) : "--";
								break;
							}
							return result;
						}

						@Override
						public int getRowCount() {
							return results.size();
						}

						@Override
						public String getColumnName(final int i) {
							return COLUMN_NAMES[i];
						}

						@Override
						public int getColumnCount() {
							return COLUMN_NAMES.length;
						}

						@Override
						public Class<?> getColumnClass(final int i) {
							return String.class;
						}
					};
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							tblResults.setModel(tableModel);
							tblResults.setRowSorter(new TableRowSorter<TableModel>(tableModel));
						}
					});
				} catch (final Exception ex) {
					ex.printStackTrace();
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							JOptionPane.showMessageDialog(DeputeeFactionParticipationPanel.this, ex.getClass().getCanonicalName() + " " + ex.getMessage(),
									"Error occurred", JOptionPane.ERROR_MESSAGE);
						}
					});
				} finally {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							btnUpdate.setEnabled(true);
							DeputeeFactionParticipationPanel.this.remove(txaLoadingLogContainer);
							DeputeeFactionParticipationPanel.this.add(resultsContainer, BorderLayout.CENTER);
							DeputeeFactionParticipationPanel.this.invalidate();
							DeputeeFactionParticipationPanel.this.revalidate();
							DeputeeFactionParticipationPanel.this.repaint();
						}
					});
				}
			}
		}.start();
	}
}
