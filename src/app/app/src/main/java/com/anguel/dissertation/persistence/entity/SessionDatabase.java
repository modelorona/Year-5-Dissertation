package com.anguel.dissertation.persistence.entity;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.anguel.dissertation.R;
import com.anguel.dissertation.persistence.entity.app.App;
import com.anguel.dissertation.persistence.entity.app.AppDao;
import com.anguel.dissertation.persistence.entity.session.Session;
import com.anguel.dissertation.persistence.entity.session.SessionDao;

@Database(entities = {App.class, Session.class}, version = 2)
public abstract class SessionDatabase extends RoomDatabase {

    public abstract AppDao appDao();

    public abstract SessionDao sessionDao();

    private static volatile SessionDatabase INSTANCE;

    public static SessionDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SessionDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), SessionDatabase.class, context.getString(R.string.session_db))
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
