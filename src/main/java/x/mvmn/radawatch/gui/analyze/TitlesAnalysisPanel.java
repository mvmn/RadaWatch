package x.mvmn.radawatch.gui.analyze;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import x.mvmn.lang.StringDisplay;
import x.mvmn.radawatch.gui.browse.DataBrowser;
import x.mvmn.radawatch.model.Entity;
import x.mvmn.radawatch.service.analyze.TitlesAnalyzisHelper;
import x.mvmn.radawatch.service.db.DataBrowseQuery;
import x.mvmn.radawatch.swing.DefaultMouseListener;

public class TitlesAnalysisPanel<T extends Entity> extends JPanel {

	private static final long serialVersionUID = -1709665611976801927L;

	protected final DataBrowser<T> dataBrowser;

	protected final FilterPanel filterPanel;

	public TitlesAnalysisPanel(final DataBrowser<T> dataBrowser, final StringDisplay<T> itemStringDisplay, final Component parentComponent) {
		super(new BorderLayout());

		this.dataBrowser = dataBrowser;
		final JButton btnAnalyzeTitles = new JButton("Analyze titles");

		filterPanel = new FilterPanel(dataBrowser.supportsDateFilter(), dataBrowser.supportsTitleFilter());

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
							final List<T> items = dataBrowser.getDataBrowseService().fetchItems(-1,
									new DataBrowseQuery(filterPanel.getSearchText(), null, null, filterPanel.getDateFrom(), filterPanel.getDateTo()));
							// final List<String> titles = titlesAnalyzer.getTitles(new DataBrowseQuery(filterPanel.getSearchText(), null, null, filterPanel
							// .getDateFrom(), filterPanel.getDateTo()));
							final TitlesTree.TreeNode<T> rootNode = TitlesAnalyzisHelper.mapItemsByTitlesToTreeNodes(items, itemStringDisplay);
							final TitlesTree<T> titlesTree = new TitlesTree<T>(rootNode);
							titlesTree.addMouseListener(new DefaultMouseListener() {
								@Override
								public void mouseClicked(final MouseEvent e) {
									if (e.getClickCount() == 2) {
										TreePath selPath = titlesTree.getPathForLocation(e.getX(), e.getY());
										Object lastPathComponent = selPath.getLastPathComponent();
										if (lastPathComponent instanceof TitlesTree.LeafTreeNode) {
											@SuppressWarnings("unchecked")
											TitlesTree.LeafTreeNode<T> leafTreeNode = (TitlesTree.LeafTreeNode<T>) lastPathComponent;
											dataBrowser.displayDetails(leafTreeNode.getValue().getDbId());
										}
									}
								}
							});

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
