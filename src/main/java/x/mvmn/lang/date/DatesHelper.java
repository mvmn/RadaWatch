package x.mvmn.lang.date;

import java.sql.Date;

public class DatesHelper {
	public static Date utilDateToSqlDate(final java.util.Date utillDate) {
		final Date result;
		if (utillDate != null) {
			result = new Date(utillDate.getTime());
		} else {
			result = null;
		}
		return result;
	}
}
