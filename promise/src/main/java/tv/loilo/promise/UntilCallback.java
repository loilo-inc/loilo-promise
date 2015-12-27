package tv.loilo.promise;

/**
 * Created by Junpei on 2015/09/11.
 */
public interface UntilCallback<TIn> {
    boolean run(UntilParams<TIn> params) throws Exception;
}
