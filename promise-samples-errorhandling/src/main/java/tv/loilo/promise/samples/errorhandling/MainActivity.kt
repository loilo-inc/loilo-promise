/*
 * Copyright 2016 LoiLo inc.
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

package tv.loilo.promise.samples.errorhandling

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import tv.loilo.promise.kotlin.promiseWhen

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById(R.id.crash_on_error_button)?.setOnClickListener {
            ErrorOnCrashDialogFragment().show(supportFragmentManager, null)
        }

        findViewById(R.id.crash_on_unhandled_exception_button)?.setOnClickListener {
            promiseWhen<Unit> {
                throw Exception()
            }.submit()
        }
    }
}
