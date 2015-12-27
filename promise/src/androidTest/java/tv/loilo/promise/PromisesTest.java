package tv.loilo.promise;

import android.test.InstrumentationTestCase;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Junpei on 2015/06/16.
 */
public class PromisesTest extends InstrumentationTestCase {

    private static void ensureSucceeded(final Result<?> result) {
        if (result.getCancelToken().isCanceled()) {
            throw new RuntimeException("Not succeeded.");
        }

        final Exception e = result.getException();
        if (e != null) {
            throw new RuntimeException("Not succeeded.", e);
        }
    }

    private static void ensureCanceled(final Result<?> result) {
        if (!result.getCancelToken().isCanceled()) {
            throw new RuntimeException("Not canceled.");
        }
    }

    private static void ensureFailed(final Result<?> result) {
        if (result.getCancelToken().isCanceled()) {
            throw new RuntimeException("Not failed.");
        }

        //noinspection ThrowableResultOfMethodCallIgnored
        final Exception e = result.getException();
        if (e == null) {
            throw new RuntimeException("Not Failed.");
        }
    }

    public void testNormal() throws Exception {

        final ExecutorService executorService = Executors.newSingleThreadExecutor();

        Promises.when(new EntryFunction<String>() {
            @Override
            public Deferred<String> run(EntryParams args) throws Exception {
                return Defer.success("Hello");
            }
        }).succeeded(new SuccessCallback<String, String>() {
            @Override
            public Deferred<String> run(SuccessParams<String> args) throws Exception {
                return Defer.success(args.getValue() + " Promise");
            }
        }).finish(new FinishCallback<String>() {
            @Override
            public void run(ResultParams<String> args) {
                ensureSucceeded(args.asResult());
                Log.i("PromisesTest", "testNormal " + args.getValue());
            }
        }).submitOn(executorService);

        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    public void testCancellableWhenNotRunningFuture() throws Exception {

        final ExecutorService executorService = Executors.newSingleThreadExecutor();

        Promises.when(new EntryFunction<String>() {
            @Override
            public Deferred<String> run(EntryParams args) throws Exception {
                TimeUnit.SECONDS.sleep(5);
                return Defer.success("Hello");
            }
        }).succeeded(new SuccessCallback<String, String>() {
            @Override
            public Deferred<String> run(SuccessParams<String> args) throws Exception {
                return Defer.success(args.getValue() + " Promise");
            }

        }).finish(new FinishCallback<String>() {
            @Override
            public void run(ResultParams<String> args) {
                ensureSucceeded(args.asResult());
                Log.i("PromisesTest", "testCancellableWhenNotRunningFuture 1 " + args.getValue());
            }
        }).submitOn(executorService);

        final Canceller canceller = Promises.when(new EntryFunction<String>() {

            @Override
            public Deferred<String> run(EntryParams args) throws Exception {
                return Defer.success("Hello");
            }
        }).succeeded(new SuccessCallback<String, String>() {

            @Override
            public Deferred<String> run(SuccessParams<String> args) throws Exception {
                return Defer.success(args.getValue() + " Promise");
            }
        }).finish(new FinishCallback<String>() {
            @Override
            public void run(ResultParams<String> args) {
                ensureCanceled(args.asResult());
                Log.i("PromisesTest", "testCancellableWhenNotRunningFuture 2");
            }
        }).submitOn(executorService);
        canceller.cancel();

        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    public void testCancellableWhenSleep() throws Exception {

        final ExecutorService executorService = Executors.newSingleThreadExecutor();

        Promises.when(new EntryFunction<String>() {

            @Override
            public Deferred<String> run(EntryParams args) throws Exception {
                TimeUnit.SECONDS.sleep(10);
                return Defer.success("Hello");
            }
        }).succeeded(new SuccessCallback<String, String>() {

            @Override
            public Deferred<String> run(SuccessParams<String> args) throws Exception {
                return Defer.success(args.getValue() + " Promise");
            }
        }).finish(new FinishCallback<String>() {
            @Override
            public void run(ResultParams<String> args) {
                ensureCanceled(args.asResult());
                Log.i("PromisesTest", "testCancellableWhenSleep");
            }
        }).submitOn(executorService).cancel();

        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    public void testCallPromiseOverPromise() throws Exception {

        final ExecutorService executorService = Executors.newSingleThreadExecutor();

        Promises.when(new EntryFunction<String>() {

            @Override
            public Deferred<String> run(EntryParams args) throws Exception {
                return Defer.success("Hello");
            }
        }).succeeded(new SuccessCallback<String, String>() {

            @Override
            public Deferred<String> run(final SuccessParams<String> args) throws Exception {
                //プロミスの中でプロミスを同期実行する
                return Promises.when(new EntryFunction<String>() {

                    @Override
                    public Deferred<String> run(EntryParams subArgs) throws Exception {
                        return Defer.success(args.getValue() + " Promise");
                    }
                }).get(args);
            }

        }).finish(new FinishCallback<String>() {
            @Override
            public void run(ResultParams<String> args) {
                ensureSucceeded(args.asResult());
                Log.i("PromisesTest", "testCallPromiseOverPromise " + args.getValue());

            }
        }).submitOn(executorService);

        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    public void testFail() throws Exception {
        final ExecutorService executorService = Executors.newSingleThreadExecutor();

        Promises.when(new EntryFunction<String>() {

            @Override
            public Deferred<String> run(EntryParams args) throws Exception {
                throw new Exception("testFail");
            }
        }).succeeded(new SuccessCallback<String, String>() {

            @Override
            public Deferred<String> run(SuccessParams<String> args) throws Exception {
                assertTrue(false);
                throw new UnsupportedOperationException();
            }
        }).failed(new FailCallback<String>() {

            @Override
            public Deferred<String> run(FailParams<String> args) throws Exception {
                assertTrue("testFail".equals(args.getException().getMessage()));
                return Defer.success("Success");
            }
        }).finish(new FinishCallback<String>() {
            @Override
            public void run(ResultParams<String> args) {
                ensureSucceeded(args.asResult());
                Log.i("PromisesTest", "testCallPromiseOverPromise " + args.getValue());
            }
        }).submitOn(executorService);

        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }
}