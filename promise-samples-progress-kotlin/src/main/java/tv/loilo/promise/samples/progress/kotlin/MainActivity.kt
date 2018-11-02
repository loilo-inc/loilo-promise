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

package tv.loilo.promise.samples.progress.kotlin

import android.os.Bundle
import android.view.View
import android.support.v7.app.AppCompatActivity

class MainActivity : AppCompatActivity()
        , SampleProgressBarDialogFragment.OnFinishedListener
        , SampleProgressSpinnerDialogFragment.OnFinishedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.main_show_progress_bar_button)?.setOnClickListener {
            SampleProgressBarDialogFragment().show(supportFragmentManager, null)
        }

        findViewById<View>(R.id.main_show_progress_spinner_button)?.setOnClickListener {
            SampleProgressSpinnerDialogFragment().show(supportFragmentManager, null)
        }
    }

    override fun onSampleProgressBarCanceled() {
        MessageDialogFragment.newInstance("Canceled.").show(supportFragmentManager, null)
    }

    override fun onSampleProgressBarFailed(e: Exception) {
        MessageDialogFragment.newInstance("Failed. ${e.message}").show(supportFragmentManager, null)
    }

    override fun onSampleProgressBarSucceeded() {
        MessageDialogFragment.newInstance("Succeeded.").show(supportFragmentManager, null)
    }

    override fun onSampleProgressSpinnerCanceled() {
        MessageDialogFragment.newInstance("Canceled.").show(supportFragmentManager, null)
    }

    override fun onSampleProgressSpinnerFailed(e: Exception) {
        MessageDialogFragment.newInstance("Failed. ${e.message}").show(supportFragmentManager, null)
    }

    override fun onSampleProgressSpinnerSucceeded() {
        MessageDialogFragment.newInstance("Succeeded.").show(supportFragmentManager, null)
    }
}
