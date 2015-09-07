package x.mvmn.radawatch.gui.analyze;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import x.mvmn.radawatch.swing.SwingHelper;

public class TableDataUpdateController<T> {

	public static interface TableModelProvider<P> {
		public TableModel provide(P... param) throws Exception;
	}

	protected final TableModelProvider<T> provider;
	protected final JTable controlledTable;
	protected final Component parentWindow;
	protected final Component[] controlComponents;
	protected final Runnable preAction;
	protected final Runnable postAction;

	public TableDataUpdateController(final TableModelProvider<T> provider, final JTable controlledTable, final Component[] controlComponents,
			final Component parentWindow, final Runnable preAction, final Runnable postAction) {
		this.controlledTable = controlledTable;
		this.parentWindow = parentWindow;
		this.provider = provider;
		this.controlComponents = controlComponents;
		this.preAction = preAction;
		this.postAction = postAction;
	}

	public void perform(final T... param) {
		for (final Component c : controlComponents) {
			c.setEnabled(false);
		}
		if (preAction != null) {
			preAction.run();
		}
		new Thread() {
			public void run() {
				try {
					final TableModel result = provider.provide(param);
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							controlledTable.setModel(result);
							controlledTable.setRowSorter(new TableRowSorter<TableModel>(result));
							controlledTable.invalidate();
							controlledTable.revalidate();
							controlledTable.repaint();
							if (postAction != null) {
								postAction.run();
							}
						}
					});
				} catch (final Exception ex) {
					ex.printStackTrace();
					SwingHelper.reportError(true, parentWindow, ex);
				} finally {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							for (final Component c : controlComponents) {
								c.setEnabled(true);
							}
						}
					});
				}
			}
		}.start();
	}
}
