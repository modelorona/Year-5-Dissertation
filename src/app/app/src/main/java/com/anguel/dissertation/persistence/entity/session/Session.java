package com.anguel.dissertation.persistence.entity.session;

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
@Entity(tableName = "Session")
public class Session {

    @PrimaryKey(autoGenerate = true)
    public long sessionId;

    public long sessionStart;
    public long sessionEnd;

    // for when data is shared, to keep track if the session is from a socially anxious user or not
    public boolean anxious;
}


