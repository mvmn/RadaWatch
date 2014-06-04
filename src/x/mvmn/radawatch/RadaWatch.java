package x.mvmn.radawatch;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

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
		btnBrowseDb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				storageService.openDbBrowser();
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
		mainWindow.setLayout(new BorderLayout());
		{
			JPanel btnPanel = new JPanel(new BorderLayout());
			btnPanel.add(btnBrowseDb, BorderLayout.WEST);
			btnPanel.add(btnRecreateDb, BorderLayout.EAST);
			btnPanel.add(btnFetch, BorderLayout.CENTER);
			mainWindow.add(btnPanel, BorderLayout.SOUTH);
		}
		{
			prbFetch.setIndeterminate(true);
			prbFetch.setEnabled(false);
			JPanel progressPanel = new JPanel(new BorderLayout());
			progressPanel.add(new JLabel("Fetch progress"), BorderLayout.WEST);
			progressPanel.add(prbFetch, BorderLayout.CENTER);
			progressPanel.add(btnStop, BorderLayout.EAST);
			mainWindow.add(progressPanel, BorderLayout.NORTH);
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
		mainWindow.add(new JScrollPane(txaLog), BorderLayout.CENTER);

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
			prbFetch.setMaximum(parser.getLastPageNumber() + 3);
			prbFetch.setValue(0);
		}

		public void stop() {
			this.stopRequested = true;
		}

		public void run() {
			try {
				for (int i = 1; i < parser.getLastPageNumber() + 3 && !stopRequested; i++) {
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
