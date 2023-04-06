package org.morriswa.taskapp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.morriswa.taskapp.entity.Planner;
import org.morriswa.taskapp.entity.Task;

import java.util.Set;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class PlannerResponse {
    Planner plannerInfo;
    Set<Task> tasks;
}
