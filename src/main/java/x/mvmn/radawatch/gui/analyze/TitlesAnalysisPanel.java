package x.mvmn.radawatch.gui.analyze;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import x.mvmn.radawatch.service.analyze.TitlesAnalyzisHelper;
import x.mvmn.radawatch.service.analyze.TitlesAnalyzisHelper.TextNode;
import x.mvmn.radawatch.service.analyze.TitlesExtractor;
import x.mvmn.radawatch.service.db.DataBrowseQuery;

public class TitlesAnalysisPanel extends JPanel {

	private static final long serialVersionUID = -1709665611976801927L;

	protected final TitlesExtractor titlesAnalyzer;

	protected final FilterPanel filterPanel;

	public TitlesAnalysisPanel(final TitlesExtractor titlesAnalyzer, final Component parentComponent) {
		super(new BorderLayout());
		// TODO: Refactor
		this.titlesAnalyzer = titlesAnalyzer;
		final JButton btnAnalyzeTitles = new JButton("Analyze titles");

		filterPanel = new FilterPanel();

		this.add(btnAnalyzeTitles, BorderLayout.SOUTH);
		this.add(filterPanel, BorderLayout.NORTH);
		final JPanel resultsPanel = new JPanel(new BorderLayout());
		this.add(resultsPanel, BorderLayout.CENTER);
		btnAnalyzeTitles.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				btnAnalyzeTitles.setEnabled(false);

				new Thread() {
					public void run() {
						try {
							final List<String> titles = titlesAnalyzer.getTitles(new DataBrowseQuery(filterPanel.getSearchText(), null, null, filterPanel
									.getDateFrom(), filterPanel.getDateTo()));
							final TextNode rootNode = TitlesAnalyzisHelper.mapTitlesToTreeNodes(titles);
							final TitlesTree titlesTree = new TitlesTree(rootNode.getChildren());

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
									JOptionPane.showMessageDialog(parentComponent, ex.getClass().getCanonicalName() + " " + ex.getMessage(), "Error occurred",
											JOptionPane.ERROR_MESSAGE);
								}
							});
						}
					}
				}.start();
			}
		});
	}
}
