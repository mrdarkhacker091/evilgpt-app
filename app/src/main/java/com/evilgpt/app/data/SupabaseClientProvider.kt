package com.evilgpt.app.data

import android.content.Context

/** Lightweight placeholder — full supabase-kt wired when building on a machine with more RAM. */
object SupabaseClientProvider {
    fun init(context: Context) {
        // no-op in slim build; auth falls back to local skip-login
    }
}
