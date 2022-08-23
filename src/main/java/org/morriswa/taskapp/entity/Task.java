package org.morriswa.taskapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.morriswa.taskapp.enums.TaskStatus;
import org.morriswa.taskapp.enums.TaskType;

import javax.persistence.*;
import java.util.GregorianCalendar;

@Entity @Table(name = "Tasks")
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter @Builder
public class Task implements Comparable<Task> {
    @Id
    @SequenceGenerator(name = "task_seq")
    @GeneratedValue(generator = "task_seq", strategy = GenerationType.AUTO)
    @Column(name = "task_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "planner_id", referencedColumnName = "planner_id")
    @JsonIgnore
    private Planner planner;
    private String title;
    private GregorianCalendar creationDate = new GregorianCalendar();
    private GregorianCalendar startDate;
    private GregorianCalendar dueDate;
    private GregorianCalendar completedDate;

    private String category = "";
    private String description = "";
    private TaskStatus status = TaskStatus.NEW;
    private TaskType type = TaskType.TASK;
//
//    public Task(String title,
//                GregorianCalendar startDate,
//                GregorianCalendar finishDate) {
//        this.title = title;
//        this.startDate = startDate;
//        this.dueDate = finishDate;
//    }
//
//    public Task(String title,
//                GregorianCalendar startDate,
//                GregorianCalendar finishDate,
//                TaskType type,
//                String description) {
//        this.title = title;
//        this.startDate = startDate;
//        this.dueDate = finishDate;
//        this.type = type;
//        this.description = description;
//    }

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
