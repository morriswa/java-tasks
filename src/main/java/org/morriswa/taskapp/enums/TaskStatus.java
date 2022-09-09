package org.morriswa.taskapp.enums;

public enum TaskStatus {
    BACKLOG(-1),
    PAST_DUE(-1),
    // IN PROGRESS MARKERS
    NEW(0),
    STARTED(0),
    IN_PROGRESS(0),
    // REVIEW
    REVIEW(1),
    // COMPLETE MARKERS
    COMPLETED(2),
    TURNED_IN(2),
    CLOSED(2),
    EXPIRED(3);

    public final int progress;

    TaskStatus(int progress) {
        this.progress = progress;
    }
}
