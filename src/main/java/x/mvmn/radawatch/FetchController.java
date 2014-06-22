package x.mvmn.radawatch;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import x.mvmn.lang.ExceptionHandler;
import x.mvmn.radawatch.gui.fetch.DBStatsPanel;
import x.mvmn.radawatch.gui.fetch.FetchPanel;
import x.mvmn.radawatch.model.Entity;
import x.mvmn.radawatch.service.db.DataStorageService;
import x.mvmn.radawatch.service.parse.ItemsByPagedLinksParser;

public class FetchController<T extends Entity> implements ActionListener {

	protected final Component parentView;
	protected final FetchPanel<T> fetchPanel;

	protected final ItemsByPagedLinksParser<T> parser;
	protected final DataStorageService<T> storage;

	protected volatile FetchJob<T> fetchJob = null;

	public FetchController(final ItemsByPagedLinksParser<T> parser, final DataStorageService<T> storage, final FetchPanel<T> fetchPanel,
			final Component parentView) {
		this.parser = parser;
		this.storage = storage;
		this.fetchPanel = fetchPanel;
		this.parentView = parentView;
		fetchPanel.addFetchButtonActionListener(this);
		fetchPanel.addStopButtonActionListener(this);
		fetchPanel.addResetDbTablesButtonActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent actEvent) {
		if (FetchPanel.ACT_COMMAND_FETCH.equals(actEvent.getActionCommand())) {
			synchronized (this) {
				if (fetchJob == null) {
					try {
						fetchPanel.setInProgress(true);
						fetchJob = new FetchJob<T>(parser, storage, new Runnable() {
							public void run() {
								FetchController.this.fetchJob = null;
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										fetchPanel.setInProgress(false);
									}
								});
								DBStatsPanel<?> dbStatsPanel = fetchPanel.getDbStatsPanel();
								if (dbStatsPanel != null) {
									dbStatsPanel.refresh();
								}
							}
						}, fetchPanel.getFetchProgressPanel(), fetchPanel.getTxaFetchLog(), new ExceptionHandler<Exception>() {
							@Override
							public void handleException(final Exception exception) {
								exception.printStackTrace();
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										JOptionPane.showMessageDialog(parentView,
												"Fetch error: " + exception.getClass().getCanonicalName() + " " + exception.getMessage(), "Error occurred",
												JOptionPane.ERROR_MESSAGE);
									}
								});

							}
						});
						new Thread(fetchJob).start();
					} catch (Exception ex) {
						fetchPanel.setInProgress(false);
						ex.printStackTrace();
						JOptionPane.showMessageDialog(parentView, ex.getClass().getCanonicalName() + " " + ex.getMessage(), "Error occurred",
								JOptionPane.ERROR_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(parentView, "Fetch Job already running");
				}
			}
		} else if (FetchPanel.ACT_COMMAND_STOP.equals(actEvent.getActionCommand())) {
			if (fetchJob != null) {
				fetchJob.stop();
			}
		} else if (FetchPanel.ACT_COMMAND_RESET_DB_TABLES.equals(actEvent.getActionCommand())) {
			try {
				if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(parentView, "Really reset DB tables for " + fetchPanel.getDataTitle() + " (all "
						+ fetchPanel.getDataTitle() + " data will be lost)?", "Are you sure?", JOptionPane.YES_NO_OPTION)) {
					storage.dropAllTables();
					storage.createAllTables();
					JOptionPane.showMessageDialog(parentView, fetchPanel.getDataTitle() + " tables reset succeeded");
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				JOptionPane
						.showMessageDialog(parentView, ex.getClass().getCanonicalName() + " " + ex.getMessage(), "Error occurred", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public boolean isInProgress() {
		return fetchJob != null;
	}

	public Component getParentView() {
		return parentView;
	}

	public FetchPanel<T> getView() {
		return fetchPanel;
	}

	public void setControlsEnabled(boolean enabled) {
		fetchPanel.setEnabled(enabled);
	}

	public ItemsByPagedLinksParser<T> getParser() {
		return parser;
	}

	public DataStorageService<T> getStorage() {
		return storage;
	}

	public String getDataTitle() {
		return fetchPanel.getDataTitle();
	}
}
