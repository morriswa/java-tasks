package org.morriswa.taskapp.enums;

public enum TaskStatus {
    BACKLOG(-1),
    // IN PROGRESS MARKERS
    NEW(0),
    STARTED(1),
    IN_PROGRESS(2),
    PAST_DUE(3),
    REVIEW(4),
    // COMPLETE MARKERS
    COMPLETED(5),
    TURNED_IN(6),
    CLOSED(7),
    EXPIRED(8);

    public final int progress;

    TaskStatus(int progress) {
        this.progress = progress;
    }
}
