package tv.loilo.promise;

/**
 * Created by Junpei on 2015/06/16.
 */
public final class Defer {
    private Defer() {
    }

    public static <T> Deferred<T> complete(final Result<T> result) {
        return new Completed<>(result);
    }

    public static <T> Deferred<T> success(final T value) {
        return new Completed<>(Results.success(value));
    }

    public static <T> Deferred<T> fail(final Exception e) {
        return new Completed<>(Results.<T>fail(e));
    }

    public static <T> Deferred<T> cancel() {
        return new Completed<>(Results.<T>cancel());
    }

    public static <T> Deferred<T> notImpl() {
        return new Completed<>(Results.<T>notImpl());
    }

    public static <TIn, TOut> Deferred<TOut> exchangeValue(final Result<TIn> result, TOut replace) {
        return new Completed<>(Results.exchangeValue(result, replace));
    }

    private static class Completed<T> implements Deferred<T> {

        private final Result<T> mResult;

        public Completed(final Result<T> result) {
            mResult = result;
        }

        @Override
        public Result<T> getResult() {
            return mResult;
        }
    }
}
