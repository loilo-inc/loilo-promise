/*
 * Copyright (c) 2015-2016 LoiLo inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tv.loilo.promise;

/**
 * Class to be able to synchronously get the result of asynchronous processing.
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
