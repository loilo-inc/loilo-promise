/*
 * Copyright 2015 LoiLo inc.
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

public class Scheduler {

    private static MessageLoop mMessageLoop = null;

    private static Dispatcher mDispatcher = null;

    public static Dispatcher getDispatcher() {
        Dispatcher dispatcher = mDispatcher;
        if (dispatcher == null) {
            //Double check locking.
            synchronized (Dispatcher.class) {
                if (mDispatcher == null) {
                    if (mMessageLoop == null) {
                        mMessageLoop = MessageLoop.run();
                    }
                    mDispatcher = new Dispatcher(mMessageLoop.getLooper());
                }
                dispatcher = mDispatcher;
            }
        }
        return dispatcher;
    }

    private final int mMaxThreads;
    private final ArrayList<Task> mPendingTasks = new ArrayList<>();
    private final HashSet<Task> mRunningTasks = new HashSet<>();

    public Scheduler(int maxThreads) {
        mMaxThreads = maxThreads;
    }

    private boolean isRunnable() {
        return mMaxThreads <= 0 || mMaxThreads > mRunningTasks.size();
    }

    private void runTask(final Task task) {
        task.run(new Runnable() {
            @Override
            public void run() {
                getDispatcher().run(new Runnable() {
                    @Override
                    public void run() {
                        mRunningTasks.remove(task);
                        if (!isRunnable() || mPendingTasks.size() <= 0) {
                            return;
                        }
                        final Task next = mPendingTasks.get(0);
                        mPendingTasks.remove(0);
                        runTask(next);
                    }
                });
            }
        });
        mRunningTasks.add(task);
    }

    public Canceller post(final Job job, final Object tag) {
        final Task task = new Task(job, tag);

        getDispatcher().run(new Runnable() {
            @Override
            public void run() {
                if (!isRunnable()) {
                    mPendingTasks.add(task);
                    return;
                }

                runTask(task);
            }
        });

        return new TaskCanceller(task);
    }

    private static class Task {
        private final Job mJob;
        private final Object mTag;
        private Canceller mCanceller;

        public Task(Job job, Object tag) {
            mJob = job;
            mTag = tag;
        }

        void run(Runnable postProcess) {
            if (mCanceller != null) {
                throw new UnsupportedOperationException();
            }
            mCanceller = mJob.doWork(mTag, postProcess);
        }

        void cancel() {
            if (mCanceller == null) {
                return;
            }
            mCanceller.cancel();
            mCanceller = null;
        }

        void giveUp() {
            if (mCanceller != null) {
                throw new UnsupportedOperationException();
            }
            mJob.giveUp(mTag);
        }
    }

    private class TaskCanceller implements Canceller {
        private final Task mTask;
        private final AtomicBoolean mIsCanceled = new AtomicBoolean();

        public TaskCanceller(Task task) {
            mTask = task;
        }

        @Override
        public boolean isCanceled() {
            return mIsCanceled.get();
        }

        @Override
        public void cancel() {
            if (mIsCanceled.getAndSet(true)) {
                return;
            }

            getDispatcher().run(new Runnable() {
                @Override
                public void run() {
                    if (mRunningTasks.contains(mTask)) {
                        mTask.cancel();
                        return;
                    }

                    if (!mPendingTasks.remove(mTask)) {
                        return;
                    }

                    mTask.giveUp();
                }
            });
        }
    }
}
