package x.mvmn.lang.collections;

import java.util.Iterator;

public class CollectionsHelper {

	public static <E, T extends Iterable<E>> E getFirst(T iterable) {
		return getFirst(iterable.iterator());
	}

	public static <E, T extends Iterator<E>> E getFirst(T iterator) {
		E result = null;

		if (iterator.hasNext()) {
			result = iterator.next();
		}
		return result;
	}
}
