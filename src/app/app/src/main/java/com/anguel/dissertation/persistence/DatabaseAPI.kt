package com.anguel.dissertation.persistence

import android.content.Context
import com.anguel.dissertation.persistence.coroutines.app.AppCoroutines
import com.anguel.dissertation.persistence.coroutines.session.SessionCoroutines
import com.anguel.dissertation.persistence.entity.SessionWithApps
import com.anguel.dissertation.persistence.entity.app.App
import com.anguel.dissertation.persistence.entity.session.Session
import com.google.firebase.perf.metrics.AddTrace
import io.sentry.Sentry
import kotlinx.coroutines.*

class DatabaseAPI private constructor() {

    @AddTrace(name = "databaseSaveSession")
    fun saveSession(session: Session, context: Context): Long {
        return runBlocking {
            try {
                SessionCoroutines.saveSession(session, context)
            } catch (e: Exception) {
                Sentry.captureException(e)
                -1L
            }
        }
    }

    @AddTrace(name = "databaseGetSessionsInTimePeriod")
    fun getSessionsInTimePeriod(startTime: Long, endTime: Long, context: Context): List<SessionWithApps> {
        return runBlocking {
            return@runBlocking withContext(Dispatchers.IO) {
                try {
                    SessionCoroutines.getSessionsInTimePeriod(startTime, endTime, context)
                } catch (e: Exception) {
                    Sentry.captureException(e)
                    ArrayList() // return empty
                }
            }
        }
    }

    @AddTrace(name = "databaseGetAllSessions")
    fun getAllSessions(context: Context): List<SessionWithApps> {
        return runBlocking {
            try {
                SessionCoroutines.getAllSessions(context)
            } catch (e: Exception) {
                ArrayList()
            }
        }
    }

    @AddTrace(name = "databaseSaveApps")
    fun saveApps(apps: List<App>, context: Context): List<Long> {
        return runBlocking {
            try {
                AppCoroutines.saveApps(apps, context)
            } catch (e: Exception) {
                Sentry.captureException(e)
                ArrayList()
            }
        }
    }

    companion object {
        @JvmStatic
        @Volatile
        var instance: DatabaseAPI? = null
            get() {
                if (field == null) {
                    synchronized(DatabaseAPI::class.java) { field = DatabaseAPI() }
                }
                return field
            }
            private set
    }
}