package tv.loilo.promise;

import java.util.concurrent.ExecutorService;

/**
 * Created by Junpei on 2015/06/12.
 */
public interface Promise<TOut> extends Submittable {

    Deferred<TOut> get(final TaggedCancelState state);


    Deferred<TOut> getOn(final ExecutorService executorService, final Tagged state);


    Promise<TOut> promiseOn(final ExecutorService executorService);


    <TNextOut> Promise<TNextOut> then(final Continuation<TOut, TNextOut> continuation);


    Promise<TOut> watch(final WatchCallback<TOut> watchCallback);


    <TNextOut> Promise<TNextOut> succeeded(final SuccessCallback<TOut, TNextOut> successCallback);


    Promise<TOut> failed(final FailCallback<TOut> failCallback);


    Submittable finish(final FinishCallback<TOut> finishCallback);


    <TReplace> Promise<TReplace> exchange(final TReplace replace);
}
