package x.mvmn.radawatch.gui.browse;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import x.mvmn.radawatch.gui.analyze.FilterPanel;
import x.mvmn.radawatch.model.Entity;
import x.mvmn.radawatch.service.db.DataBrowseQuery;
import x.mvmn.radawatch.service.db.DataBrowseService;
import x.mvmn.radawatch.swing.DefaultMouseListener;
import x.mvmn.radawatch.swing.SwingHelper;

public class DataBrowser<T extends Entity> extends JPanel {
	private static final long serialVersionUID = 1875124079254164325L;

	public static interface ViewAdaptor<T> {
		public int getFieldsCount(boolean fullView);

		public String getFieldName(int fieldIndex, boolean fullView);

		public Object getFieldValue(T item, int fieldIndex, boolean fullView);

		public Class<?> getFieldType(int fieldIndex, boolean fullView);
	}

	public static class ItemDetailView<E extends Entity> extends JPanel {
		private static final long serialVersionUID = 4285281165175763791L;

		public ItemDetailView(final E item, final ViewAdaptor<E> viewAdaptor, DataBrowser<? extends Entity> subItemsBrowser) {
			super(new BorderLayout());
			final int fieldsCount = viewAdaptor.getFieldsCount(true);
			final JPanel detailsPanel = new JPanel(new GridLayout(fieldsCount, 1));

			for (int i = 0; i < fieldsCount; i++) {
				// detailsPanel.add(new JLabel(viewAdaptor.getFieldName(i, true)));
				JTextArea textArea = new JTextArea(viewAdaptor.getFieldValue(item, i, true).toString());
				textArea.setEditable(false);
				textArea.setBorder(BorderFactory.createTitledBorder(viewAdaptor.getFieldName(i, true)));
				detailsPanel.add(textArea);
			}
			final Component mainComponent;
			if (subItemsBrowser == null) {
				mainComponent = new JScrollPane(detailsPanel);
			} else {
				subItemsBrowser.setParentEntityId(item.getDbId());
				subItemsBrowser.triggerDataUpdate();
				final JTabbedPane tabPane = new JTabbedPane();
				tabPane.add("Details", new JScrollPane(detailsPanel));
				tabPane.add(subItemsBrowser.getDataTitle(), subItemsBrowser);
				mainComponent = tabPane;
			}
			this.add(mainComponent, BorderLayout.CENTER);
		}
	}

	public class DataBrowserTableModel<X extends Entity> extends DefaultTableModel {
		private static final long serialVersionUID = 8298942535989020431L;

		private final List<X> items;
		private final ViewAdaptor<X> viewAdaptor;

		public DataBrowserTableModel(final List<X> items, final ViewAdaptor<X> viewAdaptor) {
			this.items = items;
			this.viewAdaptor = viewAdaptor;
		}

		public X getItemAt(int rowIndex) {
			return items == null ? null : items.get(rowIndex);
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return viewAdaptor.getFieldType(columnIndex, false);
		}

		@Override
		public String getColumnName(int columnIndex) {
			return viewAdaptor.getFieldName(columnIndex, false);
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return viewAdaptor.getFieldValue(getItemAt(rowIndex), columnIndex, false);
		}

		@Override
		public int getRowCount() {
			return items == null ? 0 : items.size();
		}

		@Override
		public int getColumnCount() {
			return viewAdaptor.getFieldsCount(false);
		}
	}

	private volatile int parentEntityId;
	private final DataBrowseService<T> dataBrowseService;
	private final FilterPanel filterPanel;
	private final JButton btnLoadData = new JButton("Load data");
	private final JTable mainTable = new JTable();
	private final JLabel itemsCountLabel = new JLabel("Results: -");
	private final ViewAdaptor<T> viewAdaptor;
	private final String dataTitle;
	private final DataBrowser<? extends Entity> subItemsBrowser;

	public DataBrowser(final String dataTitle, final DataBrowseService<T> dataBrowseService, final int parentEntityId, final ViewAdaptor<T> viewAdaptor,
			final DataBrowser<? extends Entity> subItemsBrowser) {
		super(new BorderLayout());
		this.dataTitle = dataTitle;
		this.dataBrowseService = dataBrowseService;
		this.parentEntityId = parentEntityId;
		this.viewAdaptor = viewAdaptor;
		this.subItemsBrowser = subItemsBrowser;

		this.filterPanel = new FilterPanel(dataBrowseService.supportsDateFilter(), dataBrowseService.supportsTitleFilter());

		this.add(filterPanel, BorderLayout.NORTH);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JScrollPane(mainTable), BorderLayout.CENTER);
		panel.add(itemsCountLabel, BorderLayout.SOUTH);
		this.add(panel, BorderLayout.CENTER);
		this.add(btnLoadData, BorderLayout.SOUTH);

		btnLoadData.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				triggerDataUpdate();
			}
		});

		mainTable.addMouseListener(new DefaultMouseListener() {
			@Override
			public void mouseClicked(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2) {
					int row = mainTable.rowAtPoint(mouseEvent.getPoint());
					if (row > -1) {
						@SuppressWarnings("unchecked")
						T item = ((DataBrowserTableModel<T>) mainTable.getModel()).getItemAt(mainTable.convertRowIndexToModel(row));
						displayDetails(item.getDbId());
					}
				}
			}
		});
	}

	public void displayDetails(int itemDbId) {
		try {
			// TODO: consider moving off EDT
			final T item = dataBrowseService.fetchItem(itemDbId);
			SwingHelper.enframeComponent(new ItemDetailView<T>(item, viewAdaptor, subItemsBrowser), dataTitle + " - " + String.valueOf(item.getDbId()))
					.setVisible(true);
		} catch (final Exception ex) {
			ex.printStackTrace();
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					JOptionPane.showMessageDialog(DataBrowser.this, ex.getClass().getCanonicalName() + " " + ex.getMessage(), "Error occurred",
							JOptionPane.ERROR_MESSAGE);
				}
			});
		}
	}

	public void triggerDataUpdate() {
		btnLoadData.setEnabled(false);
		new Thread() {
			public void run() {
				try {
					final DataBrowseQuery dbQuery = new DataBrowseQuery(filterPanel.getSearchText(), null, null, filterPanel.getDateFrom(),
							filterPanel.getDateTo());
					final int itemsCount = dataBrowseService.countItems(parentEntityId, dbQuery);
					final List<T> items = dataBrowseService.fetchItems(parentEntityId, dbQuery);
					final TableModel tableModel = new DataBrowserTableModel<T>(items, viewAdaptor);
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							itemsCountLabel.setText("Results: " + String.valueOf(itemsCount));
							mainTable.setModel(tableModel);
							mainTable.setRowSorter(new TableRowSorter<TableModel>(tableModel));
						}
					});
				} catch (final Exception loadException) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							loadException.printStackTrace();
							JOptionPane.showMessageDialog(DataBrowser.this, loadException.getClass().getCanonicalName() + " " + loadException.getMessage(),
									"Error occurred", JOptionPane.ERROR_MESSAGE);
						}
					});
				} finally {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							btnLoadData.setEnabled(true);
						}
					});
				}
			}
		}.start();
	}

	public int getParentEntityId() {
		return parentEntityId;
	}

	public void setParentEntityId(int parentEntityId) {
		this.parentEntityId = parentEntityId;
	}

	public String getDataTitle() {
		return dataTitle;
	}

	public DataBrowseService<T> getDataBrowseService() {
		return dataBrowseService;
	}

	public boolean supportsTitleFilter() {
		return dataBrowseService.supportsTitleFilter();
	}

	public boolean supportsDateFilter() {
		return dataBrowseService.supportsDateFilter();
	}
}
