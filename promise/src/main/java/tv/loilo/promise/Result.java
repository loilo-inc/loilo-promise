package tv.loilo.promise;

/**
 * Created by Junpei on 2015/06/12.
 */
public interface Result<T> extends CancelState {

    Exception getException();

    boolean hasValue();

    T getValue();

    T safeGetValue() throws Exception;
}
