package x.mvmn.radawatch.gui.analyze;

import java.awt.BorderLayout;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sourceforge.jdatepicker.impl.JDatePanelImpl;
import net.sourceforge.jdatepicker.impl.JDatePickerImpl;
import net.sourceforge.jdatepicker.impl.UtilDateModel;

public class FilterPanel extends JPanel {

	private static final long serialVersionUID = -8823355516907938534L;

	private final JDatePickerImpl datePickerFrom = new JDatePickerImpl(new JDatePanelImpl(new UtilDateModel()));
	private final JDatePickerImpl datePickerTo = new JDatePickerImpl(new JDatePanelImpl(new UtilDateModel()));
	private final JTextField tfTitleFilter = new JTextField();

	public FilterPanel() {
		super(new BorderLayout());
		this.add(datePickerFrom, BorderLayout.WEST);
		this.add(datePickerTo, BorderLayout.EAST);

		final JPanel titleFilterPanel = new JPanel(new BorderLayout());
		titleFilterPanel.add(new JLabel("<== Date FROM", JLabel.CENTER), BorderLayout.WEST);
		titleFilterPanel.add(new JLabel("Date TO ==>", JLabel.CENTER), BorderLayout.EAST);
		titleFilterPanel.add(tfTitleFilter, BorderLayout.CENTER);
		this.add(titleFilterPanel, BorderLayout.CENTER);
	}

	public Date getDateFrom() {
		return (Date) datePickerFrom.getModel().getValue();
	}

	public Date getDateTo() {
		return (Date) datePickerTo.getModel().getValue();
	}

	public String getSearchText() {
		return tfTitleFilter.getText();
	}
}
