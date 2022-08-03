package org.morriswa.taskapp.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.OptimisticLock;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

@Data
public class Task implements Serializable,Comparable<Task> {
    private String title;
    private GregorianCalendar startDate;
    private GregorianCalendar finishDate;
    private String description;
    private TaskStatus status = TaskStatus.NEW;

    public Task(String title,
                GregorianCalendar startDate,
                GregorianCalendar finishDate) {
        this.title = title;
        this.startDate = startDate;
        this.finishDate = finishDate;
        this.description = "";
    }

    public Task(String title,
                GregorianCalendar startDate,
                GregorianCalendar finishDate,
                String description) {
        this.title = title;
        this.startDate = startDate;
        this.finishDate = finishDate;
        this.description = description;
    }

    @Override
    public int compareTo(Task o) {
        if (this.status.equals(o.status)) {
            return this.getStartDate().compareTo(o.getStartDate());
        }

        if (this.status.progress < o.status.progress) {
            return -1;
        } else {
            return 1;
        }
    }
}
