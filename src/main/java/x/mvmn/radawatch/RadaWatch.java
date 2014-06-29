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

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.h2.tools.RunScript;
import org.h2.tools.Script;
import org.h2.util.IOUtils;
import org.h2.util.JdbcUtils;

import x.mvmn.lang.StringDisplay;
import x.mvmn.radawatch.gui.analyze.DeputeesStatsPanel;
import x.mvmn.radawatch.gui.analyze.TitlesAnalysisPanel;
import x.mvmn.radawatch.gui.browse.DataBrowser;
import x.mvmn.radawatch.gui.fetch.FetchPanel;
import x.mvmn.radawatch.gui.stats.StatsPanel;
import x.mvmn.radawatch.model.presdecrees.PresidentialDecree;
import x.mvmn.radawatch.model.radavotes.IndividualDeputyVoteData;
import x.mvmn.radawatch.model.radavotes.VoteSessionPerFactionData;
import x.mvmn.radawatch.model.radavotes.VoteSessionResultsData;
import x.mvmn.radawatch.service.analyze.DeputeesStatsAnalyzer;
import x.mvmn.radawatch.service.db.DataBaseConnectionService;
import x.mvmn.radawatch.service.db.presdecrees.PresidentialDecreesAggregationService;
import x.mvmn.radawatch.service.db.presdecrees.PresidentialDecreesBrowseService;
import x.mvmn.radawatch.service.db.presdecrees.PresidentialDecreesStorageService;
import x.mvmn.radawatch.service.db.radavotes.RadaIndividualVotesBrowseService;
import x.mvmn.radawatch.service.db.radavotes.RadaVoteSessionPerFactionResultsBrowseService;
import x.mvmn.radawatch.service.db.radavotes.RadaVoteSessionResultsBrowseService;
import x.mvmn.radawatch.service.db.radavotes.RadaVotesAggregationService;
import x.mvmn.radawatch.service.db.radavotes.RadaVotesStorageService;
import x.mvmn.radawatch.service.parse.presdecrees.PredisentialDecreesParser;
import x.mvmn.radawatch.service.parse.radavotes.VoteResultsParser;
import x.mvmn.radawatch.swing.EmptyWindowListener;
import x.mvmn.radawatch.swing.SwingHelper;

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
	private final JButton btnBrowseDb = new JButton("Browse DB");
	private final JButton btnBackupDb = new JButton("Backup DB");
	private final JButton btnRestoreDb = new JButton("Restore DB");
	private final RadaVotesStorageService rvStorage = new RadaVotesStorageService(storageService);
	private final PresidentialDecreesStorageService pdStorage = new PresidentialDecreesStorageService(storageService);
	private final FetchController<VoteSessionResultsData> votesFetchController = new FetchController<VoteSessionResultsData>(new VoteResultsParser(),
			rvStorage, new FetchPanel<VoteSessionResultsData>("Rada Votes", rvStorage), mainWindow);
	private final FetchController<PresidentialDecree> presDecreesFetchController = new FetchController<PresidentialDecree>(new PredisentialDecreesParser(),
			pdStorage, new FetchPanel<PresidentialDecree>("Presidential Decrees", pdStorage), mainWindow);

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
		final JPanel tabBrowse = new JPanel(new BorderLayout());
		final JPanel tabAnalyze = new JPanel(new BorderLayout());
		final JPanel tabStats = new JPanel(new BorderLayout());
		tabPane.addTab("Fetch", tabFetch);
		tabPane.addTab("Browse", tabBrowse);
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
							presDecreesFetchController.setControlsEnabled(false);
							new Thread() {
								public void run() {
									FileReader fis = null;
									Connection conn = null;
									try {
										votesFetchController.getStorage().dropAllTables();
										presDecreesFetchController.getStorage().dropAllTables();
										conn = storageService.getConnection();
										fis = new FileReader(fileToLoadFrom);
										RunScript.execute(conn, fis);
										SwingUtilities.invokeLater(new Runnable() {
											@Override
											public void run() {
												btnRestoreDb.setEnabled(true);
												btnBackupDb.setEnabled(true);
												votesFetchController.setControlsEnabled(true);
												presDecreesFetchController.setControlsEnabled(true);

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
												presDecreesFetchController.setControlsEnabled(true);

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

		{
			JTabbedPane statsTabs = new JTabbedPane();
			tabStats.add(statsTabs, BorderLayout.CENTER);
			statsTabs.addTab("Rada votes", new StatsPanel(new RadaVotesAggregationService(storageService)));
			statsTabs.addTab("Presidential decrees", new StatsPanel(new PresidentialDecreesAggregationService(storageService)));
			statsTabs.addTab("Deputees stats", new DeputeesStatsPanel(new DeputeesStatsAnalyzer(storageService)));
		}

		JTabbedPane analyzeSubtabs = new JTabbedPane();
		tabAnalyze.add(analyzeSubtabs, BorderLayout.CENTER);

		{
			JTabbedPane tabBrowseSubtabs = new JTabbedPane();
			tabBrowse.add(tabBrowseSubtabs, BorderLayout.CENTER);
			// tabBrowseSubtabs.addTab(votesFetchController.getDataTitle(), votesFetchController.getView());

			{
				final DataBrowser<IndividualDeputyVoteData> individualVotesBrowser = new DataBrowser<IndividualDeputyVoteData>("Deputy vote ",
						new RadaIndividualVotesBrowseService(storageService), -1, new RadaIndividualVotesBrowseService.IndividualDeputyVoteViewAdaptor(), null);
				final DataBrowser<VoteSessionPerFactionData> votesPerFactionsBrowser = new DataBrowser<VoteSessionPerFactionData>(
						"Vote Session Results Per Faction", new RadaVoteSessionPerFactionResultsBrowseService(storageService), -1,
						new RadaVoteSessionPerFactionResultsBrowseService.RadaVotesPerFactionViewAdaptor(), individualVotesBrowser);
				final DataBrowser<VoteSessionResultsData> voteSessionsDataBrowser = new DataBrowser<VoteSessionResultsData>("Rada votes",
						new RadaVoteSessionResultsBrowseService(storageService), -1, new RadaVoteSessionResultsBrowseService.RadaVotesViewAdaptor(),
						votesPerFactionsBrowser);
				tabBrowseSubtabs.addTab(votesFetchController.getDataTitle(), voteSessionsDataBrowser);
				analyzeSubtabs.addTab("Analyze Rada Votes Titles", new TitlesAnalysisPanel<VoteSessionResultsData>(voteSessionsDataBrowser,
						new StringDisplay<VoteSessionResultsData>() {
							public String getStringDisplay(VoteSessionResultsData item) {
								final StringBuilder result = new StringBuilder(item.getTitle());
								result.append(" /").append(item.getDate().toString()).append(" / ")
										.append(item.getResult().booleanValue() ? "<прийнято> " : "<НЕ прийнято> ");
								result.append(String.valueOf(item.getVotedYes())).append("/").append(String.valueOf(item.getVotedNo()));
								result.append(" (").append(String.valueOf(item.getTotal())).append(")");

								return result.toString();
							}
						}, mainWindow));
			}
			{
				final DataBrowser<PresidentialDecree> presidentialDecreesDataBrowser = new DataBrowser<PresidentialDecree>("Presidential decrees",
						new PresidentialDecreesBrowseService(storageService), -1, new PresidentialDecreesBrowseService.PresidentialDecreesViewAdaptor(), null);
				tabBrowseSubtabs.addTab(presDecreesFetchController.getDataTitle(), presidentialDecreesDataBrowser);
				analyzeSubtabs.addTab("Analyze Presidential Decrees Titles", new TitlesAnalysisPanel<PresidentialDecree>(presidentialDecreesDataBrowser,
						new StringDisplay<PresidentialDecree>() {
							@Override
							public String getStringDisplay(PresidentialDecree item) {
								// concat(decreetype,': ', title, '. /', reldate,'/ ', numcode) ";
								return new StringBuilder(item.getType()).append(": ").append(item.getTitle()).append(". /").append(item.getDate().toString())
										.append("/ ").append(item.getNumberCode()).toString();
							}
						}, mainWindow));
			}
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
		SwingHelper.resizeToScreenProportions(mainWindow, 0.7d);
		SwingHelper.moveToScreenCenter(mainWindow);
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
								btnRestoreDb.setEnabled(true);
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
