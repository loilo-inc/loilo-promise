# loilo-promise

A simple promise library for Android.

The library can work with java and kotlin.

The purpose and use of this library is the following.

* Avoiding the callback hell on asynchronous operations.
* Propagating the exception, which occurred in the callback, to the next callback.
* Canceling the asynchronous operation immediately, even if you had used blocking API, such as the `java.lang.Thread#sleep()`. And detecting canceled.

## Download

loilo-promise is available on Marven Central.

via Gradle

```groovy
dependencies {
    compile 'tv.loilo.promise:promise:0.4.3'
    compile 'tv.loilo.promise:promise-support:0.4.3'
}
```

## with Kotlin

Import the following files to your project.

 * [PromiseKotlin.kt](https://github.com/loilo-inc/loilo-promise/blob/master/promise-kotlin/src/main/java/tv/loilo/promise/kotlin/PromiseKotlin.kt)
 * [PromiseSupportKotlin.kt](https://github.com/loilo-inc/loilo-promise/blob/master/promise-support-kotlin/src/main/java/tv/loilo/promise/support/kotlin/PromiseSupportKotlin.kt)

> Kotlin seems to require the build by same version of kotlin compiler in all module(?).
Thus building our Kotlin code in your project looks like better than that your project depends to modules built by us.

## Asynchronous Callback Chain

Promise can chain asynchronous callbacks.

by Java

```java
    final Canceller canceller = Promises.when(new WhenCallback<String>() {
        @Override
        public Deferred<String> run(WhenParams params) throws Exception {
            return doSomething(params);
        }
    }).succeeded(new SuccessCallback<String, String>() {
        @Override
        public Deferred<String> run(SuccessParams<String> params) throws Exception {
            return handleSuccessResult(params);
        }
    }).failed(new FailCallback<String>() {
        @Override
        public Deferred<String> run(FailParams<String> params) throws Exception {
            return handleFailResult(params);
        }
    }).then(new ThenCallback<String, String>() {
        @Override
        public Deferred<String> run(ThenParams<String> params) throws Exception {
            return handleSuccessOrFailOrCancelResult(params);
        }
    }).watch(new WatchCallback<String>() {
        @Override
        public void run(final WatchParams<String> params) throws Exception {
            watchSuccessOrFailOrCancelResult(params);
        }
    }).finish(new FinishCallback<String>() {
        @Override
        public void run(final FinishParams<String> params) {
            // Dispatch main thread.
            Dispatcher.getMainDispatcher().run(new Runnable() {
                @Override
                public void run() {
                    if (params.getCancelToken().isCanceled()) {
                        //Promise is canceled.
                        return;
                    }

                    final Exception exception = params.getException();
                    if (e == null) {
                        //Promise is failed
                        return;
                    }

                    //Promise is succeeded.
                    final String value = params.getValue();
                }
            });
        }
    }).submit();
```

by Kotlin

```kotlin
    val canceller = promiseWhen {
        doSomething(it)
    }.succeeded {
        handleSuccessResult(it)
    }.failed {
        handleFailResult(it)
    }.then {
        handleSuccessOrFailOrCancelResult(it)
    }.watch {
        watchSuccessOrFailOrCancelResult(it)
    }.finish {
        //Dispatch main thread.
        runOnUi {
            it.whenSucceeded({
                //Promise succeeded.
                val value = it
            }, whenFailed = {
                //Promise failed.
                val exception = it
            }) ?: run{
                //Promise canceled.
            }
        }
    }.submit()
```

When you call `submit()`, the library executes callback chain asynchronously.

## Result

Result of Promise takes one of the following three states.

* Succeeded.
* Failed.
* Canceled.

You will receive such a result at callbacks.

## Exception Handling

When unhandled exception occurred on your callback implementation,
you can get the exception at chained callbacks.

## Cancellation

The library support cancellation of the asynchronous calls.

Cancellation will be notified by the boolean flag and the
[`java.lang.Thread.interrupt()`](http://developer.android.com/intl/ja/reference/java/lang/Thread.html#interrupt()) to your callbacks.

By `java.lang.Thread.interrupt()`, you can cancel blocking API immediately,
such as `java.lang.Thread.sleep()` and `java.util.concurrent.locks.Lock.lockInterruptibly()`.

You can cancel promise by calling `Canceller.cancel()`.

by Java
```java
    final Canceller canceller = Promises.when(new WhenCallback<String>() {
        @Override
        public Deferred<String> run(WhenParams params) throws Exception {
            return doSomething(params);
        }
    }).submit();

    canceller.cancel()
```

by Kotlin
```kotlin
    val canceller = promiseWhen {
        doSomething(it)
    }.submit()

    canceller.cancel()
```

In addition,
when `java.lang.InterruptedException` or `java.util.concurrent.CancellationException` thrown,
the library catches them and interprets as that promise was canceled.

## Nested Promise

You can call promise in promise callbacks.

by Java
```java
    Promises.when(new WhenCallback<String>() {
        @Override
        public Deferred<String> run(WhenParams params) throws Exception {
            return Promises.when(new WhenCallback<String>() {
                @Override
                public Deferred<String> run(WhenParams params) throws Exception {
                    return doSomething(params);
                }
            }).get(params);
        }
    }).submit();
```

by Kotlin
```kotlin
    promiseWhen {
        promiseWhen {
            doSomething(it)
        }.get(it)
    }.submit()
```

## Asynchronous Calls to Other Libraries

You can use other asynchronous API in promise callbacks by `Deferrable` class.

by Java
```java
    Promises.when(new WhenCallback<String>() {
        @Override
        public Deferred<String> run(WhenParams params) throws Exception {
            final Deferrable<String> deferrable = new Deferrable<>();

            final Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if(params.getCancelToken().isCanceled()){
                            //Notify canceled.
                            deferrable.setCanceled();
                            return;
                        }
                        //Do your asynchronous process.
                        Thread.sleep(5000);

                        //Notify succeeded and value.
                        deferrable.setSucceeded("Sleep succeeded.");
                    } catch (InterruptedException e) {
                        //Notify canceled.
                        deferrable.setCanceled();
                    } catch (Exception e){
                        //Notify failed and cause.
                        deferrable.setFailed(e);
                    }
                }
            });

            deferrable.setCancellable(new Cancellable() {
                @Override
                public void cancel() {
                    //Cancel thread.
                    thread.interrupt();
                }
            });

            thread.start();

            return deferrable;
        }
    }).finish(new FinishCallback<String>() {
        @Override
        public void run(FinishParams<String> params) {
            //Calling on main thread.
            if (params.getCancelToken().isCanceled()) {
                //Promise is canceled.
                return;
            }

            final Exception e = params.getException();
            if (e == null) {
                //Promise is failed
                return;
            }

            //Promise is succeeded.
            final String value = params.getValue();
            assert(value == "Sleep succeeded.");
        }
    }).submit();
```

by Kotlin
```kotlin
    promiseWhen { p ->
        deferrable<String> { d ->
            val thread = Thread {
                try{
                    if(p.cancelToken.isCanceled){
                        //Notify canceled.
                        d.setCanceled()
                    } else {
                        //Do your asynchronous process.
                        Thread.sleep(5000)

                        //Notify succeeded and value.
                        d.setSucceeded("Sleep succeeded.")
                    }
                } catch (e : InterruptedException){
                    //Notify canceled.
                    d.setCanceled()
                } catch (e : Exception){
                    //Notify failed and cause.
                    d.setFailed(e)
                }
            }

            d.setCancellable {
                //Cancel thread.
                thread.interrupt()
            }

            thread.start()
        }
    }.finish {
        it.whenSucceeded({
            //Promise succeeded.
            val value = it
            assert(value == "Sleep succeeded.")
        }, whenFailed = {
            //Promise failed.
            val exception = it
        }) ?: run {
            //Promise canceled.
        }
    }.submit()
```

## Loop Asynchronous Calls

By using `Promises.repeat().until()` and `Promises.forEach()`,
you can call asynchronous API repeatedly without callback hell and reclusive call.

### Repeat Asynchronous Calls

by Java
```java
    Promises.repeat(new RepeatCallback<String>() {
        @Override
        public Deferred<String> run(RepeatParams params) throws Exception {
            return doSomething(params);
        }
    }).until(new UntilCallback<String>() {
        @Override
        public boolean run(UntilParams<String> params) throws Exception {
            if (params.getCancelToken().isCanceled()) {
                // End of loop.
                return true;
            }
            final Exception e = params.getException();
            if(e != null && params.getIndex().incrementAndGet() <= 3){
                //Retrying for error until three times.
                return false;
            }

            //End of loop.
            return true;
        }
    }).finish(new FinishCallback<String>() {
        @Override
        public void run(FinishParams<String> params) {
            //The params contains last RepeatCallback result.
        }
    }).submit();
```

by Kotlin
```kotlin
        promiseRepeat {
            doSomething(it)
        }.until { p ->
            if(p.index.incrementAndGet() <= 3){
                p.whenFailed {
                    //Retrying for error until three times.
                    return@until false
                }
            }

            //End of loop.
            true
        }.finish {
            //The params contains last RepeatCallback result.
        }.submit()
```

### Enumerate Parameters and Asynchronous Calls

by Java
```java
    final List<Integer> list = new ArrayList<>();
    list.add(1);
    list.add(2);
    list.add(3);

    Promises.forEach(list, new AtomicInteger(), new ForEachCallback<Integer, AtomicInteger>() {
        @Override
        public Deferred<ForEachOp> run(ForEachParams<Integer, AtomicInteger> params) throws Exception {
            params.getOperand().addAndGet(params.getValue());
            return Defer.success(ForEachOp.CONTINUE);
        }
    }).finish(new FinishCallback<AtomicInteger>() {
        @Override
        public void run(FinishParams<AtomicInteger> params) {
            //Calling on main thread.
            if (params.getCancelToken().isCanceled()) {
                //Promise is canceled.
                return;
            }

            final Exception e = params.getException();
            if (e == null) {
                //Promise is failed
                return;
            }

            //Promise is succeeded.
            final int value = params.getValue().get();
            assert (value == (1 + 2 + 3));
        }
    }).submit();
```

by Kotlin
```kotlin
    val list = listOf(1, 2, 3)

    promiseForEach(list, AtomicInteger(), {
        defer {
            it.operand.addAndGet(it.value)
            ForEachOp.CONTINUE
        }
    }).finish {
        it.whenSucceeded({
            //Promise succeeded.
            val value = it.get()
            assert(value ==  1 + 2 + 3)
        }, whenFailed = {
            //Promise failed.
            val exception = it
        }) ?: run {
            //Promise canceled.
        }
    }.submit()
```
## Dispatch Android Main Thread

If you want to handle result on main(UI) thread,
you can do it by calling `Dispatcher` class's functions.

by Java
```java
    Promises.when(new WhenCallback<String>() {
        @Override
        public Deferred<String> run(WhenParams params) throws Exception {
            return Dispatcher.getMainDispatcher().call(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    //Calling on main thread.
                    return "Hello Promise";
                }
            });
        }
    }).finish(new FinishCallback<String>() {
        @Override
        public void run(final FinishParams<String> params) {
            Dispatcher.getMainDispatcher().run(new Runnable() {
                @Override
                public void run() {
                    //Calling on main thread.
                    if (params.getCancelToken().isCanceled()) {
                        //Promise is canceled.
                        return;
                    }

                    final Exception e = params.getException();
                    if (e == null) {
                        //Promise is failed
                        return;
                    }

                    //Promise is succeeded.
                    final String value = params.getValue();
                    assert(value == "Hello Promise");
                }
            });
        }
    }).submit();
```

by Kotlin
```kotlin
    promiseWhen {
        callOnUi {
            "Hello Promise"
        }
    }.finish {
        //Dispatch main thread.
        runOnUi {
            it.whenSucceeded({
                //Promise succeeded.
                val value = it
                assert(value == "Hello Promise")
            }, whenFailed = {
                //Promise failed.
                val exception = it
            }) ?: run{
                //Promise canceled.
            }
        }
    }.submit()
```

> If you call any blocking API on main(UI) thread,
`Dispatcher.call()` has a risk of deadlock.

## Run on Your ExecutorService

by Java
```java
    Promises.when(new WhenCallback<String>() {
        @Override
        public Deferred<String> run(WhenParams params) throws Exception {
            return doSomething(params);
        }
    }).submitOn(YOUR_EXECUTOR);
```

by Kotlin
```kotlin
    promiseWhen {
        doSomething(it)
    }.submitOn(YOUR_EXECUTOR)
```

Nested Promise can also.

by Java
```java
    Promises.when(new WhenCallback<String>() {
        @Override
        public Deferred<String> run(WhenParams params) throws Exception {
            return Promises.when(new WhenCallback<String>() {
                @Override
                public Deferred<String> run(WhenParams params) throws Exception {
                    return doSomething(params);
                }
            }).getOn(YOUR_EXECUTOR, params);
        }
    }).submit();
```

by Kotlin
```kotlin
    promiseWhen {
        promiseWhen {
            doSomething(it)
        }.getOn(YOUR_EXECUTOR, it)
    }.submit()
```

## Android Loader Implementation

Promise has implementation of `android.support.v4.content.Loader`.
You can use it as the Android `AsyncTaskLoader` class.

See the following sample code.

 * [promise-samples-progress](https://github.com/loilo-inc/loilo-promise/tree/master/promise-samples-progress/src/main/java/tv/loilo/promise/samples/progress)
 * [promise-samples-progress-kotlin](https://github.com/loilo-inc/loilo-promise/tree/master/promise-samples-progress-kotlin/src/main/java/tv/loilo/promise/samples/progress/kotlin)

## License

    Copyright 2015-2016 LoiLo inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
