package com.afrivest.app.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class CrashlyticsTree : Timber.Tree() {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun isLoggable(tag: String?, priority: Int) = priority >= android.util.Log.WARN

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        tag?.let { crashlytics.setCustomKey("timber_tag", it) }
        crashlytics.log("$tag: $message")
        t?.let { crashlytics.recordException(it) }
    }
}