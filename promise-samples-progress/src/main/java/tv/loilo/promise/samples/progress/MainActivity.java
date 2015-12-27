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

package tv.loilo.promise.samples.progress;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity implements SampleProgressBarDialogFragment.OnFinishedListener, SampleProgressSpinnerDialogFragment.OnFinishedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.main_show_progress_bar_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SampleProgressBarDialogFragment().show(getSupportFragmentManager(), null);
            }
        });

        findViewById(R.id.main_show_progress_spinner_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SampleProgressSpinnerDialogFragment().show(getSupportFragmentManager(), null);
            }
        });
    }

    @Override
    public void onSampleProgressBarCanceled() {
        MessageDialogFragment.newInstance("Canceled.").show(getSupportFragmentManager(), null);
    }

    @Override
    public void onSampleProgressBarFailed(@NonNull Exception e) {
        MessageDialogFragment.newInstance("Failed. " + e.getMessage()).show(getSupportFragmentManager(), null);
    }

    @Override
    public void onSampleProgressBarSucceeded() {
        MessageDialogFragment.newInstance("Succeeded.").show(getSupportFragmentManager(), null);
    }

    @Override
    public void onSampleProgressSpinnerCanceled() {
        MessageDialogFragment.newInstance("Canceled.").show(getSupportFragmentManager(), null);
    }

    @Override
    public void onSampleProgressSpinnerFailed(@NonNull Exception e) {
        MessageDialogFragment.newInstance("Failed. " + e.getMessage()).show(getSupportFragmentManager(), null);
    }

    @Override
    public void onSampleProgressSpinnerSucceeded() {
        MessageDialogFragment.newInstance("Succeeded.").show(getSupportFragmentManager(), null);
    }
}
