package tv.loilo.promise.uitest.kotlin

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import tv.loilo.promise.Cancellable
import tv.loilo.promise.kotlin.postOnUiWithCancel

class MainActivity : AppCompatActivity() {

    private var cancellable: Cancellable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById(R.id.greet_after_3sec_button)?.setOnClickListener {
            cancellable?.cancel()
            cancellable = postOnUiWithCancel({
                Toast.makeText(this, "Hello", Toast.LENGTH_SHORT).show()
            }, 3000)
        }

        findViewById(R.id.cancel_button)?.setOnClickListener {
            cancellable?.cancel()
            cancellable = null
        }
    }
}
