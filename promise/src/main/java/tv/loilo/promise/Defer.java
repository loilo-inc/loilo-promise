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
 * Class to make a {@link Deferred}.
 */
public final class Defer {
    private Defer() {
    }

    public static <T> Deferred<T> complete(Result<T> result) {
        return new Completed<>(result);
    }

    public static <T> Deferred<T> success(T value) {
        return new Completed<>(Results.success(value));
    }

    public static <T> Deferred<T> fail(Throwable e) {
        return new Completed<>(Results.<T>fail(e));
    }

    public static <T> Deferred<T> cancel() {
        return new Completed<>(Results.<T>cancel());
    }

    public static <T> Deferred<T> notImpl() {
        return new Completed<>(Results.<T>notImpl());
    }

    public static <TIn, TOut> Deferred<TOut> exchangeValue(Result<TIn> result, TOut replace) {
        return new Completed<>(Results.exchangeValue(result, replace));
    }

    private static class Completed<T> implements Deferred<T> {

        private final Result<T> mResult;

        public Completed(Result<T> result) {
            mResult = result;
        }

        @Override
        public Result<T> getResult() {
            return mResult;
        }
    }
}
