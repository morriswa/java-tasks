package org.morriswa.taskapp.service;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.apache.commons.lang3.StringUtils;
import org.morriswa.common.model.BadRequestException;
import org.morriswa.taskapp.entity.Planner;
import org.morriswa.taskapp.entity.Task;
import org.morriswa.taskapp.model.*;
import org.morriswa.taskapp.repo.PlannerRepo;
import org.morriswa.taskapp.repo.TaskRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.MissingRequestValueException;

import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Set;

import static org.morriswa.taskapp.exception.TaskApiExceptionSupply.noPlannerFoundException;
import static org.morriswa.taskapp.exception.TaskApiExceptionSupply.noTaskFoundException;

@Service
public class TaskServiceImpl implements TaskService {
    private final PlannerRepo plannerRepo;
    private final TaskRepo taskRepo;
    private final GregorianCalendar gc = new GregorianCalendar();
    private final Validator validator;

    // PLANNER REQUEST KEYS
    public final static String PLANNER_ID_REQUEST_KEY = "planner-id";
    public final static String PLANNER_NAME_REQUEST_KEY = "planner-name";
    public final static String PLANNER_GOAL_REQUEST_KEY = "planner-goal";

    // PROFILE REQUEST KEYS
    public final static String NAME_FIRST_REQUEST_KEY = "name-first";
    public final static String NAME_MIDDLE_REQUEST_KEY = "name-middle";
    public final static String NAME_LAST_REQUEST_KEY = "name-last";
    public final static String NAME_DISPLAY_REQUEST_KEY = "name-display";
    public final static String PRONOUNS_REQUEST_KEY = "pronouns";

    // TASK REQUEST KEYS
    public final static String TASK_ID_REQUEST_KEY = "task-id";
    public final static String TASK_NAME_REQUEST_KEY = "task-name";
    public final static String TASK_STATUS_REQUEST_KEY = "task-status";
    public final static String TASK_TYPE_REQUEST_KEY = "task-type";
    public final static String TASK_DETAILS_REQUEST_KEY = "task-details";
    public final static String TASK_CATEGORY_REQUEST_KEY = "task-category";
    public final static String START_TIME_REQUEST_KEY ="start-greg";
    public final static String DUE_TIME_REQUEST_KEY ="due-greg";
    public final static String FINISH_TIME_REQUEST_KEY ="finish-greg";


    @Autowired
    public TaskServiceImpl(PlannerRepo pr,
                           TaskRepo tr,
                           Validator validator) {
        this.plannerRepo = pr;
        this.taskRepo = tr;
        this.validator = validator;
    }


    // Helpers
    private Object retrieveKeyOrThrow(String key, Map<String,Object> request_map) throws MissingRequestValueException {
        if (!request_map.containsKey(key)) {
            throw new MissingRequestValueException("Request failed due to missing required params: " + key);
        }
        return request_map.get(key);
    }


    // Planner Actions
    @Override
    public Planner getPlanner(String onlineId, Long plannerId) throws Exception {
        return plannerRepo.findByIdAndOnlineId(plannerId, onlineId)
                .orElseThrow(noPlannerFoundException(plannerId,onlineId));
    }

    @Override
    public PlannerResponse getPlannerWithTasks(String onlineId, Long plannerId) throws BadRequestException {
        Planner planner = plannerRepo.findByIdAndOnlineId(plannerId, onlineId)
                .orElseThrow(noPlannerFoundException(plannerId,onlineId));
        Set<Task> tasks = taskRepo.findAllByPlannerIdAndOnlineId(plannerId,onlineId);
        return PlannerResponse.builder()
                .plannerInfo(planner)
                .tasks(tasks)
                .build();
    }

    @Override
    public Set<Planner> getAllPlanners(String onlineId) {
        return plannerRepo.findAllByOnlineId(onlineId);
    }

    @Override
    public Set<Planner> plannerAdd(@Valid PlannerRequest newPlannerRequest) throws Exception
    {
        var requestErrors = validator.validate(newPlannerRequest);

        if (plannerRepo.existsByOnlineIdAndName(
                newPlannerRequest.getOnlineId(),
                newPlannerRequest.getName()))
            throw new BadRequestException(
                    String.format(
                            "Planner with name %s already exists for user %s",
                            newPlannerRequest.getName(),
                            newPlannerRequest.getOnlineId()));

        var plannerBuilder = Planner.builder()
                .onlineId(newPlannerRequest.getOnlineId())
                .name(newPlannerRequest.getName())
                .creationDate(new GregorianCalendar());

        if (!StringUtils.isBlank(newPlannerRequest.getGoal())) {
            plannerBuilder.goal(newPlannerRequest.getGoal());
        }

        if (newPlannerRequest.getStartDate() != null) {
            gc.setTimeInMillis(newPlannerRequest.getStartDate());
            plannerBuilder.startDate((GregorianCalendar) gc.clone());
        }

        if (newPlannerRequest.getFinishDate() != null) {
            gc.setTimeInMillis(newPlannerRequest.getFinishDate());
            plannerBuilder.startDate((GregorianCalendar) gc.clone());
        }

        var newPlanner = plannerBuilder.build();
        var newPlannerViolations = validator.validate(newPlanner);
        if (!newPlannerViolations.isEmpty()) throw new ConstraintViolationException(newPlannerViolations);

        plannerRepo.save(newPlanner);
        return plannerRepo.findAllByOnlineId(newPlannerRequest.getOnlineId());
    }

    @Override
    public Set<Planner> plannerDel(@Valid PlannerRequest request)
            throws Exception
    {
        Planner planner = plannerRepo.findByIdAndOnlineId(request.getPlannerId(), request.getOnlineId())
                .orElseThrow(noPlannerFoundException(request.getPlannerId(), request.getOnlineId()));

        plannerRepo.delete(planner);

        return plannerRepo.findAllByOnlineId(request.getOnlineId());
    }

    @Override
    public Planner updatePlanner(@Valid PlannerRequest request) throws BadRequestException {
        Planner planner = plannerRepo.findByIdAndOnlineId(request.getPlannerId(), request.getOnlineId())
                .orElseThrow(noPlannerFoundException(request.getPlannerId(), request.getOnlineId()));

        if (!StringUtils.isBlank(request.getName())) {
            planner.setName(request.getName());
        }

        if (!StringUtils.isBlank(request.getGoal())) {
            planner.setGoal(planner.getGoal());
        }

        if (request.getStartDate() != null) {
            gc.setTimeInMillis(request.getStartDate());
            planner.setStartDate((GregorianCalendar) gc.clone());
        }

        if (request.getFinishDate() != null) {
            gc.setTimeInMillis(request.getFinishDate());
            planner.setFinishDate((GregorianCalendar) gc.clone());
        }

        var violations = validator.validate(planner);
        if (!violations.isEmpty()) throw new ConstraintViolationException(violations);
        plannerRepo.save(planner);
        return planner;
    }


    // Task Actions
    @Override
    public Set<Task> taskAdd(@Valid TaskRequest request) throws Exception {
        var taskBuilder = Task.builder()
                .onlineId(request.getOnlineId())
                .plannerId(request.getPlannerId())
                .title(request.getTitle())
                .creationDate(new GregorianCalendar());

        if (request.getStartDate() != null) {
            gc.setTimeInMillis(request.getStartDate());
            taskBuilder.startDate((GregorianCalendar) gc.clone());
        }

        if (request.getDueDate() != null) {
            gc.setTimeInMillis(request.getDueDate());
            taskBuilder.dueDate((GregorianCalendar) gc.clone());
        }

        if (request.getStatus() != null) {
            taskBuilder.status(request.getStatus());

            if (request.getStatus().progress >= TaskStatus.COMPLETED.progress) {
                taskBuilder.completedDate(new GregorianCalendar());
            } else {
                taskBuilder.completedDate(null);
            }
        } else {
            taskBuilder.status(TaskStatus.NEW);
        }

        if (request.getFinishDate() != null) {
            gc.setTimeInMillis(request.getFinishDate());
            taskBuilder.completedDate((GregorianCalendar) gc.clone());
        }

        if (request.getType() != null) {
            taskBuilder.type(request.getType());
        } else {
            taskBuilder.type(TaskType.TASK);
        }

        if (!StringUtils.isBlank(request.getDetails())) {
            taskBuilder.description(request.getDetails());
        } else {
            taskBuilder.description("");
        }


        if (!StringUtils.isBlank(request.getCategory())) {
            taskBuilder.category(request.getCategory());
        } else {
            taskBuilder.category("");
        }

        var newTask = taskBuilder.build();
        var newTaskViolations = validator.validate(newTask);
        if (!newTaskViolations.isEmpty()) throw new ConstraintViolationException(newTaskViolations);

        taskRepo.save(newTask);
        return taskRepo.findAllByPlannerIdAndOnlineId(request.getPlannerId(), request.getOnlineId());
    }

    @Override
    public void taskDel(@Valid TaskRequest deleteTaskRequest) throws Exception {
        Task taskToDelete = taskRepo.findByIdAndOnlineId(deleteTaskRequest.getTaskId(),deleteTaskRequest.getOnlineId())
                .orElseThrow(noTaskFoundException(deleteTaskRequest.getTaskId(),deleteTaskRequest.getOnlineId()));

        taskRepo.delete(taskToDelete);
    }

    @Override
    public void updateTask(@Valid TaskRequest request) throws Exception {
        Task toUpdate = taskRepo.findByIdAndOnlineId(request.getTaskId(), request.getOnlineId())
                .orElseThrow(noTaskFoundException(request.getTaskId(), request.getOnlineId()));



        if (request.getStartDate() != null) {
            gc.setTimeInMillis(request.getStartDate());
            toUpdate.setStartDate((GregorianCalendar) gc.clone());
        }

        if (request.getDueDate() != null) {
            gc.setTimeInMillis(request.getDueDate());
            toUpdate.setDueDate((GregorianCalendar) gc.clone());
        }

        if (request.getStatus() != null) {
            toUpdate.setStatus(request.getStatus());

            if (toUpdate.getStatus().progress >= TaskStatus.COMPLETED.progress) {
                toUpdate.setCompletedDate(new GregorianCalendar());
            } else {
                toUpdate.setCompletedDate(null);
            }
        }

        if (request.getFinishDate() != null) {
            gc.setTimeInMillis(request.getFinishDate());
            toUpdate.setCompletedDate((GregorianCalendar) gc.clone());
        }

        if (!StringUtils.isBlank(request.getTitle())) {
            toUpdate.setTitle(request.getTitle());
        }

        if (request.getType() != null) {
            toUpdate.setType(request.getType());
        }

        if (!StringUtils.isBlank(request.getDetails())) {
            toUpdate.setDescription(request.getDetails());
        }

        if (!StringUtils.isBlank(request.getCategory())) {
            toUpdate.setCategory(request.getCategory());
        }

        var taskViolations = validator.validate(toUpdate);
        if (!taskViolations.isEmpty()) throw new ConstraintViolationException(taskViolations);
        taskRepo.save(toUpdate);
    }

}
