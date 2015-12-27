package tv.loilo.promise;

/**
 * Created by Junpei on 2015/09/11.
 */
public interface Repeat<TOut> {

    Promise<TOut> until(final Condition<TOut> condition);
}
