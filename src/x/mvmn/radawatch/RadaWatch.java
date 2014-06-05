package x.mvmn.radawatch;

import java.awt.BorderLayout;
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
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import net.sourceforge.jdatepicker.impl.JDatePanelImpl;
import net.sourceforge.jdatepicker.impl.JDatePickerImpl;
import net.sourceforge.jdatepicker.impl.UtilDateModel;

import org.h2.tools.RunScript;
import org.h2.tools.Script;
import org.h2.util.IOUtils;
import org.h2.util.JdbcUtils;

import x.mvmn.radawatch.service.analyze.VotingTitlesAnalyzer;
import x.mvmn.radawatch.service.db.StorageService;
import x.mvmn.radawatch.service.db.VoteResultsStorageService;
import x.mvmn.radawatch.service.parse.MeetingsListParser;
import x.mvmn.radawatch.swing.EmptyWindowListener;

public class RadaWatch {
	public static void main(String args[]) {
		System.out.println(RadaWatch.getInstance());
	}

	private static final RadaWatch INSTANCE = new RadaWatch();

	public static RadaWatch getInstance() {
		return INSTANCE;
	}

	private final JFrame mainWindow = new JFrame("Rada Watch");
	private final StorageService storageService = new StorageService();
	private final VoteResultsStorageService vrStore = new VoteResultsStorageService(storageService);
	private final JButton btnRecreateDb = new JButton("Re-create DB");
	private final JButton btnBrowseDb = new JButton("Browse DB");
	private final JButton btnBackupDb = new JButton("Backup DB");
	private final JButton btnRestoreDb = new JButton("Restore DB");
	private final JButton btnFetch = new JButton("Fetch data");
	private final JButton btnStop = new JButton("Stop fetch");
	private final JProgressBar prbFetch = new JProgressBar(JProgressBar.HORIZONTAL);
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
					btnRecreateDb.setEnabled(false);
					btnFetch.setEnabled(false);
					new Thread() {
						public void run() {
							FileReader fis = null;
							Connection conn = null;
							try {
								storageService.dropTables();
								conn = storageService.getConnection();
								fis = new FileReader(fileToLoadFrom);
								RunScript.execute(conn, fis);
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										btnRestoreDb.setEnabled(true);
										btnBackupDb.setEnabled(true);
										btnRecreateDb.setEnabled(true);
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
										btnRecreateDb.setEnabled(true);
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
		btnRecreateDb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(mainWindow, "Are you sure?", "Reset DB (all data will be lost)",
							JOptionPane.YES_NO_OPTION)) {
						storageService.dropTables();
						storageService.createTables();
						JOptionPane.showMessageDialog(mainWindow, "DB reset succeeded");
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
			JPanel datesPanel = new JPanel(new BorderLayout());
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

			JPanel datesPanel = new JPanel(new BorderLayout());
			datesPanel.add(datePickerFrom, BorderLayout.WEST);
			datesPanel.add(datePickerTo, BorderLayout.EAST);
			datesPanel.add(new JLabel("<== Date FROM | Date TO ==>", JLabel.CENTER), BorderLayout.CENTER);
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
								VotingTitlesAnalyzer titlesAnalyzer = new VotingTitlesAnalyzer(storageService);
								Date fromDate = (Date) datePickerFrom.getModel().getValue();
								Date toDate = (Date) datePickerTo.getModel().getValue();
								final TitlesTree titlesTree = new TitlesTree(titlesAnalyzer.mapTitles(titlesAnalyzer.getVotingTitles(fromDate, toDate)));

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
				JPanel btnSubPanel = new JPanel(new BorderLayout());
				btnSubPanel.add(btnRestoreDb, BorderLayout.CENTER);
				btnSubPanel.add(btnRecreateDb, BorderLayout.EAST);
				btnPanel.add(btnSubPanel, BorderLayout.EAST);
			}
			btnPanel.add(btnFetch, BorderLayout.CENTER);
			tabFetch.add(btnPanel, BorderLayout.SOUTH);
		}
		{
			prbFetch.setIndeterminate(true);
			prbFetch.setEnabled(false);
			JPanel progressPanel = new JPanel(new BorderLayout());
			progressPanel.add(new JLabel("Fetch progress"), BorderLayout.WEST);
			progressPanel.add(prbFetch, BorderLayout.CENTER);
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

		private MeetingsListParser parser;
		private volatile boolean stopRequested = false;

		public void beforeRun() throws Exception {
			btnFetch.setEnabled(false);
			prbFetch.setEnabled(true);
			parser = new MeetingsListParser();
			prbFetch.setIndeterminate(false);
			prbFetch.setMinimum(0);
			prbFetch.setMaximum(parser.getLastPageNumber());
			prbFetch.setValue(0);
		}

		public void stop() {
			this.stopRequested = true;
		}

		public void run() {
			try {
				for (int i = 1; i < parser.getLastPageNumber() && !stopRequested; i++) {
					final int currentPage = i;
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							txaLog.append(String.format("Fetching from page %s...\n", currentPage));
						}
					});
					final int fetchedRecords = parser.fetchNewMeetings(i, vrStore, vrStore);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							prbFetch.setValue(currentPage);
							txaLog.append(String.format("Fetched %s records from page %s.\n", fetchedRecords, currentPage));
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
						prbFetch.setIndeterminate(true);
						prbFetch.setEnabled(false);
						btnFetch.setEnabled(true);
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
}
