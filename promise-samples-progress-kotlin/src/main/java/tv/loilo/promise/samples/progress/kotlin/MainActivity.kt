package tv.loilo.promise.samples.progress.kotlin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity()
        , SampleProgressBarDialogFragment.OnFinishedListener
        , SampleProgressSpinnerDialogFragment.OnFinishedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById(R.id.main_show_progress_bar_button)?.setOnClickListener {
            SampleProgressBarDialogFragment().show(supportFragmentManager, null)
        }

        findViewById(R.id.main_show_progress_spinner_button)?.setOnClickListener {
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
