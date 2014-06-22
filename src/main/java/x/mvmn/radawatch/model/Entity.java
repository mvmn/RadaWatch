package x.mvmn.radawatch.model;

public abstract class Entity {

	private final int dbId;

	public Entity(final int dbId) {
		this.dbId = dbId;
	}

	public int getDbId() {
		return dbId;
	}
}
