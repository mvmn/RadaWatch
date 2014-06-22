package x.mvmn.radawatch;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import x.mvmn.lang.ExceptionHandler;
import x.mvmn.radawatch.gui.FetchProgressPanel;
import x.mvmn.radawatch.model.Entity;
import x.mvmn.radawatch.service.db.DataStorageService;
import x.mvmn.radawatch.service.parse.ItemsByPagedLinksParser;
import x.mvmn.radawatch.service.parse.ItemsByPagedLinksParser.ItemLinkData;

class FetchJob<T extends Entity> implements Runnable {

	private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
		int lastTriedPageNumber = 0;
		int lastTriedItemId = 0;
		try {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fetchLog.append("Fetch job started " + dateFormat.format(new Date()) + ".\n");
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
				lastTriedPageNumber = pageIndex;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						fetchLog.append(String.format("Fetching from page %s...\n", currentPage));
					}
				});
				final List<ItemLinkData<T>> itemsLinksData = parser.parseOutItemsLinksData(currentPage);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						fetchProgressPanel.setPagesProgress(currentPage);
						fetchProgressPanel.setItemsCount(itemsLinksData.size());
						fetchProgressPanel.setItemsProgress(0);
					}
				});

				int fetchedRecords = 0;
				int existedRecords = 0;
				for (int itemIndex = 0; itemIndex < itemsLinksData.size(); itemIndex++) {
					ItemLinkData<T> itemLinkData = itemsLinksData.get(itemIndex);
					int itemId = itemLinkData.getItemSiteId();
					lastTriedItemId = itemId;
					if (stopRequested) {
						break;
					}
					if (!store.checkExists(itemId)) {
						store.storeNewRecord(parser.parseOutItem(itemLinkData));
						fetchedRecords++;
					} else {
						existedRecords++;
					}
					final int currentItem = itemIndex + 1;
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							fetchProgressPanel.setItemsProgress(currentItem);

						}
					});
				}
				final int finalFetchedRecords = fetchedRecords;
				final int finalExistedRecords = existedRecords;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						fetchLog.append(String.format("Fetched %s new of %s total items (%s aready fetched) from page %s\n", finalFetchedRecords,
								itemsLinksData.size(), finalExistedRecords, currentPage));
					}
				});
			}
			if (stopRequested) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						fetchLog.append(String.format("Stopping fetch on user request.\n"));
					}
				});
			}
		} catch (final Exception ex) {
			final int finalLastTriedPageNumber = lastTriedPageNumber;
			final int finalLastTriedItemId = lastTriedItemId;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fetchLog.append(String.format("Fetch failed (while fetching page %s item %s) with exception %s %s.\n", finalLastTriedPageNumber,
							finalLastTriedItemId, ex.getClass().getName(), ex.getMessage()));
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
					fetchLog.append("Fetch job ended " + dateFormat.format(new Date()) + ".\n");
					fetchProgressPanel.reset();
				}
			});
			finishCallback.run();
		}
	}
}