package x.mvmn.radawatch.gui.analyze;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import x.mvmn.radawatch.gui.ResultSetToTableModelConverter;
import x.mvmn.radawatch.service.analyze.FactionsDissentAnalyzer;
import x.mvmn.radawatch.service.db.DataBaseConnectionService;

public class FactionsDissentPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = -7208381550375389480L;

	protected FactionsDissentAnalyzer<TableModel> analyzer;
	protected final TableDataUpdateController<Object> factionDissentListController;

	protected final ExtFilterPanel filterPanel = new ExtFilterPanel(true, true);
	protected final JCheckBox cbNormalizeByEffectiveVotes = new JCheckBox("Normalize by % of effective votes", true);
	protected final JTable mainTable = new JTable();
	protected final JButton doQuery = new JButton("Run Query");

	protected final JComboBox<String> cbFormula = new JComboBox<String>(new String[] {
			"[ N(for) - N(against) - N(abstained) - N(skipped) ] / ( N(total) - N(absent) )",
			"[ N(for) - N(against) - N(abstained) - N(skipped) - N(absent) ] / N(total)",
			"[ N(for) - N(against) - N(abstained) ] / ( N(total) - N(absent) - N(skipped) )", });

	public FactionsDissentPanel(final DataBaseConnectionService storageService) {
		super(new BorderLayout());
		analyzer = new FactionsDissentAnalyzer<TableModel>(storageService, new ResultSetToTableModelConverter());

		doQuery.addActionListener(this);

		this.add(filterPanel, BorderLayout.NORTH);
		this.add(new JScrollPane(mainTable), BorderLayout.CENTER);

		final JPanel btnPanel = new JPanel(new BorderLayout());
		btnPanel.add(cbFormula, BorderLayout.CENTER);
		btnPanel.add(doQuery, BorderLayout.EAST);
		btnPanel.add(cbNormalizeByEffectiveVotes, BorderLayout.SOUTH);
		this.add(btnPanel, BorderLayout.SOUTH);

		factionDissentListController = new TableDataUpdateController<Object>(new TableDataUpdateController.TableModelProvider<Object>() {
			@Override
			public TableModel provide(final Object... param) throws Exception {
				int f = cbFormula.getSelectedIndex();
				if (f < 0 || f > 2) {
					f = 0;
					cbFormula.setSelectedIndex(0);
				}
				return analyzer.queryForFactionsDissent(f == 2, f != 1, cbNormalizeByEffectiveVotes.isSelected(), filterPanel.generateDataBrowseQuery());
			}
		}, mainTable, new Component[] { doQuery }, this, null, null);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		factionDissentListController.perform();
	}
}
