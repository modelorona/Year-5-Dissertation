package com.anguel.dissertation.persistence.coroutines.session

import android.content.Context
import com.anguel.dissertation.persistence.entity.SessionDatabase
import com.anguel.dissertation.persistence.entity.SessionWithApps
import com.anguel.dissertation.persistence.entity.session.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SessionCoroutines {

    companion object {
        @JvmStatic
        suspend fun saveSession(session: Session, context: Context): Long {
            return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                SessionDatabase.getInstance(context).sessionDao().insertSession(session)
            }
        }

        @JvmStatic
        suspend fun getSessionsInTimePeriod(startTime: Long, endTime: Long, context: Context): List<SessionWithApps> {
            return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                SessionDatabase.getInstance(context).sessionDao().getSessionsInTimeframe(startTime, endTime)
            }
        }

        @JvmStatic
        suspend fun getAllSessions(context: Context): List<SessionWithApps> {
            return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                SessionDatabase.getInstance(context).sessionDao().sessions
            }
        }
    }
}