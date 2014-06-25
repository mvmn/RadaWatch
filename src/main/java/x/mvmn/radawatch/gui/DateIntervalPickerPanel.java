package x.mvmn.radawatch.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class DateIntervalPickerPanel extends JTabbedPane {
	private static final long serialVersionUID = -5355383160036816339L;

	public static interface DateIntervalListener {
		public void setDateFrom(final Date value);

		public void setDateTo(final Date value);
	}

	final DateIntervalListener listener;

	public DateIntervalPickerPanel(final DateIntervalListener listener, final boolean displayLabel) {
		this.listener = listener;
		for (IntervalType intervalType : IntervalType.values()) {
			this.addTab(intervalType.name().toLowerCase(), new IntervalNavPanel(intervalType, displayLabel));
		}
		this.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				updateCurrentData();
			}
		});
		updateCurrentData();
	}

	private enum IntervalType {
		DAY, WEEK, MONTH, YEAR
	}

	protected IntervalNavPanel currentIntervalPanel;

	protected void updateCurrentData() {
		currentIntervalPanel = (IntervalNavPanel) DateIntervalPickerPanel.this.getSelectedComponent();
		if (currentIntervalPanel != null && listener != null) {
			listener.setDateFrom(this.getDateFrom());
			listener.setDateTo(this.getDateTo());
		}
	}

	public IntervalType getIntervalType() {
		return currentIntervalPanel.getCurrentIntervalType();
	}

	public Date getDateFrom() {
		return currentIntervalPanel.getCurrentDateFrom();
	}

	public Date getDateTo() {
		return currentIntervalPanel.getCurrentDateTo();
	}

	protected Date getDate(int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		if (year < 1) {
			year = cal.get(Calendar.YEAR);
		}
		if (month < 1) {
			month = cal.get(Calendar.MONTH) + 1;
		}
		if (day < 1) {
			day = cal.get(Calendar.DAY_OF_MONTH);
		}
		cal.set(year, month - 1, day, 0, 0, 0);
		return cal.getTime();
	}

	private class IntervalNavPanel extends JPanel implements ActionListener {
		private static final long serialVersionUID = 1532447660833421437L;

		private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		private JButton btnPrev = new JButton("<<");
		private JButton btnNext = new JButton(">>");
		private JButton btnCurrent = new JButton("Current");

		private final IntervalType intervalType;

		private final JLabel currentIntervalLabel;
		private Date currentDateFrom;
		private Date currentDateTo;
		private int intervalOffset = 0;

		public IntervalNavPanel(final IntervalType intervalType, final boolean displayLabel) {
			super(new BorderLayout());
			this.intervalType = intervalType;

			btnPrev.setActionCommand("inpac_prev");
			btnNext.setActionCommand("inpac_next");
			btnCurrent.setActionCommand("inpac_current");

			btnPrev.addActionListener(this);
			btnNext.addActionListener(this);
			btnCurrent.addActionListener(this);

			this.add(btnPrev, BorderLayout.WEST);
			this.add(btnNext, BorderLayout.EAST);
			this.add(btnCurrent, BorderLayout.CENTER);

			if (displayLabel) {
				currentIntervalLabel = new JLabel("", JLabel.CENTER);
				this.add(currentIntervalLabel, BorderLayout.NORTH);
			} else {
				currentIntervalLabel = null;
			}

			updateModel();
		}

		public Date getIntervalBeginning(int intervalOffset) {
			Calendar cal = Calendar.getInstance();
			cal.setFirstDayOfWeek(Calendar.MONDAY);
			int year = -1;
			int month = -1;
			int day = -1;

			switch (intervalType) {
				case YEAR:
					month = 1;
					day = 1;
				break;
				case MONTH:
					day = 1;
				break;
				default:
				break;
			}

			cal.setTime(getDate(year, month, day));

			if (intervalType.equals(IntervalType.WEEK)) {
				cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			}

			if (intervalOffset != 0) {
				final int field;
				switch (intervalType) {
					case YEAR:
						field = Calendar.YEAR;
					break;
					case MONTH:
						field = Calendar.MONTH;
					break;
					case WEEK:
						field = Calendar.WEEK_OF_YEAR;
					break;
					default:
					case DAY:
						field = Calendar.DAY_OF_MONTH;
					break;
				}
				cal.add(field, intervalOffset);
			}
			return cal.getTime();
		}

		protected void updateModel() {
			currentDateFrom = getIntervalBeginning(intervalOffset);
			currentDateTo = getIntervalBeginning(intervalOffset + 1);
			if (currentIntervalLabel != null) {
				currentIntervalLabel.setText(String.format("%s - %s", dateFormat.format(currentDateFrom), dateFormat.format(currentDateTo)));
			}
			updateCurrentData();
		}

		@Override
		public void actionPerformed(ActionEvent actEvent) {
			if (actEvent.getActionCommand().equals("inpac_prev")) {
				intervalOffset--;
				updateModel();
			} else if (actEvent.getActionCommand().equals("inpac_next")) {
				intervalOffset++;
				updateModel();
			} else if (actEvent.getActionCommand().equals("inpac_current")) {
				intervalOffset = 0;
				updateModel();
			}
		}

		public IntervalType getCurrentIntervalType() {
			return intervalType;
		}

		public Date getCurrentDateFrom() {
			return currentDateFrom;
		}

		public Date getCurrentDateTo() {
			return currentDateTo;
		}
	}
}
