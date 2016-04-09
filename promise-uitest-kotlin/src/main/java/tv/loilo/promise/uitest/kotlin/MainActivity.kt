package tv.loilo.promise.uitest.kotlin

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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

        findViewById(R.id.greet_after_3sec_button)?.setOnClickListener {
            uiCancellable?.cancel()
            uiCancellable = postOnUiWithCancel({
                val threadId = Thread.currentThread().id
                Toast.makeText(this, "Hello id=$threadId", Toast.LENGTH_SHORT).show()
            }, 3000)
        }

        findViewById(R.id.cancel_button)?.setOnClickListener {
            uiCancellable?.cancel()
            uiCancellable = null
        }

        findViewById(R.id.bg_greet_after_3sec_button)?.setOnClickListener {
            bgCancellable?.cancel()
            bgCancellable = postOnBgWithCancel({
                val threadId = Thread.currentThread().id
                runOnUi {
                    Toast.makeText(this, "Hello(BG) id=$threadId", Toast.LENGTH_SHORT).show()
                }
            }, 3000)
        }

        findViewById(R.id.bg_cancel_button)?.setOnClickListener {
            bgCancellable?.cancel()
            bgCancellable = null
        }
    }
}
