package org.morriswa.taskapp.model;

public enum TaskType {
    TASK(Boolean.FALSE),
    ASSIGNMENT(Boolean.FALSE),
    STUDY(Boolean.FALSE),
    QUIZ(Boolean.FALSE),
    TEST(Boolean.FALSE),
    GRADED_ASSIGNMENT(Boolean.TRUE),
    PROJECT(Boolean.TRUE),
    TAKE_HOME_QUIZ(Boolean.TRUE),
    TAKE_HOME_TEST(Boolean.TRUE);

    public final Boolean graded;

    TaskType(Boolean graded) {
        this.graded = graded;
    }
}
