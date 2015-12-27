package tv.loilo.promise.kotlin

import tv.loilo.promise.*

/**
 * Created by pepeotoito on 2015/12/26.
 */

fun runOnUi(process: () -> Unit) {
    Dispatcher.getMainDispatcher().run(process)
}

fun postOnUi(process: () -> Unit) {
    Dispatcher.getMainDispatcher().post(process)
}

fun <T> callOnUi(process: () -> T): Deferred<T> {
    return Dispatcher.getMainDispatcher().call(process)
}

fun <T> promiseWhen(f: (WhenParams) -> Deferred<T>): Promise<T> {
    return Promises.`when`(f)
}

fun <T> promiseRepeat(f: (RepeatParams) -> Deferred<T>): Repeat<T> {
    return Promises.repeat(f)
}

fun <T> promiseForEach(ite: Iterable<T>, f: (ForEachParams<T>) -> Deferred<ForEachOp>): Promise<Unit> {
    return Promises.forEach(ite, f).exchange(Unit)
}

fun <T> promiseSuccess(value: T): Promise<T> {
    return Promises.success(value)
}

fun <T> promiseFail(e: Exception): Promise<T> {
    return Promises.fail(e)
}

fun <T> promiseCancel(): Promise<T> {
    return Promises.cancel<T>()
}

fun <T> promiseNotImpl(): Promise<T> {
    return Promises.notImpl<T>()
}

fun <T> deferSuccess(value: T): Deferred<T> {
    return Defer.success(value)
}

fun <T> deferFail(e: Exception): Deferred<T> {
    return Defer.fail(e)
}

fun <T> deferCancel(): Deferred<T> {
    return Defer.cancel()
}

fun <T> deferNotImpl(): Deferred<T> {
    return Defer.notImpl()
}

fun <T> defer(f: () -> T): Deferred<T> {
    return deferSuccess(f())
}

fun <T> deferrable(f: (Deferrable<T>) -> Unit): Deferred<T> {
    return Deferrable<T>().apply {
        f(this)
    }
}

inline fun <T, R> Result<T>.whenSucceeded(f: (T) -> R): R? {
    if (this.cancelToken.isCanceled) {
        return null
    }
    this.exception?.let {
        return null
    }

    return f(this.value)
}

inline fun <T, R> Result<T>.whenSucceeded(f: (T) -> R, whenFailed: (Exception) -> R): R? {
    if (this.cancelToken.isCanceled) {
        return null
    }
    this.exception?.let {
        return whenFailed(it)
    }

    return f(this.value)
}

inline fun <T, R> Result<T>.whenFailed(f: (Exception) -> R): R? {
    if (this.cancelToken.isCanceled) {
        return null
    }
    this.exception?.let {
        return f(it)
    }

    return null
}

inline fun <T, R> Result<T>.whenFailed(f: (Exception) -> R, whenSucceeded: (T) -> R): R? {
    if (this.cancelToken.isCanceled) {
        return null
    }
    this.exception?.let {
        return f(it)
    }

    return whenSucceeded(this.value)
}

inline fun <T, R> ResultParams<T>.whenSucceeded(f: (T) -> R): R? {
    return this.asResult().whenSucceeded(f)
}

inline fun <T, R> ResultParams<T>.whenSucceeded(f: (T) -> R, whenFailed: (Exception) -> R): R? {
    return this.asResult().whenSucceeded(f, whenFailed)
}

inline fun <T, R> ResultParams<T>.whenFailed(f: (Exception) -> R): R? {
    return this.asResult().whenFailed(f)
}

inline fun <T, R> ResultParams<T>.whenFailed(f: (Exception) -> R, whenSucceeded: (T) -> R): R? {
    return this.asResult().whenFailed(f, whenSucceeded)
}