package com.anguel.dissertation.persistence.entity.app;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AppDao {
    @Query("SELECT * FROM App")
    List<App> getAll();


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertApps(List<App> apps);

}
