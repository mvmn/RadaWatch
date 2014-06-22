package x.mvmn.lang.tuple;

public class TupleOfTwo<A, B> {

	private final A first;
	private final B second;

	public TupleOfTwo(A first, B second) {
		this.first = first;
		this.second = second;
	}

	public A getFirst() {
		return first;
	}

	public B getSecond() {
		return second;
	}
}
