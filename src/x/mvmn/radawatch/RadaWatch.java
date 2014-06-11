package x.mvmn.radawatch;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.sourceforge.jdatepicker.impl.JDatePanelImpl;
import net.sourceforge.jdatepicker.impl.JDatePickerImpl;
import net.sourceforge.jdatepicker.impl.UtilDateModel;

import org.h2.tools.RunScript;
import org.h2.tools.Script;
import org.h2.util.IOUtils;
import org.h2.util.JdbcUtils;

import x.mvmn.radawatch.gui.FetchProgressPanel;
import x.mvmn.radawatch.model.radavotes.VoteResultsData;
import x.mvmn.radawatch.service.analyze.radavotes.VotingTitlesAnalyzer;
import x.mvmn.radawatch.service.db.DataBaseConnectionService;
import x.mvmn.radawatch.service.db.presdecrees.PresidentialDecreesStorageService;
import x.mvmn.radawatch.service.db.radavotes.RadaVotesStorageService;
import x.mvmn.radawatch.service.parse.ItemsByPagedLinksParser;
import x.mvmn.radawatch.service.parse.radavotes.VoteResultsParser;
import x.mvmn.radawatch.swing.EmptyWindowListener;

public class RadaWatch {
	public static void main(String args[]) {
		System.out.println(RadaWatch.getInstance());
	}

	private static final RadaWatch INSTANCE = new RadaWatch();

	public static RadaWatch getInstance() {
		return INSTANCE;
	}

	private final JFrame mainWindow = new JFrame("Rada Watch by Mykola Makhin"); // Shameless selfpromotion, hehe
	private final DataBaseConnectionService storageService = new DataBaseConnectionService();
	private final RadaVotesStorageService vrStore = new RadaVotesStorageService(storageService);
	private final PresidentialDecreesStorageService pdStore = new PresidentialDecreesStorageService(storageService);
	private final JButton btnRecreateVotesTables = new JButton("Re-create Votes tables");
	private final JButton btnRecreateDecreesTables = new JButton("Re-create Pres.Decrees tables");
	private final JButton btnBrowseDb = new JButton("Browse DB");
	private final JButton btnBackupDb = new JButton("Backup DB");
	private final JButton btnRestoreDb = new JButton("Restore DB");
	private final JButton btnFetch = new JButton("Fetch data");
	private final JButton btnStop = new JButton("Stop fetch");
	private final FetchProgressPanel fetchProgressPanel = new FetchProgressPanel("Rada votes fetch progress");
	private final JTextArea txaLog = new JTextArea();

	private volatile FetchJob fetchJob = null;

	public RadaWatch() {
		mainWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainWindow.addWindowListener(new EmptyWindowListener() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				RadaWatch.this.closeRequest();
			}
		});

		JTabbedPane tabPane = new JTabbedPane();
		final JPanel tabFetch = new JPanel(new BorderLayout());
		final JPanel tabAnalyze = new JPanel(new BorderLayout());
		final JPanel tabStats = new JPanel(new BorderLayout());
		tabPane.addTab("Fetch", tabFetch);
		tabPane.addTab("Analyze", tabAnalyze);
		tabPane.addTab("Stats", tabStats);

		btnBrowseDb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				storageService.openDbBrowser();
			}
		});

		btnRestoreDb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(mainWindow)) {
					final File fileToLoadFrom = fileChooser.getSelectedFile();
					btnRestoreDb.setEnabled(false);
					btnBackupDb.setEnabled(false);
					btnRecreateVotesTables.setEnabled(false);
					btnFetch.setEnabled(false);
					new Thread() {
						public void run() {
							FileReader fis = null;
							Connection conn = null;
							try {
								vrStore.dropAllTables();
								pdStore.dropAllTables();
								conn = storageService.getConnection();
								fis = new FileReader(fileToLoadFrom);
								RunScript.execute(conn, fis);
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										btnRestoreDb.setEnabled(true);
										btnBackupDb.setEnabled(true);
										btnRecreateVotesTables.setEnabled(true);
										btnFetch.setEnabled(true);

										JOptionPane.showMessageDialog(mainWindow, "Script " + fileToLoadFrom.getPath() + " executed successfully",
												"DB restore succeeded", JOptionPane.INFORMATION_MESSAGE);
									}
								});
							} catch (final Exception ex) {
								ex.printStackTrace();
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										btnRestoreDb.setEnabled(true);
										btnBackupDb.setEnabled(true);
										btnRecreateVotesTables.setEnabled(true);
										btnFetch.setEnabled(true);

										JOptionPane.showMessageDialog(mainWindow, ex.getClass().getCanonicalName() + " " + ex.getMessage(), "Error occurred",
												JOptionPane.ERROR_MESSAGE);
									}
								});
							} finally {
								IOUtils.closeSilently(fis);
								JdbcUtils.closeSilently(conn);
							}
						}
					}.start();
				}
			}
		});
		btnBackupDb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(mainWindow)) {
					final File fileToSaveTo = fileChooser.getSelectedFile();
					btnBackupDb.setEnabled(false);
					new Thread() {
						public void run() {
							FileOutputStream fis = null;
							try {
								fis = new FileOutputStream(fileToSaveTo);
								Method m = Script.class.getDeclaredMethod("process", Connection.class, OutputStream.class);
								m.setAccessible(true);
								m.invoke(null, storageService.getConnection(), fis);
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										btnBackupDb.setEnabled(true);
										JOptionPane.showMessageDialog(mainWindow, "File " + fileToSaveTo.getPath() + " saved successfully",
												"DB backup succeeded", JOptionPane.INFORMATION_MESSAGE);
									}
								});
							} catch (final Exception ex) {
								ex.printStackTrace();
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										btnBackupDb.setEnabled(true);
										JOptionPane.showMessageDialog(mainWindow, ex.getClass().getCanonicalName() + " " + ex.getMessage(), "Error occurred",
												JOptionPane.ERROR_MESSAGE);
									}
								});
							} finally {
								IOUtils.closeSilently(fis);

							}
						}
					}.start();
				}
			}
		});
		btnRecreateDecreesTables.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(mainWindow,
							"Really reset Presidential Decrees tables (all decrees data will be lost)?", "Are you sure?", JOptionPane.YES_NO_OPTION)) {
						pdStore.dropAllTables();
						pdStore.createAllTables();
						JOptionPane.showMessageDialog(mainWindow, "Presidential Decrees tables reset succeeded");
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(mainWindow, ex.getClass().getCanonicalName() + " " + ex.getMessage(), "Error occurred",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnRecreateVotesTables.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(mainWindow, "Really reset Votes tables (all votes data will be lost)?",
							"Are you sure?", JOptionPane.YES_NO_OPTION)) {
						vrStore.dropAllTables();
						vrStore.createAllTables();
						JOptionPane.showMessageDialog(mainWindow, "Votes tables reset succeeded");
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(mainWindow, ex.getClass().getCanonicalName() + " " + ex.getMessage(), "Error occurred",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		// ----- //
		{
			// TODO: refactor
			final JButton btnGenCharts = new JButton("Generate charts");
			final JDatePickerImpl datePickerFrom = new JDatePickerImpl(new JDatePanelImpl(new UtilDateModel()));
			final JDatePickerImpl datePickerTo = new JDatePickerImpl(new JDatePanelImpl(new UtilDateModel()));
			final JPanel datesPanel = new JPanel(new BorderLayout());
			datesPanel.add(datePickerFrom, BorderLayout.WEST);
			datesPanel.add(datePickerTo, BorderLayout.EAST);
			datesPanel.add(new JLabel("<== Date FROM | Date TO ==>", JLabel.CENTER), BorderLayout.CENTER);
			tabStats.add(btnGenCharts, BorderLayout.SOUTH);
			tabStats.add(datesPanel, BorderLayout.NORTH);
			final JPanel resultsPanel = new JPanel(new BorderLayout());
			tabStats.add(resultsPanel, BorderLayout.CENTER);
			btnGenCharts.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					btnGenCharts.setEnabled(false);
					new Thread() {
						public void run() {
							try {

								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										btnGenCharts.setEnabled(true);
										resultsPanel.removeAll();
										// resultsPanel.add(new JScrollPane(titlesTree), BorderLayout.CENTER);
										resultsPanel.validate();

									}
								});
							} catch (final Exception ex) {
								ex.printStackTrace();
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										btnGenCharts.setEnabled(true);
										JOptionPane.showMessageDialog(mainWindow, ex.getClass().getCanonicalName() + " " + ex.getMessage(), "Error occurred",
												JOptionPane.ERROR_MESSAGE);
									}
								});
							}
						}
					}.start();
				}
			});
		}

		{
			// TODO: refactor
			final JButton btnAnalyzeTitles = new JButton("Analyze titles");
			final JDatePickerImpl datePickerFrom = new JDatePickerImpl(new JDatePanelImpl(new UtilDateModel()));
			final JDatePickerImpl datePickerTo = new JDatePickerImpl(new JDatePanelImpl(new UtilDateModel()));

			final JPanel datesPanel = new JPanel(new BorderLayout());
			datesPanel.add(datePickerFrom, BorderLayout.WEST);
			datesPanel.add(datePickerTo, BorderLayout.EAST);
			final JTextField tfTitleFilter = new JTextField();
			final JPanel titleFilterPanel = new JPanel(new BorderLayout());
			titleFilterPanel.add(new JLabel("<== Date FROM", JLabel.CENTER), BorderLayout.WEST);
			titleFilterPanel.add(new JLabel("Date TO ==>", JLabel.CENTER), BorderLayout.EAST);
			titleFilterPanel.add(tfTitleFilter, BorderLayout.CENTER);
			datesPanel.add(titleFilterPanel, BorderLayout.CENTER);
			tabAnalyze.add(btnAnalyzeTitles, BorderLayout.SOUTH);
			tabAnalyze.add(datesPanel, BorderLayout.NORTH);
			final JPanel resultsPanel = new JPanel(new BorderLayout());
			tabAnalyze.add(resultsPanel, BorderLayout.CENTER);
			btnAnalyzeTitles.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					btnAnalyzeTitles.setEnabled(false);

					new Thread() {
						public void run() {
							try {
								final VotingTitlesAnalyzer titlesAnalyzer = new VotingTitlesAnalyzer(storageService);
								final Date fromDate = (Date) datePickerFrom.getModel().getValue();
								final Date toDate = (Date) datePickerTo.getModel().getValue();
								final TitlesTree titlesTree = new TitlesTree(titlesAnalyzer.mapTitles(titlesAnalyzer.getVotingTitles(fromDate, toDate,
										tfTitleFilter.getText())));

								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										btnAnalyzeTitles.setEnabled(true);
										resultsPanel.removeAll();
										resultsPanel.add(new JScrollPane(titlesTree), BorderLayout.CENTER);
										resultsPanel.validate();

									}
								});
							} catch (final Exception ex) {
								ex.printStackTrace();
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										btnAnalyzeTitles.setEnabled(true);
										JOptionPane.showMessageDialog(mainWindow, ex.getClass().getCanonicalName() + " " + ex.getMessage(), "Error occurred",
												JOptionPane.ERROR_MESSAGE);
									}
								});
							}
						}
					}.start();
				}
			});
		}
		{
			JPanel btnPanel = new JPanel(new BorderLayout());
			{
				JPanel btnSubPanel = new JPanel(new BorderLayout());
				btnSubPanel.add(btnBrowseDb, BorderLayout.CENTER);
				btnSubPanel.add(btnBackupDb, BorderLayout.EAST);
				btnPanel.add(btnSubPanel, BorderLayout.WEST);
			}
			{
				JPanel btnSubPanel = new JPanel(new GridLayout(3, 1));
				btnSubPanel.add(btnRestoreDb);
				btnSubPanel.add(btnRecreateDecreesTables);
				btnSubPanel.add(btnRecreateVotesTables);
				btnPanel.add(btnSubPanel, BorderLayout.EAST);
			}
			btnPanel.add(btnFetch, BorderLayout.CENTER);
			tabFetch.add(btnPanel, BorderLayout.SOUTH);
		}
		{
			fetchProgressPanel.reset();
			JPanel progressPanel = new JPanel(new BorderLayout());
			progressPanel.add(fetchProgressPanel, BorderLayout.CENTER);
			progressPanel.add(btnStop, BorderLayout.EAST);
			tabFetch.add(progressPanel, BorderLayout.NORTH);
		}

		btnStop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (fetchJob != null) {
					fetchJob.stop();
				}
			}
		});

		btnFetch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				synchronized (this) {
					if (fetchJob == null) {
						try {
							fetchJob = new FetchJob();
							fetchJob.beforeRun();
							new Thread(fetchJob).start();
						} catch (Exception ex) {
							ex.printStackTrace();
							JOptionPane.showMessageDialog(mainWindow, ex.getClass().getCanonicalName() + " " + ex.getMessage(), "Error occurred",
									JOptionPane.ERROR_MESSAGE);
						}
					} else {
						JOptionPane.showMessageDialog(mainWindow, "Fetch Job already running");
					}
				}

			}
		});
		tabFetch.add(new JScrollPane(txaLog), BorderLayout.CENTER);

		mainWindow.getContentPane().setLayout(new BorderLayout());
		mainWindow.getContentPane().add(tabPane, BorderLayout.CENTER);
		mainWindow.pack();
		mainWindow.setVisible(true);
	}

	private class FetchJob implements Runnable {

		private ItemsByPagedLinksParser<VoteResultsData> parser;
		private volatile boolean stopRequested = false;
		private int totalPagesCount = 0;

		public void beforeRun() throws Exception {
			btnFetch.setEnabled(false);
			parser = new VoteResultsParser();
			totalPagesCount = parser.parseOutTotalPagesCount(); // TODO: move off EDT
			fetchProgressPanel.setPagesCount(totalPagesCount);
		}

		public void stop() {
			this.stopRequested = true;
		}

		public void run() {
			try {
				for (int pageIndex = 1; pageIndex < totalPagesCount && !stopRequested; pageIndex++) {
					final int currentPage = pageIndex;
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							txaLog.append(String.format("Fetching from page %s...\n", currentPage));
						}
					});
					final int[] itemIds = parser.parseOutItemsSiteIds(currentPage);
					fetchProgressPanel.setItemsCount(totalPagesCount);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							fetchProgressPanel.setPagesProgress(currentPage);
							fetchProgressPanel.setItemsCount(itemIds.length);
						}
					});

					int fetchedRecords = 0;
					for (int itemIndex = 0; itemIndex < itemIds.length; itemIndex++) {
						int itemId = itemIds[itemIndex];
						if (stopRequested) {
							break;
						}
						if (!vrStore.checkExists(itemId)) {
							vrStore.storeNewRecord(parser.parseOutItem(itemId));
							fetchedRecords++;
						}
						final int currentItem = itemIndex;
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								fetchProgressPanel.setItemsProgress(currentItem);

							}
						});
					}
					final int finalFetchedRecords = fetchedRecords;
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							txaLog.append(String.format("Fetched %s new items from page %s\n", finalFetchedRecords, currentPage));
						}
					});
				}
				if (stopRequested) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							txaLog.append(String.format("Stopped by user.\n"));
						}
					});
				}
			} catch (final Exception ex) {
				ex.printStackTrace();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JOptionPane.showMessageDialog(mainWindow, "Fetch error: " + ex.getClass().getCanonicalName() + " " + ex.getMessage(), "Error occurred",
								JOptionPane.ERROR_MESSAGE);
					}
				});
			} finally {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						fetchProgressPanel.reset();
					}
				});
				fetchJob = null;
			}
		}
	}

	public void closeRequest() {
		try {
			storageService.dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
		mainWindow.setVisible(false);
		mainWindow.dispose();
		System.exit(0);
	}

	public String toString() {
		return mainWindow.getTitle();
	}
}
