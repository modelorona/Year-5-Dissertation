package com.anguel.dissertation.persistence.coroutines.app

import android.content.Context
import com.anguel.dissertation.persistence.entity.SessionDatabase
import com.anguel.dissertation.persistence.entity.app.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppCoroutines {

    companion object {
        @JvmStatic
        suspend fun saveApps(apps: List<App>, context: Context): List<Long> {
            return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                SessionDatabase.getInstance(context).appDao().insertApps(apps)
            }
        }
    }
}