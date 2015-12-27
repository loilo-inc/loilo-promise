package tv.loilo.promise;

/**
 * Created by pepeotoito on 2015/12/27.
 */
public interface ForEachCallback<TIn> {
    Deferred<ForEachOp> run(ForEachParams<TIn> params) throws Exception;
}
