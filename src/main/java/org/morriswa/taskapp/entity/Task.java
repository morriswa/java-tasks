package org.morriswa.taskapp.entity;

import lombok.*;
import org.morriswa.taskapp.model.TaskStatus;
import org.morriswa.taskapp.model.TaskType;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.GregorianCalendar;

@Entity @Table(name = "task")
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter @Builder
public class Task implements Comparable<Task> {
    @Id @Column(name = "task_id")
    @SequenceGenerator(name = "task_seq")
    @GeneratedValue(generator = "task_seq", strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank
    @Column(name = "online_id",nullable = false,updatable = false)
    private String onlineId;

    @Column(name = "planner_id")
    private Long plannerId;

    @NotBlank
    private String title;

    @NotNull
    private GregorianCalendar creationDate = new GregorianCalendar();

    private GregorianCalendar startDate;
    private GregorianCalendar dueDate;
    private GregorianCalendar completedDate;

    private String category = "";
    private String description = "";
    private TaskStatus status = TaskStatus.NEW;
    private TaskType type = TaskType.TASK;

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
