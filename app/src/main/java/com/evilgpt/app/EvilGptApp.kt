package com.evilgpt.app

import android.app.Application
import com.evilgpt.app.data.SupabaseClientProvider

class EvilGptApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SupabaseClientProvider.init(this)
    }
}
