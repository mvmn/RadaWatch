package x.mvmn.radawatch.model;

public abstract class DbEntry {

	private final int dbId;

	public DbEntry(final int dbId) {
		this.dbId = dbId;
	}

	public int getDbId() {
		return dbId;
	}

}
