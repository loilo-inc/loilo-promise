package tv.loilo.promise.uitest.kotlin

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import tv.loilo.promise.Cancellable
import tv.loilo.promise.kotlin.postOnSlaveWithCancel
import tv.loilo.promise.kotlin.postOnUiWithCancel
import tv.loilo.promise.kotlin.runOnUi

class MainActivity : AppCompatActivity() {

    private var cancellable: Cancellable? = null
    private var slaveCancellable : Cancellable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById(R.id.greet_after_3sec_button)?.setOnClickListener {
            cancellable?.cancel()
            cancellable = postOnUiWithCancel({
                val threadId = Thread.currentThread().id
                Toast.makeText(this, "Hello id=$threadId", Toast.LENGTH_SHORT).show()
            }, 3000)
        }

        findViewById(R.id.cancel_button)?.setOnClickListener {
            cancellable?.cancel()
            cancellable = null
        }

        findViewById(R.id.slave_greet_after_3sec_button)?.setOnClickListener {
            slaveCancellable?.cancel()
            slaveCancellable = postOnSlaveWithCancel({
                val threadId = Thread.currentThread().id
                runOnUi {
                    Toast.makeText(this, "Hello(Slave) id=$threadId", Toast.LENGTH_SHORT).show()
                }
            }, 3000)
        }

        findViewById(R.id.slave_cancel_button)?.setOnClickListener {
            slaveCancellable?.cancel()
            slaveCancellable = null
        }
    }
}
