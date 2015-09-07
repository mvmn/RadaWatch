package x.mvmn.radawatch;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import x.mvmn.lang.StringDisplay;
import x.mvmn.radawatch.gui.DBConnectionDialog;
import x.mvmn.radawatch.gui.analyze.DeputeeFactionParticipationPanel;
import x.mvmn.radawatch.gui.analyze.DeputeesDissentPanel;
import x.mvmn.radawatch.gui.analyze.DeputeesStatsPanel;
import x.mvmn.radawatch.gui.analyze.FactionsDissentPanel;
import x.mvmn.radawatch.gui.analyze.FactionsDissentingLawsPanel;
import x.mvmn.radawatch.gui.analyze.TitlesAnalysisPanel;
import x.mvmn.radawatch.gui.browse.DataBrowser;
import x.mvmn.radawatch.gui.fetch.FetchPanel;
import x.mvmn.radawatch.gui.stats.StatsPanel;
import x.mvmn.radawatch.model.presdecrees.PresidentialDecree;
import x.mvmn.radawatch.model.radavotes.IndividualDeputyVoteData;
import x.mvmn.radawatch.model.radavotes.VoteSessionPerFactionData;
import x.mvmn.radawatch.model.radavotes.VoteSessionResultsData;
import x.mvmn.radawatch.service.analyze.DeputeesFactionsParticipationAnalyzer;
import x.mvmn.radawatch.service.analyze.DeputeesStatsAnalyzer;
import x.mvmn.radawatch.service.db.DataBaseConnectionService;
import x.mvmn.radawatch.service.db.presdecrees.PresidentialDecreesAggregationService;
import x.mvmn.radawatch.service.db.presdecrees.PresidentialDecreesBrowseService;
import x.mvmn.radawatch.service.db.presdecrees.PresidentialDecreesStorageService;
import x.mvmn.radawatch.service.db.radavotes.RadaFactionsVotesAggregationService;
import x.mvmn.radawatch.service.db.radavotes.RadaIndividualVotesAggregationService;
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
	private final DataBaseConnectionService storageService;
	private final RadaVotesStorageService rvStorage;
	private final PresidentialDecreesStorageService pdStorage;
	private final FetchController<VoteSessionResultsData> votesFetchController;
	private final FetchController<VoteSessionResultsData> votesFetchControllerSeven;
	private final FetchController<PresidentialDecree> presDecreesFetchController;

	public RadaWatch() {
		final DBConnectionDialog dbConnectionDialog = new DBConnectionDialog();
		dbConnectionDialog.showInput();

		storageService = new DataBaseConnectionService(dbConnectionDialog.useEmbeddedDb(), dbConnectionDialog.getDbHost(), dbConnectionDialog.getDbPort(),
				dbConnectionDialog.getDbName(), dbConnectionDialog.getLogin(), dbConnectionDialog.getPasswordOnce(), mainWindow.getContentPane());
		rvStorage = new RadaVotesStorageService(storageService);
		pdStorage = new PresidentialDecreesStorageService(storageService);
		votesFetchController = new FetchController<VoteSessionResultsData>(new VoteResultsParser(false), rvStorage, new FetchPanel<VoteSessionResultsData>(
				"Rada Votes VIII", rvStorage), mainWindow);
		votesFetchControllerSeven = new FetchController<VoteSessionResultsData>(new VoteResultsParser(true), rvStorage, new FetchPanel<VoteSessionResultsData>(
				"Rada Votes VII", rvStorage), mainWindow);
		presDecreesFetchController = new FetchController<PresidentialDecree>(new PredisentialDecreesParser(), pdStorage, new FetchPanel<PresidentialDecree>(
				"Presidential Decrees", pdStorage), mainWindow);

		mainWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainWindow.addWindowListener(new EmptyWindowListener() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				RadaWatch.this.closeRequest();
			}
		});

		final JTabbedPane tabPane = new JTabbedPane();
		final JPanel tabFetch = new JPanel(new BorderLayout());
		final JPanel tabBrowse = new JPanel(new BorderLayout());
		final JPanel tabAnalyze = new JPanel(new BorderLayout());
		final JPanel tabStats = new JPanel(new BorderLayout());
		tabPane.addTab("Fetch", tabFetch);
		tabPane.addTab("Browse", tabBrowse);
		tabPane.addTab("Analyze", tabAnalyze);
		tabPane.addTab("Stats", tabStats);

		{
			JTabbedPane statsTabs = new JTabbedPane();
			tabStats.add(statsTabs, BorderLayout.CENTER);
			statsTabs.addTab("Rada votes", new StatsPanel(new RadaVotesAggregationService(storageService)));
			statsTabs.addTab("Rada votes by Factions", new StatsPanel(new RadaFactionsVotesAggregationService(storageService)));
			statsTabs.addTab("Rada votes by Deputees", new StatsPanel(new RadaIndividualVotesAggregationService(storageService)));
			statsTabs.addTab("Presidential decrees", new StatsPanel(new PresidentialDecreesAggregationService(storageService)));
			statsTabs.addTab("Deputees stats", new DeputeesStatsPanel(new DeputeesStatsAnalyzer(storageService)));
			statsTabs.addTab("Deputees factions", new DeputeeFactionParticipationPanel(new DeputeesFactionsParticipationAnalyzer(storageService)));
		}

		JTabbedPane analyzeSubtabs = new JTabbedPane();
		tabAnalyze.add(analyzeSubtabs, BorderLayout.CENTER);

		{
			JTabbedPane tabBrowseSubtabs = new JTabbedPane();
			tabBrowse.add(tabBrowseSubtabs, BorderLayout.CENTER);
			// tabBrowseSubtabs.addTab(votesFetchController.getDataTitle(), votesFetchController.getView());

			final DataBrowser<VoteSessionResultsData> voteSessionsDataBrowser;
			{
				final DataBrowser<IndividualDeputyVoteData> individualVotesBrowser = new DataBrowser<IndividualDeputyVoteData>("Deputy vote ",
						new RadaIndividualVotesBrowseService(storageService), -1, new RadaIndividualVotesBrowseService.IndividualDeputyVoteViewAdaptor(), null);
				final DataBrowser<VoteSessionPerFactionData> votesPerFactionsBrowser = new DataBrowser<VoteSessionPerFactionData>(
						"Vote Session Results Per Faction", new RadaVoteSessionPerFactionResultsBrowseService(storageService), -1,
						new RadaVoteSessionPerFactionResultsBrowseService.RadaVotesPerFactionViewAdaptor(), individualVotesBrowser);
				voteSessionsDataBrowser = new DataBrowser<VoteSessionResultsData>("Rada votes", new RadaVoteSessionResultsBrowseService(storageService), -1,
						new RadaVoteSessionResultsBrowseService.RadaVotesViewAdaptor(), votesPerFactionsBrowser);
				tabBrowseSubtabs.addTab("Rada votes", voteSessionsDataBrowser);
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
			analyzeSubtabs.addTab("Deputees Dissent", new DeputeesDissentPanel(storageService));
			analyzeSubtabs.addTab("Factions Dissent", new FactionsDissentPanel(storageService));
			analyzeSubtabs.addTab("Factions Dissenting Laws", new FactionsDissentingLawsPanel(storageService, voteSessionsDataBrowser));
		}

		{
			JTabbedPane tabFetchSubtabs = new JTabbedPane();
			tabFetch.add(tabFetchSubtabs, BorderLayout.CENTER);
			tabFetchSubtabs.addTab(votesFetchController.getDataTitle(), votesFetchController.getView());
			tabFetchSubtabs.addTab(votesFetchControllerSeven.getDataTitle(), votesFetchControllerSeven.getView());
			tabFetchSubtabs.addTab(presDecreesFetchController.getDataTitle(), presDecreesFetchController.getView());
		}

		mainWindow.getContentPane().setLayout(new BorderLayout());
		mainWindow.getContentPane().add(tabPane, BorderLayout.CENTER);
		{
			JPanel dbPanel = new JPanel(new GridLayout(storageService.getDbOpsNames().length + 1, 1));
			dbPanel.add(new JScrollPane(new JLabel(storageService.getDbInfo())));
			for (final String dbOpName : storageService.getDbOpsNames()) {
				final JButton btnDbOp = new JButton(dbOpName);
				btnDbOp.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent actListener) {
						storageService.getDbOp(dbOpName).run();
					}
				});
				dbPanel.add(btnDbOp);
			}

			// mainWindow.getContentPane().add(btnPanel, BorderLayout.SOUTH);
			tabPane.addTab("DataBase", dbPanel);
		}
		mainWindow.pack();
		SwingHelper.resizeToScreenProportions(mainWindow, 0.7d);
		SwingHelper.moveToScreenCenter(mainWindow);
		mainWindow.setVisible(true);
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
