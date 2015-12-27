package tv.loilo.promise;

/**
 * Created by Junpei on 2015/09/11.
 */
public interface Condition<TIn> {
    boolean run(final ResultParams<TIn> params) throws Exception;
}
