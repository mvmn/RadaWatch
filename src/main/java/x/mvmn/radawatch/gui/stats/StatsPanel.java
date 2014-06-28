package x.mvmn.radawatch.gui.stats;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import x.mvmn.radawatch.gui.analyze.FilterPanel;
import x.mvmn.radawatch.service.db.DataAggregationService;
import x.mvmn.radawatch.service.db.DataAggregationService.AggregationInterval;
import x.mvmn.radawatch.service.db.DataBrowseQuery;

public class StatsPanel extends JPanel {
	private static final long serialVersionUID = -5460048410145768494L;

	protected final DataAggregationService daService;

	protected final FilterPanel filterPanel;

	protected final JButton performAggregation = new JButton("Perform");

	protected volatile AggregationInterval selectedAggregationInterval = AggregationInterval.YEAR;

	protected final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	protected final List<JCheckBox> metricsCheckboxes = new ArrayList<JCheckBox>();

	protected final JPanel middlePanel = new JPanel(new BorderLayout());

	protected volatile Component currentChartPanelHolder = null;

	public StatsPanel(final DataAggregationService daService) {
		super(new BorderLayout());
		this.daService = daService;
		this.filterPanel = new FilterPanel(daService.supportsDateFilter(), daService.supportsTitleFilter());
		this.add(filterPanel, BorderLayout.NORTH);

		this.add(middlePanel, BorderLayout.CENTER);
		this.add(performAggregation, BorderLayout.SOUTH);

		if (daService.getSupportedMetrics() != null && daService.getSupportedMetrics().size() > 0) {
			JPanel metricSelectionPanel = new JPanel(new GridLayout(daService.getSupportedMetrics().size(), 1));
			for (final String metricName : daService.getSupportedMetrics()) {
				JCheckBox metricCheckBox = new JCheckBox(metricName);
				metricCheckBox.setSelected(true);
				metricsCheckboxes.add(metricCheckBox);
				metricSelectionPanel.add(metricCheckBox);
			}
			middlePanel.add(new JScrollPane(metricSelectionPanel), BorderLayout.EAST);
		}

		final JPanel intervalSelectionPanel = new JPanel(new GridLayout(AggregationInterval.values().length, 1));
		middlePanel.add(new JScrollPane(intervalSelectionPanel), BorderLayout.WEST);
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
							final AggregationInterval interval = selectedAggregationInterval;
							final List<String> metrics = getEnabledMetrics();
							final Map<Date, Map<String, Integer>> results = daService.getAggregatedCounts(metrics, interval,
									new DataBrowseQuery(filterPanel.getSearchText(), null, null, filterPanel.getDateFrom(), filterPanel.getDateTo()));

							final DefaultCategoryDataset mainDataset = new DefaultCategoryDataset();
							if (results.size() > 0) {
								final Date lastDate = filterPanel.getDateTo() != null ? filterPanel.getDateTo() : getMaxDate(results);
								Date date = moveToBeginnigOfInterval(filterPanel.getDateFrom() != null ? filterPanel.getDateFrom() : getMinDate(results),
										interval);
								while (date.before(lastDate)) {
									final Map<String, Integer> valuesForDate = results.get(date);
									mainDataset.addValue(valuesForDate != null ? valuesForDate.get("") : 0, "Total", dateFormat.format(date));
									for (final String metricName : metrics) {
										mainDataset.addValue(valuesForDate != null ? valuesForDate.get(metricName) : 0, metricName, dateFormat.format(date));
									}
									date = advanceDateByInterval(date, interval, 1);
								}
							}
							JFreeChart chart = ChartFactory.createBarChart("", "Date", "Value", mainDataset, PlotOrientation.VERTICAL, true, true, false);
							{
								CategoryPlot plot = ((CategoryPlot) chart.getPlot());
								plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.DOWN_90);
								plot.getRenderer().setSeriesItemLabelGenerator(0, new StandardCategoryItemLabelGenerator());
								plot.getRenderer().setSeriesItemLabelsVisible(0, true);
								((BarRenderer) plot.getRenderer()).setItemMargin(0.5d / mainDataset.getColumnCount());
							}
							final ChartPanel chartPanel = new ChartPanel(chart);
							final Component newChartPanelHolder = new JScrollPane(chartPanel);

							int oneBarWidth = (int) (getGraphics().getFontMetrics().getHeight() * 1.2);
							if (oneBarWidth * mainDataset.getColumnCount() * mainDataset.getRowCount() > middlePanel.getPreferredSize().width) {
								chartPanel.setMinimumDrawWidth(oneBarWidth * mainDataset.getColumnCount() * mainDataset.getRowCount());
								chartPanel.setMaximumDrawWidth(oneBarWidth * mainDataset.getColumnCount() * mainDataset.getRowCount());
								chartPanel.setPreferredSize(new java.awt.Dimension(oneBarWidth * mainDataset.getColumnCount() * mainDataset.getRowCount(),
										(int) newChartPanelHolder.getPreferredSize().height));
							}

							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									if (currentChartPanelHolder != null) {
										middlePanel.remove(currentChartPanelHolder);
									}
									middlePanel.add(newChartPanelHolder, BorderLayout.CENTER);
									currentChartPanelHolder = newChartPanelHolder;
									middlePanel.invalidate();
									middlePanel.revalidate();
									middlePanel.repaint();
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

	protected Date getMinDate(Map<Date, Map<String, Integer>> results) {
		Date result = null;
		final Iterator<Date> iterator = results.keySet().iterator();
		while (iterator.hasNext()) {
			Date newDate = iterator.next();
			if (result == null || result.after(newDate)) {
				result = newDate;
			}
		}
		return result;
	}

	protected Date getMaxDate(Map<Date, Map<String, Integer>> results) {
		Date result = null;
		final Iterator<Date> iterator = results.keySet().iterator();
		while (iterator.hasNext()) {
			Date newDate = iterator.next();
			if (result == null || result.before(newDate)) {
				result = newDate;
			}
		}
		return result;
	}

	public List<String> getEnabledMetrics() {
		List<String> result = Collections.emptyList();

		if (metricsCheckboxes != null && metricsCheckboxes.size() > 0) {
			result = new ArrayList<String>(metricsCheckboxes.size());
			for (final JCheckBox checkBox : metricsCheckboxes) {
				if (checkBox.isSelected()) {
					result.add(checkBox.getText());
				}
			}
		}

		return result;
	}

	protected Date moveToBeginnigOfInterval(final Date date, final AggregationInterval interval) {
		Calendar cal = Calendar.getInstance();
		cal.setFirstDayOfWeek(Calendar.MONDAY);
		cal.setTime(date);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);

		switch (interval) {
			case YEAR:
				cal.set(Calendar.MONTH, 0);
			case QUARTER:
				cal.set(Calendar.MONTH, ((int) Math.floor(cal.get(Calendar.MONTH) / 3.0d)) * 3);
			case MONTH:
				cal.set(Calendar.DAY_OF_MONTH, 1);
			case WEEK:
				if (interval == AggregationInterval.WEEK) {
					cal.set(Calendar.WEEK_OF_YEAR, cal.get(Calendar.WEEK_OF_YEAR));
					cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
				}
			case DAY:
				cal.set(Calendar.HOUR, 0);
			case HOUR:
			default:
		}

		return cal.getTime();
	}

	protected Date advanceDateByInterval(final Date date, final AggregationInterval interval, int amount) {
		Calendar cal = Calendar.getInstance();
		cal.setFirstDayOfWeek(Calendar.MONDAY);
		cal.setTime(date);

		final int calendarField;
		switch (interval) {
			case HOUR:
				calendarField = Calendar.HOUR_OF_DAY;
			break;
			case DAY:
				calendarField = Calendar.DAY_OF_MONTH;
			break;
			case WEEK:
				calendarField = Calendar.WEEK_OF_YEAR;
			break;
			case MONTH:
				calendarField = Calendar.MONTH;
			break;
			case QUARTER:
				calendarField = Calendar.MONTH;
				amount = amount * 3;
			break;
			default:
			case YEAR:
				calendarField = Calendar.YEAR;
		}

		cal.add(calendarField, amount);

		return cal.getTime();
	}
}
