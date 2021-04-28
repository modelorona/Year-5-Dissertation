package com.anguel.dissertation.persistence.entity.app;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(tableName = "App")
public class App {

    @PrimaryKey(autoGenerate = true)
    public long appId;

    public String name;

    public String packageName;

    public String appCategory;

    public long lastTimeUsed;

    public long totalTimeInForeground;

    public long sessionIdFK;

}
