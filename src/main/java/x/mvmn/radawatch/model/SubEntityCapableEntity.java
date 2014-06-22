package x.mvmn.radawatch.model;

import java.util.List;

public abstract class SubEntityCapableEntity<T extends Entity> extends Entity {

	public SubEntityCapableEntity(final int dbId) {
		super(dbId);
	}

	public abstract List<T> getSubEntities();

}
