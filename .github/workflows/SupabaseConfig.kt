package com.sleepgate.app

/**
 * Put your Supabase project values here for a real cloud connection.
 * Never put the service_role key in an Android app. Use only the public anon/publishable key.
 */
object SupabaseConfig {
    const val URL = "https://YOUR_PROJECT.supabase.co"
    const val ANON_KEY = "YOUR_PUBLIC_ANON_OR_PUBLISHABLE_KEY"

    val configured: Boolean
        get() = !URL.contains("YOUR_PROJECT") && !ANON_KEY.contains("YOUR_PUBLIC")
}
