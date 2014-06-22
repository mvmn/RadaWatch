package x.mvmn.lang;

public interface ExceptionHandler<T extends Throwable> {

	public void handleException(T exception);
}
