package com.askornilov.app

import android.app.Application
import com.askornilov.app.data.AppDatabase

class App : Application() {
    val db: AppDatabase by lazy { AppDatabase.create(this) }
}
