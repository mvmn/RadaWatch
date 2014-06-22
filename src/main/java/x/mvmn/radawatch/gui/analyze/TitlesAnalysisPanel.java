package x.mvmn.radawatch.gui.analyze;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.sourceforge.jdatepicker.impl.JDatePanelImpl;
import net.sourceforge.jdatepicker.impl.JDatePickerImpl;
import net.sourceforge.jdatepicker.impl.UtilDateModel;
import x.mvmn.radawatch.service.analyze.TitlesExtractor;
import x.mvmn.radawatch.service.analyze.TitlesAnalyzisHelper;
import x.mvmn.radawatch.service.analyze.TitlesAnalyzisHelper.TextNode;
import x.mvmn.radawatch.service.db.DataBrowseQuery;

public class TitlesAnalysisPanel extends JPanel {

	private static final long serialVersionUID = -1709665611976801927L;

	protected final TitlesExtractor titlesAnalyzer;

	public TitlesAnalysisPanel(final TitlesExtractor titlesAnalyzer, final Component parentComponent) {
		super(new BorderLayout());
		// TODO: Refactor
		this.titlesAnalyzer = titlesAnalyzer;
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

		this.add(btnAnalyzeTitles, BorderLayout.SOUTH);
		this.add(datesPanel, BorderLayout.NORTH);
		final JPanel resultsPanel = new JPanel(new BorderLayout());
		this.add(resultsPanel, BorderLayout.CENTER);
		btnAnalyzeTitles.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				btnAnalyzeTitles.setEnabled(false);

				new Thread() {
					public void run() {
						try {
							final Date fromDate = (Date) datePickerFrom.getModel().getValue();
							final Date toDate = (Date) datePickerTo.getModel().getValue();
							final List<String> titles = titlesAnalyzer.getTitles(new DataBrowseQuery(tfTitleFilter.getText(), null, null, fromDate, toDate));
							final TextNode rootNode = TitlesAnalyzisHelper.mapTitlesToTreeNodes(titles);
							// TitlesAnalyzisHelper.mapTitles(titles);
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
