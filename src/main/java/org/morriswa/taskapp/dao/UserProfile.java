package org.morriswa.taskapp.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.morriswa.taskapp.entity.Planner;
import org.morriswa.taskapp.entity.Task;
import org.morriswa.taskapp.entity.TaskStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashMap;

@Entity @Table(name = "profiles")
@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class UserProfile implements Serializable {
    @OneToOne(optional = false)
    @JoinColumn(name = "user_id",referencedColumnName = "online_id")
    private CustomAuth0User user;

    @Id @Column(name = "db_id")
    @SequenceGenerator(name = "db_id_seq_profile") @GeneratedValue(strategy = GenerationType.AUTO,generator = "db_id_seq_profile")
    private Long id;

    private String nameFirst;
    private String nameMiddle;
    private String nameLast;
    private String displayName;
    private String pronouns;
    private HashMap<String, Planner> planners = new HashMap<>();

    public UserProfile(CustomAuth0User user) {
        this.user = user;
        this.nameFirst = "";
        this.nameMiddle = "";
        this.nameLast = "";
        this.displayName = "";
        this.pronouns = "";
    }

    public HashMap<String,Planner> addPlanner(Planner newPlanner) {
        this.planners.put(newPlanner.getName(),newPlanner);
        return this.planners;
    }

    public HashMap<String,Planner> updatePlanner(String plannerToUpdate, Task newTask) {
        if (!this.planners.containsKey(plannerToUpdate)) {
            throw new NullPointerException("Cannot update planner that doesn't exist");
        }

        this.planners.replace(
                plannerToUpdate,
                this.planners.get(plannerToUpdate).addTask(newTask));
        return this.planners;
    }

    public HashMap<String,Planner> updateTaskInPlanner(int indexToComplete, TaskStatus status,String plannerToUpdate) {
        this.planners.replace(
                plannerToUpdate,
                this.planners.get(plannerToUpdate).updateTaskStatus(indexToComplete,status));
        return this.planners;
    }

    public HashMap<String,Planner> deleteTaskInPlanner(int task_index, String planner_name) {
        this.planners.replace(
                planner_name,
                this.planners.get(planner_name).deleteTask(task_index));
        return this.planners;
    }
}
