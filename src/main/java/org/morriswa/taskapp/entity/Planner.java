package org.morriswa.taskapp.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

@Data @AllArgsConstructor @NoArgsConstructor
public class Planner implements Serializable {
    private String name;
    private ArrayList<Task> tasks = new ArrayList<>();

    public Planner addTask(Task newTask) {
        this.tasks.add(newTask);
        this.tasks.sort(Task::compareTo);
        return this;
    }

    public Planner updateTaskStatus(int indexToCheck,TaskStatus status) {
        if (this.tasks.get(indexToCheck) == null) {
            throw new NullPointerException("No task at that index");
        }
        this.tasks.get(indexToCheck).setStatus(status);
        this.tasks.sort(Task::compareTo);
        return this;
    }

    public Planner deleteTask(int task_index) {
        if (this.tasks.get(task_index) == null) {
            throw new NullPointerException("No task at that index");
        }
        this.tasks.remove(task_index);
        this.tasks.sort(Task::compareTo);
        return this;
    }
}
