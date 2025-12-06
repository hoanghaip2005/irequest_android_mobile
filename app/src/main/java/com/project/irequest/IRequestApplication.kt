package com.project.irequest

import android.app.Application
import com.facebook.appevents.AppEventsLogger

class IRequestApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Facebook SDK will auto-initialize via meta-data in AndroidManifest.xml
        // Just activate app events
        AppEventsLogger.activateApp(this)
    }
}
