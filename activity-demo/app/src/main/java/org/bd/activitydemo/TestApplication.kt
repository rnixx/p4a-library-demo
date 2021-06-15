package org.bd.activitydemo

import android.app.Application
import androidx.work.Configuration

class TestApplication : Application(), Configuration.Provider {

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setDefaultProcessName("org.bd.activitydemo")
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
}
