package tv.loilo.promise;

/**
 * Created by Junpei on 2015/06/12.
 */
public final class CancelTokens {

    static final CancelToken CANCELED = new StaticCancelToken(true);
    static final CancelToken NONE = new StaticCancelToken(false);
    private CancelTokens() {
    }

    private static class StaticCancelToken implements CancelToken {
        private final boolean mIsCanceled;

        public StaticCancelToken(boolean isCanceled) {
            mIsCanceled = isCanceled;
        }

        @Override
        public boolean isCanceled() {
            return mIsCanceled;
        }
    }
}
