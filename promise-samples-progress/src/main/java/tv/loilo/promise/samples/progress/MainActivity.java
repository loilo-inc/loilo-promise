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
