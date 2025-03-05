package com.example.thecube



import android.app.Application
import com.cloudinary.android.MediaManager

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Cloudinary configuration
        val config = mapOf(
            "cloud_name" to "dlqydpa1y",
            "api_key" to "686692495789422",
            "api_secret" to "4giahbSne3ZqZlZO6T_e9Sa9hLw",
            "secure" to true
        )

        // Initialize Cloudinary's MediaManager with the configuration
        MediaManager.init(this, config)
    }
}
