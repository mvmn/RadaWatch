package x.mvmn.radawatch.gui.analyze;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableModel;

import x.mvmn.radawatch.gui.ResultSetToTableModelConverter;
import x.mvmn.radawatch.gui.browse.DataBrowser;
import x.mvmn.radawatch.model.radavotes.VoteSessionResultsData;
import x.mvmn.radawatch.service.analyze.FactionsDissentAnalyzer;
import x.mvmn.radawatch.service.db.DataBaseConnectionService;
import x.mvmn.radawatch.swing.DefaultMouseListener;

public class FactionsDissentingLawsPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = -614913620654997668L;

	protected FactionsDissentAnalyzer<TableModel> analyzer;
	protected final TableDataUpdateController<Object> factionDissentListController;
	protected final DataBrowser<VoteSessionResultsData> voteSessionsDataBrowser;

	protected final ExtFilterPanel filterPanel = new ExtFilterPanel(true, true);
	protected final JTable mainTable = new JTable();
	protected final JButton doQuery = new JButton("Run Query");
	protected final JLabel sliderVal = new JLabel("75");
	protected final JCheckBox cbNormalizeByEffectiveVotes = new JCheckBox("Normalize by % of effective votes", true);
	protected final JSlider sliderPercentageThreshold = new JSlider(JSlider.HORIZONTAL, 1, 100, 75);

	protected final JComboBox<String> cmFormula = new JComboBox<String>(new String[] {
			"[ N(for) - N(against) - N(abstained) - N(skipped) ] / ( N(total) - N(absent) )",
			"[ N(for) - N(against) - N(abstained) - N(skipped) - N(absent) ] / N(total)",
			"[ N(for) - N(against) - N(abstained) ] / ( N(total) - N(absent) - N(skipped) )", });

	public FactionsDissentingLawsPanel(DataBaseConnectionService dbService, DataBrowser<VoteSessionResultsData> voteSessionsDataBrowser) {
		super(new BorderLayout());
		this.voteSessionsDataBrowser = voteSessionsDataBrowser;
		analyzer = new FactionsDissentAnalyzer<TableModel>(dbService, new ResultSetToTableModelConverter());

		doQuery.addActionListener(this);

		sliderPercentageThreshold.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				sliderVal.setText(String.valueOf(sliderPercentageThreshold.getValue()));
			}
		});

		this.add(filterPanel, BorderLayout.NORTH);
		this.add(new JScrollPane(mainTable), BorderLayout.CENTER);

		final JPanel btnPanel = new JPanel(new BorderLayout());
		btnPanel.add(cmFormula, BorderLayout.NORTH);
		btnPanel.add(doQuery, BorderLayout.EAST);
		btnPanel.add(sliderPercentageThreshold, BorderLayout.CENTER);
		btnPanel.add(sliderVal, BorderLayout.WEST);
		btnPanel.add(cbNormalizeByEffectiveVotes, BorderLayout.SOUTH);
		this.add(btnPanel, BorderLayout.SOUTH);

		factionDissentListController = new TableDataUpdateController<Object>(new TableDataUpdateController.TableModelProvider<Object>() {
			@Override
			public TableModel provide(final Object... param) throws Exception {
				int f = cmFormula.getSelectedIndex();
				if (f < 0 || f > 2) {
					f = 0;
					cmFormula.setSelectedIndex(0);
				}
				return analyzer.queryForDissentingLaws(sliderPercentageThreshold.getValue(), f == 2, f != 1, cbNormalizeByEffectiveVotes.isSelected(),
						filterPanel.generateDataBrowseQuery());
			}
		}, mainTable, new Component[] { doQuery }, this, null, null);

		mainTable.addMouseListener(new DefaultMouseListener() {
			@Override
			public void mouseClicked(final MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2) {
					int row = mainTable.rowAtPoint(mouseEvent.getPoint());
					if (row > -1) {
						final Integer voteId = (Integer) mainTable.getModel().getValueAt(row, 0);
						displayVoteDetails(voteId);
					}
				}
			}
		});
	}

	protected void displayVoteDetails(Integer voteId) {
		voteSessionsDataBrowser.displayDetails(voteId);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		factionDissentListController.perform();
	}
}
