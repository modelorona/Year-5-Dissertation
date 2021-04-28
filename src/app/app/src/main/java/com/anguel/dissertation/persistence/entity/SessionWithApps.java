package com.anguel.dissertation.persistence.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.anguel.dissertation.persistence.entity.app.App;
import com.anguel.dissertation.persistence.entity.session.Session;

import java.util.List;

import lombok.Getter;

@Getter
public class SessionWithApps {
    @Embedded
    public Session session;

    @Relation(
            parentColumn = "sessionId",
            entityColumn = "sessionIdFK"
    )
    public List<App> sessionApps;
}
