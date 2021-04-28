package com.anguel.dissertation.persistence.entity.session;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.anguel.dissertation.persistence.entity.SessionWithApps;

import java.util.List;

@Dao
public interface SessionDao {

    @Transaction
    @Query("SELECT * FROM Session")
    List<SessionWithApps> getSessions();

    @Transaction
    @Query("SELECT * FROM Session where sessionStart >= :sessionStart AND sessionEnd <= :sessionEnd")
    List<SessionWithApps> getSessionsInTimeframe(long sessionStart, long sessionEnd);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSession(Session session);

}
