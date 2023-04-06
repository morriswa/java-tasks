package org.morriswa.taskapp.service;

import jakarta.validation.Valid;
import org.morriswa.common.model.BadRequestException;
import org.morriswa.taskapp.entity.Planner;
import org.morriswa.taskapp.entity.Task;
import org.morriswa.taskapp.model.PlannerRequest;
import org.morriswa.taskapp.model.PlannerResponse;
import org.morriswa.taskapp.model.TaskRequest;
import org.springframework.web.bind.MissingRequestValueException;

import java.util.Set;

public interface TaskService {
    // Planner Actions
    Planner getPlanner(String onlineId, Long plannerId) throws Exception;
    PlannerResponse getPlannerWithTasks(String onlineId, Long plannerId) throws BadRequestException;

    Set<Planner> getAllPlanners(String onlineId);
    Set<Planner> plannerAdd(@Valid PlannerRequest newPlannerRequest) throws Exception;
    Set<Planner> plannerDel(@Valid PlannerRequest request) throws Exception;
    Planner updatePlanner(@Valid PlannerRequest request) throws MissingRequestValueException, BadRequestException;

    // Task Actions
    Set<Task> taskAdd(@Valid TaskRequest request) throws Exception;
    void taskDel(@Valid TaskRequest deleteTaskRequest) throws Exception;
    void updateTask(@Valid TaskRequest request) throws Exception;
}
