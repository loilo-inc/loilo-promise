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

package tv.loilo.promise.uitest.kotlin

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import tv.loilo.promise.Cancellable
import tv.loilo.promise.kotlin.postOnBgWithCancel
import tv.loilo.promise.kotlin.postOnUiWithCancel
import tv.loilo.promise.kotlin.runOnUi

class MainActivity : AppCompatActivity() {

    private var uiCancellable: Cancellable? = null
    private var bgCancellable: Cancellable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.greet_after_3sec_button)?.setOnClickListener {
            uiCancellable?.cancel()
            uiCancellable = postOnUiWithCancel({
                val threadId = Thread.currentThread().id
                Toast.makeText(this, "Hello id=$threadId", Toast.LENGTH_SHORT).show()
            }, 3000)
        }

        findViewById<View>(R.id.cancel_button)?.setOnClickListener {
            uiCancellable?.cancel()
            uiCancellable = null
        }

        findViewById<View>(R.id.bg_greet_after_3sec_button)?.setOnClickListener {
            bgCancellable?.cancel()
            bgCancellable = postOnBgWithCancel({
                val threadId = Thread.currentThread().id
                runOnUi {
                    Toast.makeText(this, "Hello(BG) id=$threadId", Toast.LENGTH_SHORT).show()
                }
            }, 3000)
        }

        findViewById<View>(R.id.bg_cancel_button)?.setOnClickListener {
            bgCancellable?.cancel()
            bgCancellable = null
        }
    }
}
