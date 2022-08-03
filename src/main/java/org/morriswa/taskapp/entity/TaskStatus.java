package org.morriswa.taskapp.entity;

public enum TaskStatus {
    BACKLOG(-1),
    NEW(0),
    STARTED(1),
    IN_PROGRESS(2),
    REVIEW(3),
    COMPLETED(4),
    TURNED_IN(5),
    CLOSED(6);

    public final int progress;

    TaskStatus(int progress) {
        this.progress = progress;
    }
}
