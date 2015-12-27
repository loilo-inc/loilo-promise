package tv.loilo.promise;

import java.util.concurrent.ExecutorService;

/**
 * Created by Junpei on 2015/06/12.
 */
public interface Promise<TOut> extends Submittable {

    Deferred<TOut> get(TaggedCancelState state);


    Deferred<TOut> getOn(ExecutorService executorService, Tagged state);


    Promise<TOut> promiseOn(ExecutorService executorService);


    <TNextOut> Promise<TNextOut> then(ThenCallback<TOut, TNextOut> thenCallback);


    Promise<TOut> watch(WatchCallback<TOut> watchCallback);


    <TNextOut> Promise<TNextOut> succeeded(SuccessCallback<TOut, TNextOut> successCallback);


    Promise<TOut> failed(FailCallback<TOut> failCallback);


    Submittable finish(FinishCallback<TOut> finishCallback);


    <TReplace> Promise<TReplace> exchange(TReplace replace);
}
