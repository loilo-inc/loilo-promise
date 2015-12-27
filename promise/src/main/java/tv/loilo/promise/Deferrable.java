package tv.loilo.promise;

/**
 * Created by Junpei on 2015/06/16.
 */
public final class Deferrable<T> implements Deferred<T> {

    private Cancellable mCancellable;
    private Pipe<Result<T>> mResultPipe;

    public Deferrable() {
        mResultPipe = new Pipe<>();
    }

    private void cancel() {
        final Cancellable cancellable = mCancellable;
        if (cancellable == null) {
            return;
        }
        cancellable.cancel();
    }

    @Override
    public Result<T> getResult() {
        boolean interrupted = false;
        Result<T> result;
        while (true) {
            try {
                result = mResultPipe.get();
                break;
            } catch (InterruptedException e) {
                interrupted = true;
                cancel();
            }
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
        }

        return result;
    }

    public void setResult(Result<T> result) {
        mResultPipe.set(result);
    }

    public void setCancellable(Cancellable cancellable) {
        mCancellable = cancellable;
    }

    public void setSucceeded(T value) {
        setResult(Results.success(value));
    }

    public void setCanceled() {
        setResult(Results.<T>cancel());
    }

    public void setFailed(Exception e) {
        setResult(Results.<T>fail(e));
    }
}
