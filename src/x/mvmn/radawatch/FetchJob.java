package x.mvmn.radawatch;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import x.mvmn.lang.ExceptionHandler;
import x.mvmn.radawatch.gui.FetchProgressPanel;
import x.mvmn.radawatch.model.Entity;
import x.mvmn.radawatch.service.db.DataStorageService;
import x.mvmn.radawatch.service.parse.ItemsByPagedLinksParser;

class FetchJob<T extends Entity> implements Runnable {

	private final ItemsByPagedLinksParser<T> parser;
	private final DataStorageService<T> store;
	private volatile boolean stopRequested = false;
	private final Runnable finishCallback;
	private final FetchProgressPanel fetchProgressPanel;
	private final JTextArea fetchLog;
	private final ExceptionHandler<Exception> errorHandler;

	public FetchJob(final ItemsByPagedLinksParser<T> parser, final DataStorageService<T> store, final Runnable finishCallback,
			final FetchProgressPanel fetchProgressPanel, final JTextArea fetchLog, ExceptionHandler<Exception> errorHandler) {
		this.parser = parser;
		this.store = store;
		this.finishCallback = finishCallback;
		this.fetchProgressPanel = fetchProgressPanel;
		this.fetchLog = fetchLog;
		this.errorHandler = errorHandler;
	}

	public void stop() {
		this.stopRequested = true;
	}

	public void run() {
		try {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fetchLog.append("Parsing pages count... ");
				}
			});
			final int totalPagesCount = parser.parseOutTotalPagesCount(); // TODO: move off EDT
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fetchLog.append(String.format("%s pages found.\n", totalPagesCount));
					fetchProgressPanel.setPagesCount(totalPagesCount);
				}
			});

			for (int pageIndex = 1; pageIndex <= totalPagesCount && !stopRequested; pageIndex++) {
				final int currentPage = pageIndex;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						fetchLog.append(String.format("Fetching from page %s...\n", currentPage));
					}
				});
				final int[] itemIds = parser.parseOutItemsSiteIds(currentPage);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						fetchProgressPanel.setPagesProgress(currentPage);
						fetchProgressPanel.setItemsCount(itemIds.length);
						fetchProgressPanel.setItemsProgress(0);
					}
				});

				int fetchedRecords = 0;
				for (int itemIndex = 0; itemIndex < itemIds.length; itemIndex++) {
					int itemId = itemIds[itemIndex];
					if (stopRequested) {
						break;
					}
					if (!store.checkExists(itemId)) {
						store.storeNewRecord(parser.parseOutItem(itemId));
						fetchedRecords++;
					}
					final int currentItem = itemIndex + 1;
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							fetchProgressPanel.setItemsProgress(currentItem);

						}
					});
				}
				final int finalFetchedRecords = fetchedRecords;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						fetchLog.append(String.format("Fetched %s new items from page %s\n", finalFetchedRecords, currentPage));
					}
				});
			}
			if (stopRequested) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						fetchLog.append(String.format("Stopped by user.\n"));
					}
				});
			}
		} catch (final Exception ex) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fetchLog.append(String.format("Fetch failed with exception %s %s", ex.getClass().getName(), ex.getMessage()));
				}
			});
			if (errorHandler != null) {
				try {
					errorHandler.handleException(ex);
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		} finally {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fetchProgressPanel.reset();
				}
			});
			finishCallback.run();
		}
	}
}