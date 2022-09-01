package org.morriswa.taskapp.entity;

import lombok.*;

import javax.persistence.*;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor @NoArgsConstructor
@Entity @Table(name = "Planners")
@Getter @Setter @Builder
public class Planner {
    @Id @Column(name = "planner_id")
    @SequenceGenerator(name = "planner_seq")
    @GeneratedValue(generator = "planner_seq", strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id",referencedColumnName = "user_id",nullable = false)
    private CustomAuth0User user;

    private String name;
    private GregorianCalendar creationDate = new GregorianCalendar();
    private GregorianCalendar startDate;
    private GregorianCalendar finishDate;
    private String goal = "";

    @OneToMany(orphanRemoval = true,cascade = CascadeType.ALL,mappedBy = "planner",fetch = FetchType.LAZY)
    private Set<Task> tasks = new HashSet<>();

    public void addTask(Task newTask) {
        this.tasks.add(newTask);
        newTask.setPlanner(this);
    }

    public void deleteTask(Task toDel) {
        this.tasks.remove(toDel);
        toDel.setPlanner(null);
    }
}
