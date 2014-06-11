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
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.sourceforge.jdatepicker.impl.JDatePanelImpl;
import net.sourceforge.jdatepicker.impl.JDatePickerImpl;
import net.sourceforge.jdatepicker.impl.UtilDateModel;

import org.h2.tools.RunScript;
import org.h2.tools.Script;
import org.h2.util.IOUtils;
import org.h2.util.JdbcUtils;

import x.mvmn.radawatch.gui.FetchPanel;
import x.mvmn.radawatch.gui.analyze.TitlesTree;
import x.mvmn.radawatch.model.presdecrees.PresidentialDecree;
import x.mvmn.radawatch.model.radavotes.VoteResultsData;
import x.mvmn.radawatch.service.analyze.radavotes.VotingTitlesAnalyzer;
import x.mvmn.radawatch.service.db.DataBaseConnectionService;
import x.mvmn.radawatch.service.db.presdecrees.PresidentialDecreesStorageService;
import x.mvmn.radawatch.service.db.radavotes.RadaVotesStorageService;
import x.mvmn.radawatch.service.parse.presdecrees.PredisentialDecreesParser;
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
	private final PresidentialDecreesStorageService pdStore = new PresidentialDecreesStorageService(storageService);
	private final JButton btnBrowseDb = new JButton("Browse DB");
	private final JButton btnBackupDb = new JButton("Backup DB");
	private final JButton btnRestoreDb = new JButton("Restore DB");
	private final FetchController<VoteResultsData> votesFetchController = new FetchController<VoteResultsData>(new VoteResultsParser(),
			new RadaVotesStorageService(storageService), new FetchPanel("Rada Votes"), mainWindow);
	private final FetchController<PresidentialDecree> presDecreesFetchController = new FetchController<PresidentialDecree>(new PredisentialDecreesParser(),
			new PresidentialDecreesStorageService(storageService), new FetchPanel("Presidential Decrees"), mainWindow);

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
				final Runnable restoreLogic = new Runnable() {
					public void run() {

						JFileChooser fileChooser = new JFileChooser();
						if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(mainWindow)) {
							final File fileToLoadFrom = fileChooser.getSelectedFile();
							btnRestoreDb.setEnabled(false);
							btnBackupDb.setEnabled(false);
							votesFetchController.setControlsEnabled(false);
							new Thread() {
								public void run() {
									FileReader fis = null;
									Connection conn = null;
									try {
										votesFetchController.getStorage().dropAllTables();
										pdStore.dropAllTables();
										conn = storageService.getConnection();
										fis = new FileReader(fileToLoadFrom);
										RunScript.execute(conn, fis);
										SwingUtilities.invokeLater(new Runnable() {
											@Override
											public void run() {
												btnRestoreDb.setEnabled(true);
												btnBackupDb.setEnabled(true);
												votesFetchController.setControlsEnabled(true);

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
												votesFetchController.setControlsEnabled(true);

												JOptionPane.showMessageDialog(mainWindow, ex.getClass().getCanonicalName() + " " + ex.getMessage(),
														"Error occurred", JOptionPane.ERROR_MESSAGE);
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
				};
				if (JOptionPane.OK_OPTION == JOptionPane
						.showConfirmDialog(
								mainWindow,
								"Restoring DB will overwrite currend DB completely \n(all current data will be deleted, and only then new data will be imported).\nDo you wish to backup current DB first?",
								"Backup current DB before restoring new?", JOptionPane.YES_NO_OPTION)) {
					RadaWatch.this.backupDb(restoreLogic);
				} else {
					restoreLogic.run();
				}
			}

		});
		btnBackupDb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				RadaWatch.this.backupDb(null);
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
			JTabbedPane tabFetchSubtabs = new JTabbedPane();
			tabFetch.add(tabFetchSubtabs, BorderLayout.CENTER);
			tabFetchSubtabs.addTab(votesFetchController.getDataTitle(), votesFetchController.getView());
			tabFetchSubtabs.addTab(presDecreesFetchController.getDataTitle(), presDecreesFetchController.getView());
		}

		mainWindow.getContentPane().setLayout(new BorderLayout());
		mainWindow.getContentPane().add(tabPane, BorderLayout.CENTER);
		{
			JPanel btnPanel = new JPanel(new BorderLayout());
			btnPanel.add(btnRestoreDb, BorderLayout.WEST);
			btnPanel.add(btnBrowseDb, BorderLayout.CENTER);
			btnPanel.add(btnBackupDb, BorderLayout.EAST);
			mainWindow.getContentPane().add(btnPanel, BorderLayout.SOUTH);
		}
		mainWindow.pack();
		mainWindow.setVisible(true);
	}

	protected void backupDb(final Runnable callbackOnSwingEDT) {
		JFileChooser fileChooser = new JFileChooser();
		if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(mainWindow)) {
			final File fileToSaveTo = fileChooser.getSelectedFile();
			btnBackupDb.setEnabled(false);
			btnRestoreDb.setEnabled(false);
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
								JOptionPane.showMessageDialog(mainWindow, "File " + fileToSaveTo.getPath() + " saved successfully", "DB backup succeeded",
										JOptionPane.INFORMATION_MESSAGE);
								if (callbackOnSwingEDT != null) {
									callbackOnSwingEDT.run();
								}
							}
						});
					} catch (final Exception ex) {
						ex.printStackTrace();
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								btnBackupDb.setEnabled(true);
								btnRestoreDb.setEnabled(true);
								JOptionPane.showMessageDialog(mainWindow, "DB backup failed: " + ex.getClass().getCanonicalName() + " " + ex.getMessage(),
										"Error occurred", JOptionPane.ERROR_MESSAGE);
							}
						});
					} finally {
						IOUtils.closeSilently(fis);
					}
				}
			}.start();
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
