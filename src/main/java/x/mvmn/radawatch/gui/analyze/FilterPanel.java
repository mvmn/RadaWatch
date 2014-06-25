package x.mvmn.radawatch.gui.analyze;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sourceforge.jdatepicker.impl.JDatePanelImpl;
import net.sourceforge.jdatepicker.impl.JDatePickerImpl;
import net.sourceforge.jdatepicker.impl.UtilDateModel;
import x.mvmn.radawatch.gui.DateIntervalPickerPanel;
import x.mvmn.radawatch.gui.DateIntervalPickerPanel.DateIntervalListener;

public class FilterPanel extends JPanel implements DateIntervalListener {

	private static final long serialVersionUID = -8823355516907938534L;

	private final JDatePickerImpl datePickerFrom = new JDatePickerImpl(new JDatePanelImpl(new UtilDateModel()));
	private final JDatePickerImpl datePickerTo = new JDatePickerImpl(new JDatePanelImpl(new UtilDateModel()));
	private final JTextField tfTitleFilter = new JTextField();

	private final JButton btnClearFromDate = new JButton("X");
	private final JButton btnClearToDate = new JButton("X");
	private final JButton btnClearTitleFilter = new JButton("X");

	public FilterPanel() {
		this(true, true);
	}

	public FilterPanel(final boolean enableDatesFilters, final boolean enableTitleFilter) {
		super(new BorderLayout());
		if (enableTitleFilter) {
			JPanel titleFilterPanel = new JPanel(new BorderLayout());
			titleFilterPanel.add(tfTitleFilter, BorderLayout.CENTER);
			titleFilterPanel.add(btnClearTitleFilter, BorderLayout.EAST);
			titleFilterPanel.setBorder(BorderFactory.createTitledBorder("Search phrase"));

			btnClearTitleFilter.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent actEvt) {
					tfTitleFilter.setText("");
				}
			});

			this.add(titleFilterPanel, BorderLayout.CENTER);
		}

		if (enableDatesFilters) {
			JPanel dateFromPanel = new JPanel(new BorderLayout());
			dateFromPanel.add(datePickerFrom, BorderLayout.CENTER);
			dateFromPanel.add(btnClearFromDate, BorderLayout.EAST);
			dateFromPanel.setBorder(BorderFactory.createTitledBorder("Date FROM"));

			JPanel dateToPanel = new JPanel(new BorderLayout());
			dateToPanel.add(datePickerTo, BorderLayout.CENTER);
			dateToPanel.add(btnClearToDate, BorderLayout.EAST);
			dateToPanel.setBorder(BorderFactory.createTitledBorder("Date TO"));

			this.add(dateFromPanel, BorderLayout.WEST);
			this.add(dateToPanel, BorderLayout.EAST);

			btnClearFromDate.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent actEvt) {
					((UtilDateModel) datePickerFrom.getModel()).setValue(null);
				}
			});
			btnClearToDate.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent actEvt) {
					((UtilDateModel) datePickerTo.getModel()).setValue(null);
				}
			});
		}

		this.add(new DateIntervalPickerPanel(this, false), BorderLayout.NORTH);
	}

	protected UtilDateModel getDateFromModel() {
		return (UtilDateModel) datePickerFrom.getModel();
	}

	protected UtilDateModel getDateToModel() {
		return (UtilDateModel) datePickerTo.getModel();
	}

	public void setDateFrom(final Date value) {
		getDateFromModel().setValue(value);
	}

	public void setDateTo(final Date value) {
		getDateToModel().setValue(value);
	}

	public Date getDateFrom() {
		return getDateFromModel().getValue();
	}

	public Date getDateTo() {
		return getDateToModel().getValue();
	}

	public String getSearchText() {
		return tfTitleFilter.getText();
	}
}
