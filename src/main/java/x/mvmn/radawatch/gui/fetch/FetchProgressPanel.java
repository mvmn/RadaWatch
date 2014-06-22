package x.mvmn.radawatch.gui.fetch;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class FetchProgressPanel extends JPanel implements PagedFetchProgressIndicator {

	private static final long serialVersionUID = 8917864549504416890L;

	private static final String PAGES_PROGRESS_LABEL_STR_PATTERN = "Pages %s/%s";
	private static final String ITEMS_PROGRESS_LABEL_STR_PATTERN = "Page items %s/%s";

	protected final JProgressBar pagesProgressBar = new JProgressBar(JProgressBar.HORIZONTAL);
	protected final JProgressBar itemsProgressBar = new JProgressBar(JProgressBar.HORIZONTAL);
	protected final JLabel pagesProgressLabel = new JLabel(String.format(PAGES_PROGRESS_LABEL_STR_PATTERN, "?", "?"));
	protected final JLabel itemsProgressLabel = new JLabel(String.format(ITEMS_PROGRESS_LABEL_STR_PATTERN, "?", "?"));

	public FetchProgressPanel(String title) {
		super(new BorderLayout());
		JLabel titleLabel = new JLabel(title);
		titleLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 6));
		this.add(titleLabel, BorderLayout.WEST);
		JPanel progressPanel = new JPanel(new GridLayout(2, 1));
		progressPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 6));
		progressPanel.add(pagesProgressBar);
		progressPanel.add(itemsProgressBar);
		JPanel labelsPanel = new JPanel(new GridLayout(2, 1));
		// labelsPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 6));
		labelsPanel.add(pagesProgressLabel);
		labelsPanel.add(itemsProgressLabel);
		this.add(progressPanel, BorderLayout.CENTER);
		this.add(labelsPanel, BorderLayout.EAST);
		reset();
	}

	@Override
	public void reset() {
		pagesProgressLabel.setText(String.format(PAGES_PROGRESS_LABEL_STR_PATTERN, "?", "?"));
		pagesProgressLabel.setEnabled(false);
		pagesProgressBar.setIndeterminate(true);
		pagesProgressBar.setEnabled(false);
		itemsProgressLabel.setText(String.format(ITEMS_PROGRESS_LABEL_STR_PATTERN, "?", "?"));
		itemsProgressLabel.setEnabled(false);
		itemsProgressBar.setIndeterminate(true);
		itemsProgressBar.setEnabled(false);
	}

	@Override
	public void setPagesCount(int pagesCount) {
		pagesProgressLabel.setText(String.format(PAGES_PROGRESS_LABEL_STR_PATTERN, "0", pagesCount));
		pagesProgressLabel.setEnabled(true);
		pagesProgressBar.setMinimum(0);
		pagesProgressBar.setMaximum(pagesCount);
		pagesProgressBar.setIndeterminate(false);
		pagesProgressBar.setValue(0);
		pagesProgressBar.setEnabled(true);
	}

	@Override
	public void setItemsCount(int itemsCount) {
		itemsProgressLabel.setText(String.format(ITEMS_PROGRESS_LABEL_STR_PATTERN, "0", itemsCount));
		itemsProgressLabel.setEnabled(true);
		itemsProgressBar.setMinimum(0);
		itemsProgressBar.setMaximum(itemsCount);
		itemsProgressBar.setIndeterminate(false);
		itemsProgressBar.setValue(0);
		itemsProgressBar.setEnabled(true);
	}

	@Override
	public void setPagesProgress(int currentPage) {
		pagesProgressLabel.setText(String.format(PAGES_PROGRESS_LABEL_STR_PATTERN, currentPage, pagesProgressBar.getMaximum()));
		pagesProgressBar.setValue(currentPage);
	}

	@Override
	public void setItemsProgress(int currentItem) {
		itemsProgressLabel.setText(String.format(ITEMS_PROGRESS_LABEL_STR_PATTERN, currentItem, itemsProgressBar.getMaximum()));
		itemsProgressBar.setValue(currentItem);
	}
}
