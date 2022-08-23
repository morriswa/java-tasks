package org.morriswa.taskapp.enums;

public enum TaskStatus {
    BACKLOG(-1),
    // IN PROGRESS MARKERS
    NEW(0),
    STARTED(1),
    IN_PROGRESS(2),
    REVIEW(3),
    // COMPLETE MARKERS
    COMPLETED(4),
    TURNED_IN(5),
    CLOSED(6),
    EXPIRED(7);

    public final int progress;

    TaskStatus(int progress) {
        this.progress = progress;
    }
}
