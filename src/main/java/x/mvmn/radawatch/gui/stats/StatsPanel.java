package x.mvmn.radawatch.gui.stats;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import x.mvmn.radawatch.gui.analyze.FilterPanel;
import x.mvmn.radawatch.service.db.DataAggregationService;
import x.mvmn.radawatch.service.db.DataAggregationService.AggregationInterval;
import x.mvmn.radawatch.service.db.DataBrowseQuery;

public class StatsPanel extends JPanel {
	private static final long serialVersionUID = -5460048410145768494L;

	protected final DataAggregationService daService;

	protected final FilterPanel filterPanel;

	protected final JButton performAggregation = new JButton("Perform");

	protected final JTextArea aggregationResults = new JTextArea();

	protected volatile AggregationInterval selectedAggregationInterval = AggregationInterval.YEAR;

	protected final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public StatsPanel(final DataAggregationService daService) {
		super(new BorderLayout());
		this.daService = daService;
		this.filterPanel = new FilterPanel(daService.supportsDateFilter(), daService.supportsTitleFilter());
		this.add(filterPanel, BorderLayout.NORTH);
		final JPanel middlePanel = new JPanel(new BorderLayout());
		this.add(middlePanel, BorderLayout.CENTER);
		this.add(performAggregation, BorderLayout.SOUTH);

		middlePanel.add(new JScrollPane(aggregationResults), BorderLayout.CENTER);
		final JPanel intervalSelectionPanel = new JPanel(new GridLayout(AggregationInterval.values().length, 1));
		middlePanel.add(new JScrollPane(intervalSelectionPanel), BorderLayout.EAST);
		intervalSelectionPanel.setBorder(BorderFactory.createTitledBorder("Group by"));
		final ButtonGroup buttonGroup = new ButtonGroup();
		for (final AggregationInterval interval : AggregationInterval.values()) {
			final JRadioButton rbInterval = new JRadioButton(interval.name().toLowerCase());
			if (selectedAggregationInterval == interval) {
				rbInterval.setSelected(true);
			}
			buttonGroup.add(rbInterval);
			intervalSelectionPanel.add(rbInterval);
			rbInterval.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					selectedAggregationInterval = interval;
				}
			});
		}

		performAggregation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				performAggregation.setEnabled(false);
				new Thread() {
					public void run() {
						try {
							final Map<Date, Map<String, Integer>> results = daService.getAggregatedCounts(daService.getAvailableMetrics(),
									selectedAggregationInterval, new DataBrowseQuery(filterPanel.getSearchText(), null, null, filterPanel.getDateFrom(),
											filterPanel.getDateTo()));
							final StringBuilder resultText = new StringBuilder();
							for (Map.Entry<Date, Map<String, Integer>> byDateEntries : results.entrySet()) {
								final Date key = byDateEntries.getKey();
								final Map<String, Integer> values = byDateEntries.getValue();
								resultText.append(" ---- ").append(dateFormat.format(key)).append(" ---- \n");
								for (Map.Entry<String, Integer> entry : values.entrySet()) {
									String metricName = entry.getKey();
									if ("".equals(metricName)) {
										metricName = "Total";
									}
									resultText.append(metricName).append(": ").append(entry.getValue().toString()).append("\n");
								}
								resultText.append("\n");
							}
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									aggregationResults.setText(resultText.toString());
								}
							});
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									performAggregation.setEnabled(true);
								}
							});
						}
					}
				}.start();
			}
		});
	}
}
