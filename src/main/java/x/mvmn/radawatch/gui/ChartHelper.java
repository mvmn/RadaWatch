package x.mvmn.radawatch.gui;

import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

public final class ChartHelper {

	private ChartHelper() {
	}

	public static ChartPanel fromTableModel(final TableModel tableModel, final String labelX, final String labelY) {
		final DefaultCategoryDataset mainDataset = new DefaultCategoryDataset();

		final List<Integer> numericColumns = new ArrayList<Integer>(tableModel.getColumnCount());
		final List<Integer> nonNumericColumns = new ArrayList<Integer>(tableModel.getColumnCount());
		for (int i = 0; i < tableModel.getColumnCount(); i++) {
			if (Number.class.isAssignableFrom(tableModel.getColumnClass(i))) {
				numericColumns.add(i);
			} else {
				nonNumericColumns.add(i);
			}
		}

		final Integer[] nc = numericColumns.toArray(new Integer[numericColumns.size()]);
		final Integer[] nnc = nonNumericColumns.toArray(new Integer[nonNumericColumns.size()]);

		for (int r = 0; r < tableModel.getRowCount(); r++) {
			final StringBuilder sb = new StringBuilder();
			for (int c = 0; c < nnc.length; c++) {
				sb.append(tableModel.getColumnName(nnc[c])).append(": ").append(tableModel.getValueAt(r, nnc[c])).append(" ");
			}
			for (int c = 0; c < nc.length; c++) {
				mainDataset.addValue((Number) tableModel.getValueAt(r, nc[c]), tableModel.getColumnName(nc[c]), sb.toString());
			}
		}

		final JFreeChart chart = ChartFactory.createBarChart("", labelX, labelY, mainDataset, PlotOrientation.VERTICAL, true, true, false);

		final ChartPanel chartPanel = new ChartPanel(chart);

		return chartPanel;
	}

	public static void updateCategoryChartRenderParameters(final ChartPanel chartPanel, final Container container) {
		final CategoryPlot plot = ((CategoryPlot) chartPanel.getChart().getPlot());
		final DefaultCategoryDataset mainDataset = (DefaultCategoryDataset) plot.getDataset();
		{
			plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.DOWN_90);
			for (int i = 0; i < mainDataset.getRowCount(); i++) {
				plot.getRenderer().setSeriesItemLabelGenerator(i, new StandardCategoryItemLabelGenerator());
				plot.getRenderer().setSeriesItemLabelsVisible(i, true);
			}
			((BarRenderer) plot.getRenderer()).setItemMargin(0.5d / mainDataset.getColumnCount());
		}
		int oneBarWidth = (int) (container.getGraphics().getFontMetrics().getHeight() * 1.2);
		if (oneBarWidth * mainDataset.getColumnCount() * mainDataset.getRowCount() > container.getPreferredSize().width) {
			chartPanel.setMinimumDrawWidth(oneBarWidth * mainDataset.getColumnCount() * mainDataset.getRowCount());
			chartPanel.setMaximumDrawWidth(oneBarWidth * mainDataset.getColumnCount() * mainDataset.getRowCount());
			chartPanel.setPreferredSize(new java.awt.Dimension(oneBarWidth * mainDataset.getColumnCount() * mainDataset.getRowCount(), (int) chartPanel
					.getPreferredSize().height));
		}

	}
}
