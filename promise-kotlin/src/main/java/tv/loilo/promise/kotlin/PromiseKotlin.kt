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

package tv.loilo.promise.kotlin

import tv.loilo.promise.*
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

fun runOnUi(process: () -> Unit) {
    Dispatcher.getMainDispatcher().run(process)
}

fun postOnUi(process: () -> Unit) {
    Dispatcher.getMainDispatcher().post(process)
}

fun postOnUi(process: () -> Unit, delayMills: Long) {
    Dispatcher.getMainDispatcher().post(process, delayMills)
}

fun postOnUiWithCancel(process: () -> Unit): Cancellable {
    val isCalled = AtomicBoolean()
    val runnable = Runnable {
        isCalled.set(true)
        process.invoke()
    }
    Dispatcher.getMainDispatcher().post(runnable)
    return Cancellable {
        if (!isCalled.get()) {
            Dispatcher.getMainDispatcher().remove(runnable)
        }
    }
}

fun postOnUiWithCancel(process: () -> Unit, delayMills: Long): Cancellable {
    val isCalled = AtomicBoolean()
    val runnable = Runnable {
        isCalled.set(true)
        process.invoke()
    }
    Dispatcher.getMainDispatcher().post(runnable, delayMills)
    return Cancellable {
        if (!isCalled.get()) {
            Dispatcher.getMainDispatcher().remove(runnable)
        }
    }
}

fun <T> callOnUi(process: () -> T): Deferred<T> {
    return Dispatcher.getMainDispatcher().call(process)
}

fun runOnBg(process: () -> Unit) {
    Dispatcher.getSubDispatcher().run(process)
}

fun postOnBg(process: () -> Unit) {
    Dispatcher.getSubDispatcher().post(process)
}

fun postOnBg(process: () -> Unit, delayMills: Long) {
    Dispatcher.getSubDispatcher().post(process, delayMills)
}

fun postOnBgWithCancel(process: () -> Unit): Cancellable {
    val isCalled = AtomicBoolean()
    val runnable = Runnable {
        isCalled.set(true)
        process.invoke()
    }
    Dispatcher.getSubDispatcher().post(runnable)
    return Cancellable {
        if (!isCalled.get()) {
            Dispatcher.getSubDispatcher().remove(runnable)
        }
    }
}

fun postOnBgWithCancel(process: () -> Unit, delayMills: Long): Cancellable {
    val isCalled = AtomicBoolean()
    val runnable = Runnable {
        isCalled.set(true)
        process.invoke()
    }
    Dispatcher.getSubDispatcher().post(runnable, delayMills)
    return Cancellable {
        if (!isCalled.get()) {
            Dispatcher.getSubDispatcher().remove(runnable)
        }
    }
}

fun <T> callOnBg(process: () -> T): Deferred<T> {
    return Dispatcher.getSubDispatcher().call(process)
}

fun <T> promiseWhen(f: (WhenParams) -> Deferred<T>): Promise<T> {
    return Promises.`when`(f)
}

fun <T> promiseRepeat(f: (RepeatParams) -> Deferred<T>): Repeat<T> {
    return Promises.repeat(f)
}

fun <T> promiseForEach(ite: Iterable<T>, f: (ForEachParams<T, Unit>) -> Deferred<ForEachOp>): Promise<Unit> {
    return Promises.forEach(ite, Unit, f)
}

fun <TIn, TOut> promiseForEach(ite: Iterable<TIn>, operand: TOut, f: (ForEachParams<TIn, TOut>) -> Deferred<ForEachOp>): Promise<TOut> {
    return Promises.forEach(ite, operand, f)
}

fun <T> promiseWhenAll(vararg promises: Promise<T>): Promise<List<T>> {
    return Promises.whenAll(*promises)
}

fun <T> promiseWhenAny(vararg promises: Promise<T>): Promise<T> {
    return Promises.whenAny(*promises)
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