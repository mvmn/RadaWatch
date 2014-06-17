package x.mvmn.radawatch.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

public class FetchPanel extends JPanel {

	private static final long serialVersionUID = 7474169631564428495L;

	public static final String ACT_COMMAND_FETCH = FetchPanel.class.getCanonicalName() + ":ACT_COMMAND_FETCH";
	public static final String ACT_COMMAND_STOP = FetchPanel.class.getCanonicalName() + ":ACT_COMMAND_STOP";
	public static final String ACT_COMMAND_RESET_DB_TABLES = FetchPanel.class.getCanonicalName() + ":ACT_COMMAND_RESET";

	protected final String dataTitle;

	protected final JTextArea txaFetchLog = new JTextArea();
	protected final FetchProgressPanel fetchProgressPanel;

	protected final JButton btnFetch;
	protected final JButton btnStop;
	protected final JButton btnDbTablesReset;

	protected volatile boolean inProgress = false;
	protected final DBStatsPanel<?> dbStatsPanel;

	public FetchPanel(final String dataTitle, final DBStatsPanel<?> dbStatsPanel) {
		super(new BorderLayout());
		this.dataTitle = dataTitle;
		this.dbStatsPanel = dbStatsPanel;
		fetchProgressPanel = new FetchProgressPanel("Fetch progress for " + dataTitle);
		btnFetch = new JButton("Fetch " + dataTitle + " data");
		btnStop = new JButton("Stop fetching " + dataTitle);
		btnDbTablesReset = new JButton("Re-create DB tables for " + dataTitle);
		btnFetch.setActionCommand(ACT_COMMAND_FETCH);
		btnStop.setActionCommand(ACT_COMMAND_STOP);
		btnDbTablesReset.setActionCommand(ACT_COMMAND_RESET_DB_TABLES);
		Component mainComponent = new JScrollPane(txaFetchLog);
		if (dbStatsPanel != null) {
			final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, mainComponent, dbStatsPanel);
			splitPane.setDividerLocation(0.5f);
			splitPane.setResizeWeight(0.5f);
			mainComponent = splitPane;
		}
		this.add(mainComponent, BorderLayout.CENTER);
		this.add(fetchProgressPanel, BorderLayout.NORTH);
		final JPanel buttonsPanel = new JPanel(new BorderLayout());
		buttonsPanel.add(btnDbTablesReset, BorderLayout.WEST);
		buttonsPanel.add(btnFetch, BorderLayout.CENTER);
		buttonsPanel.add(btnStop, BorderLayout.EAST);
		this.add(buttonsPanel, BorderLayout.SOUTH);
	}

	public void addFetchButtonActionListener(final ActionListener actionListener) {
		btnFetch.addActionListener(actionListener);
	}

	public void addStopButtonActionListener(final ActionListener actionListener) {
		btnStop.addActionListener(actionListener);
	}

	public void addResetDbTablesButtonActionListener(final ActionListener actionListener) {
		btnDbTablesReset.addActionListener(actionListener);
	}

	public void setEnabled(final boolean enabled) {
		if (!enabled) {
			btnFetch.setEnabled(false);
			btnDbTablesReset.setEnabled(false);
		} else {
			btnDbTablesReset.setEnabled(true);
			btnFetch.setEnabled(!inProgress);
		}
	}

	public JTextArea getTxaFetchLog() {
		return txaFetchLog;
	}

	public FetchProgressPanel getFetchProgressPanel() {
		return fetchProgressPanel;
	}

	public boolean isInProgress() {
		return inProgress;
	}

	public void setInProgress(boolean inProgress) {
		this.inProgress = inProgress;
		btnFetch.setEnabled(!inProgress);
	}

	public String getDataTitle() {
		return dataTitle;
	}

	public DBStatsPanel<?> getDbStatsPanel() {
		return dbStatsPanel;
	}
}
