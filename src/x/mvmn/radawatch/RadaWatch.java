package x.mvmn.radawatch;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import x.mvmn.radawatch.service.db.StorageService;
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
	private final JButton btnRecreateDb = new JButton("Re-create DB");
	private final JButton btnBrowseDb = new JButton("Browse DB");

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
					storageService.dropTables();
					storageService.createTables();
				} catch (Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(mainWindow, ex.getClass().getCanonicalName() + " " + ex.getMessage(), "Error occurred",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		// ----- //
		mainWindow.setLayout(new BorderLayout());
		JPanel btnPanel = new JPanel();
		btnPanel.add(btnBrowseDb);
		btnPanel.add(btnRecreateDb);
		mainWindow.add(btnPanel, BorderLayout.SOUTH);

		mainWindow.pack();
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
}
