package x.mvmn.radawatch.gui.analyze;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
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

	protected final DefaultTableModel replacementsTableModel = new DefaultTableModel(new String[] { "Regular expression", "Replacement" }, 0);
	protected final JTable replacementsTable = new JTable(replacementsTableModel);

	protected static final String[][] DEFAULT_REPLACEMENTS = new String[][] {
			new String[] { "(«|»)", "\"" },
			new String[] { "i", "і" },
			new String[] { "o", "о" },
			new String[] { "a", "а" },
			new String[] { "e", "е" },
			new String[] { "y", "у" },
			new String[] { "прзеидента", "президента" },
			new String[] { "^указ президента( україн?и)?( \\d+/\\d{4})?:", "указ президента україни:" },
			new String[] { "^розпорядження( президента)?( україни)?:", "розпорядження президента україни:" },
			new String[] { "м. Києва", "м.Києва" },
			new String[] { "м. Севастополя", "м.Севастополя" },
			new String[] { "в місті Києві державної адміністрації", "державної адміністрації м.Києва" },
			new String[] { "адміністрації в Автономній Республіці Крим", "адміністрації Автономної Республіки Крим" },
			new String[] { "\\bадміністрації(\\p{Lu})", "адміністрації $1" },
			new String[] {
					"\\b(\\p{Lu}[\\p{Ll}'’]+(?:-\\p{Lu}[\\p{Ll}'’]+|))\\s+(\\p{L}+)(\\s+в\\s+місті\\s+\\p{Lu}[\\p{Ll}'’]+(?:-\\p{Lu}[\\p{Ll}'’]+|))?\\s+державної\\s+адміністрації(\\s+\\p{Lu}[\\p{Ll}'’]+(?:-\\p{Lu}[\\p{Ll}'’]+|)\\s+області|\\s+м.\\s*\\p{Lu}[\\p{Ll}'’]+(?:-\\p{Lu}[\\p{Ll}'’]+|)|\\s+Автономної\\s+Республіки\\s+Крим)?",
					"держадміністрації $2 $3$4 $1" },
			new String[] { "\\b(\\p{Lu}\\.\\s*\\p{Lu}\\.\\s*\\p{Lu}[\\p{Ll}'’]+(?:-\\p{Lu}[\\p{Ll}'’]+|))\\b", "<Прізвище-ініціали>" },
			new String[] { "\\b(\\p{Lu}\\.\\s*\\p{Lu}[\\p{Ll}'’]+(?:-\\p{Lu}[\\p{Ll}'’]+|))\\b", "<Прізвище-ініціали>" },
			new String[] { "\\b(\\p{Lu}[\\p{Ll}'’]+(?:-\\p{Lu}[\\p{Ll}'’]+|)\\s+\\p{Lu}\\.\\s*\\p{Lu}\\.)", "<Прізвище-ініціали>" },
			new String[] { "\\b(\\p{Lu}[\\p{Ll}'’]+(?:-\\p{Lu}[\\p{Ll}'’]+|))\\s+області", "області $1" },
			new String[] { "\\d{1,2} (січ|лют|бер|кві|тра|чер|лип|сер|вер|жов|лис|гру)\\p{Ll}+ \\d{4}", "<числа/місяця/року>" } };

	public TitlesAnalysisPanel(final DataBrowser<T> dataBrowser, final StringDisplay<T> itemStringDisplay, final Component parentComponent) {
		super(new BorderLayout());

		this.dataBrowser = dataBrowser;
		final JButton btnAnalyzeTitles = new JButton("Analyze titles");

		for (final String[] defualtReplacement : DEFAULT_REPLACEMENTS) {
			replacementsTableModel.addRow(defualtReplacement);
		}

		filterPanel = new FilterPanel(dataBrowser.supportsDateFilter(), dataBrowser.supportsSearchPhraseFilter());
		final JPanel replacementsPanel = new JPanel(new BorderLayout());
		{
			replacementsPanel.setBorder(BorderFactory.createTitledBorder("Text replacements in titles"));
			final JScrollPane tableScrollPanel = new JScrollPane(replacementsTable);
			replacementsPanel.add(tableScrollPanel, BorderLayout.CENTER);
			final JPanel replacementsControlsPanel = new JPanel(new BorderLayout());
			replacementsPanel.add(replacementsControlsPanel, BorderLayout.SOUTH);
			final JButton btnAddReplacement = new JButton("+");
			final JButton btnRemoveReplacement = new JButton("-");
			final JButton btnMoveReplacementUp = new JButton("^");
			final JButton btnMoveReplacementDown = new JButton("v");
			final JButton btnResetReplacements = new JButton("Reset to defaults");

			replacementsControlsPanel.add(btnAddReplacement, BorderLayout.EAST);
			replacementsControlsPanel.add(btnRemoveReplacement, BorderLayout.WEST);
			replacementsControlsPanel.add(btnResetReplacements, BorderLayout.CENTER);

			replacementsPanel.add(btnMoveReplacementUp, BorderLayout.EAST);
			replacementsPanel.add(btnMoveReplacementDown, BorderLayout.WEST);

			btnMoveReplacementUp.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent actEvent) {
					if (replacementsTable.getSelectedRowCount() > 0) {
						final int[] selectedRows = replacementsTable.getSelectedRows();
						Arrays.sort(selectedRows);
						if (selectedRows[0] > 0) {
							for (int i = 0; i < selectedRows.length; i++) {
								final int rowIndex = selectedRows[i];
								final Object vSwapK = replacementsTableModel.getValueAt(rowIndex - 1, 0);
								final Object vSwapV = replacementsTableModel.getValueAt(rowIndex - 1, 1);
								replacementsTableModel.setValueAt(replacementsTableModel.getValueAt(rowIndex, 0), rowIndex - 1, 0);
								replacementsTableModel.setValueAt(replacementsTableModel.getValueAt(rowIndex, 1), rowIndex - 1, 1);
								replacementsTableModel.setValueAt(vSwapK, rowIndex, 0);
								replacementsTableModel.setValueAt(vSwapV, rowIndex, 1);
							}
							for (int i = 0; i < selectedRows.length; i++) {
								final int rowIndex = selectedRows[i];
								replacementsTable.changeSelection(rowIndex, 0, true, false);
								replacementsTable.changeSelection(rowIndex - 1, 0, true, false);
							}
						}
					}
				}
			});
			btnMoveReplacementDown.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent actEvent) {
					if (replacementsTable.getSelectedRowCount() > 0) {
						final int[] selectedRows = replacementsTable.getSelectedRows();
						Arrays.sort(selectedRows);
						if (selectedRows[selectedRows.length - 1] < replacementsTable.getRowCount() - 1) {
							for (int i = selectedRows.length - 1; i >= 0; i--) {
								final int rowIndex = selectedRows[i];
								final Object vSwapK = replacementsTableModel.getValueAt(rowIndex + 1, 0);
								final Object vSwapV = replacementsTableModel.getValueAt(rowIndex + 1, 1);
								replacementsTableModel.setValueAt(replacementsTableModel.getValueAt(rowIndex, 0), rowIndex + 1, 0);
								replacementsTableModel.setValueAt(replacementsTableModel.getValueAt(rowIndex, 1), rowIndex + 1, 1);
								replacementsTableModel.setValueAt(vSwapK, rowIndex, 0);
								replacementsTableModel.setValueAt(vSwapV, rowIndex, 1);
							}
							for (int i = selectedRows.length - 1; i >= 0; i--) {
								final int rowIndex = selectedRows[i];
								replacementsTable.changeSelection(rowIndex, 0, true, false);
								replacementsTable.changeSelection(rowIndex + 1, 0, true, false);
							}
						}
					}
				}
			});
			btnRemoveReplacement.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent actEvent) {
					if (replacementsTable.getSelectedRowCount() > 0) {
						final int[] selectedRows = replacementsTable.getSelectedRows();
						Arrays.sort(selectedRows);

						for (int i = selectedRows.length - 1; i >= 0; i--) {
							replacementsTableModel.removeRow(selectedRows[i]);
						}
					}
				}
			});
			btnAddReplacement.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					final String expression = JOptionPane.showInputDialog(parentComponent, "Input regular expression text");
					if (expression != null && expression.length() > 0) {
						final String replacement = JOptionPane.showInputDialog(parentComponent, "Input replacement text");
						replacementsTableModel.addRow(new String[] { expression, replacement });
					}
				}
			});
			btnResetReplacements.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (JOptionPane.showConfirmDialog(parentComponent, "Are you sure you want to reset replacements to defaults?") == JOptionPane.OK_OPTION) {
						replacementsTableModel.setRowCount(0);
						for (final String[] defualtReplacement : DEFAULT_REPLACEMENTS) {
							replacementsTableModel.addRow(defualtReplacement);
						}
					}
				}
			});
		}

		final StringDisplay<T> itemStringDisplayWithReplacements = new StringDisplay<T>() {
			@Override
			public String getStringDisplay(T item) {
				String display = itemStringDisplay.getStringDisplay(item);
				if (display != null && replacementsTableModel.getRowCount() > 0) {
					for (int i = 0; i < replacementsTableModel.getRowCount(); i++) {
						final String replacementDefKey = replacementsTableModel.getValueAt(i, 0).toString();
						if (replacementDefKey.length() > 0) {
							final String replacementDefValue = replacementsTableModel.getValueAt(i, 1).toString();
							display = display.replaceAll(replacementDefKey, replacementDefValue);
						}
					}
				}

				return display;
			}
		};

		this.add(btnAnalyzeTitles, BorderLayout.SOUTH);
		this.add(filterPanel, BorderLayout.NORTH);
		final JPanel resultsPanel = new JPanel(new BorderLayout());
		resultsPanel.add(new JScrollPane(new JLabel("Results will appear here.", JLabel.CENTER)), BorderLayout.CENTER);
		final JSplitPane mainPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false, replacementsPanel, resultsPanel);
		mainPanel.setResizeWeight(0.3d);
		this.add(mainPanel, BorderLayout.CENTER);
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
							final TitlesTree.TreeNode<T> rootNode = TitlesAnalyzisHelper.mapItemsByTitlesToTreeNodes(items, itemStringDisplayWithReplacements);
							final TitlesTree<T> titlesTree = new TitlesTree<T>(rootNode);
							titlesTree.addMouseListener(new DefaultMouseListener() {
								@Override
								public void mouseClicked(final MouseEvent e) {
									if (e.getClickCount() == 2) {
										TreePath selPath = titlesTree.getPathForLocation(e.getX(), e.getY());
										if (selPath != null) {
											Object lastPathComponent = selPath.getLastPathComponent();
											if (lastPathComponent != null && lastPathComponent instanceof TitlesTree.LeafTreeNode) {
												@SuppressWarnings("unchecked")
												TitlesTree.LeafTreeNode<T> leafTreeNode = (TitlesTree.LeafTreeNode<T>) lastPathComponent;
												dataBrowser.displayDetails(leafTreeNode.getValue().getDbId());
											}
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
