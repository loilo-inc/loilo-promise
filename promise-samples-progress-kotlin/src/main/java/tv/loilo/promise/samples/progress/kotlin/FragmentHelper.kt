package tv.loilo.promise.samples.progress.kotlin

import android.support.v4.app.Fragment

/**
 * Created by pepeotoito on 2015/12/27.
 */
inline fun <reified TListener> Fragment.resolveListener() : TListener?{
    return parentFragment as? TListener ?: activity as? TListener
}