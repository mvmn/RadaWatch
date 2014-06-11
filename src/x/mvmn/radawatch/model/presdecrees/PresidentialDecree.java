package x.mvmn.radawatch.model.presdecrees;

import java.sql.Date;

import x.mvmn.radawatch.model.Entity;

public class PresidentialDecree extends Entity {

	private final String type;
	private final String title;
	private final Date date;
	private final String numberCode;
	private final String fullText;

	public PresidentialDecree(int dbId, String type, String title, Date date, String numberCode, String fullText) {
		super(dbId);
		this.type = type;
		this.title = title;
		this.date = date;
		this.numberCode = numberCode;
		this.fullText = fullText;
	}

	public String getFullText() {
		return fullText;
	}

	public String getType() {
		return type;
	}

	public String getTitle() {
		return title;
	}

	public Date getDate() {
		return date;
	}

	public String getNumberCode() {
		return numberCode;
	}

	@Override
	public String toString() {
		return "PresidentialDecree [dbId=" + getDbId() + ", type=" + type + ", title=" + title + ", date=" + date + ", numberCode=" + numberCode
				+ ", fullText=" + fullText + "]";
	}
}
