package org.morriswa.taskapp.service;

import org.apache.commons.lang3.StringUtils;
import org.morriswa.taskapp.entity.CustomAuth0User;
import org.morriswa.taskapp.entity.Planner;
import org.morriswa.taskapp.entity.Task;
import org.morriswa.taskapp.entity.UserProfile;
import org.morriswa.taskapp.enums.TaskStatus;
import org.morriswa.taskapp.enums.TaskType;
import org.morriswa.taskapp.exception.AuthenticationFailedException;
import org.morriswa.taskapp.exception.BadRequestException;
import org.morriswa.taskapp.exception.RequestFailedException;
import org.morriswa.taskapp.repo.PlannerRepo;
import org.morriswa.taskapp.repo.TaskRepo;
import org.morriswa.taskapp.repo.UserProfileRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.MissingRequestValueException;

import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.morriswa.taskapp.exception.CustomExceptionSupply.*;

@Service
public class TaskService {
    private final UserProfileRepo profileRepo;
    private final PlannerRepo plannerRepo;
    private final TaskRepo taskRepo;
    private final GregorianCalendar gc = new GregorianCalendar();

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
    public TaskService(UserProfileRepo p,PlannerRepo pr,TaskRepo tr) {
        this.profileRepo = p;
        this.plannerRepo = pr;
        this.taskRepo = tr;
    }


    // Helpers
    private Object retrieveKeyOrThrow(String key, Map<String,Object> request_map) throws MissingRequestValueException {
        if (!request_map.containsKey(key)) {
            throw new MissingRequestValueException("Request failed due to missing required params: " + key);
        }
        return request_map.get(key);
    }


    // Profile Actions
    public UserProfile profileGet(CustomAuth0User authenticatedUser)
            throws AuthenticationFailedException
    {
        return profileRepo.findByUser(authenticatedUser)
                .orElseThrow(unableToAccessPlannerError(authenticatedUser.getOnlineId()));
    }

    public UserProfile profileUpdate(CustomAuth0User user, Map<String,Object> newProfileRequest)
            throws AuthenticationFailedException
    {
        UserProfile profile = profileRepo.findByUser(user)
                .orElseThrow(unableToAccessPlannerError(user.getOnlineId()));

        UserProfile updatedProfile = UserProfile.builder()
                .id(profile.getId())
                .user(profile.getUser())
                .nameFirst((String) newProfileRequest.getOrDefault(NAME_FIRST_REQUEST_KEY,profile.getNameFirst()))
                .nameMiddle((String) newProfileRequest.getOrDefault(NAME_MIDDLE_REQUEST_KEY,profile.getNameMiddle()))
                .nameLast((String) newProfileRequest.getOrDefault(NAME_LAST_REQUEST_KEY,profile.getNameLast()))
                .displayName((String) newProfileRequest.getOrDefault(NAME_DISPLAY_REQUEST_KEY,profile.getDisplayName()))
                .pronouns((String) newProfileRequest.getOrDefault(PRONOUNS_REQUEST_KEY,profile.getPronouns()))
                        .build();

        profileRepo.save(updatedProfile);
        return updatedProfile;
    }


    // Planner Actions
    public Planner getPlanner(CustomAuth0User authenticatedUser, Map<String, Object> request)
            throws Exception
    {
        final Long PLANNER_ID = Integer.toUnsignedLong((int) retrieveKeyOrThrow(PLANNER_ID_REQUEST_KEY, request));

        return plannerRepo.findByUserAndId(authenticatedUser,PLANNER_ID)
                .orElseThrow(noPlannerFoundException(PLANNER_ID,authenticatedUser));
    }

    public Set<Planner> getAllPlanners(CustomAuth0User authenticatedUser) {
        return plannerRepo.findAllByUser(authenticatedUser);
    }

    public Set<Planner> plannerAdd(CustomAuth0User user, Map<String,Object> newPlannerRequest)
            throws Exception
    {
        final String PLANNER_NAME = (String) retrieveKeyOrThrow(PLANNER_NAME_REQUEST_KEY,newPlannerRequest);

        if (plannerRepo.existsByUserAndName(user,PLANNER_NAME)) {
            throw new RequestFailedException(
                    String.format("Planner with name %s already exists for user %s",PLANNER_NAME,user.getOnlineId()));
        }

        if (StringUtils.isBlank(PLANNER_NAME)) {
            throw new BadRequestException("Planner cannot have no name");
        }

        Planner.PlannerBuilder newPlanner = Planner.builder()
                .user(user).name(PLANNER_NAME);

        if (newPlannerRequest.containsKey(PLANNER_GOAL_REQUEST_KEY)) {
            newPlanner.goal((String) newPlannerRequest.get(PLANNER_GOAL_REQUEST_KEY));
        }

        if (newPlannerRequest.containsKey(START_TIME_REQUEST_KEY)) {
            gc.setTimeInMillis((Long) newPlannerRequest.get(START_TIME_REQUEST_KEY));
            newPlanner.startDate((GregorianCalendar) gc.clone());
        }

        if (newPlannerRequest.containsKey(FINISH_TIME_REQUEST_KEY)) {
            gc.setTimeInMillis((Long) newPlannerRequest.get(FINISH_TIME_REQUEST_KEY));
            newPlanner.finishDate((GregorianCalendar) gc.clone());
        }

        plannerRepo.save(newPlanner.build());
        return plannerRepo.findAllByUser(user);
    }

    public Set<Planner> plannerDel(CustomAuth0User user, Map<String,Object> request)
            throws Exception
    {
        final Long PLANNER_ID = Integer.toUnsignedLong((int)
            retrieveKeyOrThrow(PLANNER_ID_REQUEST_KEY, request));

        Optional<Planner> planner = plannerRepo.findByUserAndId(user,PLANNER_ID);

        plannerRepo.delete(planner
                .orElseThrow(noPlannerFoundException(PLANNER_ID,user)));

        return plannerRepo.findAllByUser(user);
    }

    public Planner updatePlanner(CustomAuth0User user,Map<String,Object> request)
            throws MissingRequestValueException, BadRequestException {
        final Long PLANNER_ID = Integer.toUnsignedLong((int)
            retrieveKeyOrThrow(PLANNER_ID_REQUEST_KEY,request));

        Planner planner = plannerRepo.findByUserAndId(user,PLANNER_ID)
                .orElseThrow(noPlannerFoundException(PLANNER_ID,user));

        if (request.containsKey(PLANNER_NAME_REQUEST_KEY)) {
            planner.setName(request.get(PLANNER_NAME_REQUEST_KEY).toString());
        }

        if (request.containsKey(PLANNER_GOAL_REQUEST_KEY)) {
            planner.setGoal((String) request.get(PLANNER_GOAL_REQUEST_KEY));
        }

        if (request.containsKey(START_TIME_REQUEST_KEY)) {
            gc.setTimeInMillis((Long) request.get(START_TIME_REQUEST_KEY));
            planner.setStartDate((GregorianCalendar) gc.clone());
        }

        if (request.containsKey(FINISH_TIME_REQUEST_KEY)) {
            gc.setTimeInMillis((Long) request.get(FINISH_TIME_REQUEST_KEY));
            planner.setFinishDate((GregorianCalendar) gc.clone());
        }

        plannerRepo.save(planner);
        return planner;
    }


    // Task Actions
    public Planner taskAdd(CustomAuth0User user, Map<String,Object> request)
            throws Exception
    {
        final Long PLANNER_ID = Integer.toUnsignedLong((int) retrieveKeyOrThrow(PLANNER_ID_REQUEST_KEY, request));
        final String TASK_NAME = (String) retrieveKeyOrThrow(TASK_NAME_REQUEST_KEY, request);

        if (StringUtils.isBlank(TASK_NAME)) {
            throw new BadRequestException("Task cannot have no name");
        }

        Planner planner = plannerRepo.findByUserAndId(user,PLANNER_ID)
                .orElseThrow(noPlannerFoundException(PLANNER_ID,user));
        Task.TaskBuilder newTask = Task.builder()
                .planner(planner).title(TASK_NAME).creationDate(new GregorianCalendar());

        if (request.containsKey(START_TIME_REQUEST_KEY)) {
            gc.setTimeInMillis((Long) request.get(START_TIME_REQUEST_KEY));
            newTask.startDate((GregorianCalendar) gc.clone());
        }

        if (request.containsKey(DUE_TIME_REQUEST_KEY) ) {
            gc.setTimeInMillis((long) request.get(DUE_TIME_REQUEST_KEY));
            newTask.dueDate((GregorianCalendar) gc.clone());
        }

        if (request.containsKey(TASK_STATUS_REQUEST_KEY)) {
            newTask.status(TaskStatus.valueOf(request.get(TASK_STATUS_REQUEST_KEY).toString()));

            if ((TaskStatus.valueOf((String) request.get
                    (TASK_STATUS_REQUEST_KEY))).progress >= TaskStatus.COMPLETED.progress) {
                newTask.completedDate(new GregorianCalendar());
            } else {
                newTask.completedDate(null);
            }
        } else {
            newTask.status(TaskStatus.NEW);
        }

        if (request.containsKey(FINISH_TIME_REQUEST_KEY)) {
            gc.setTimeInMillis((long) request.get(FINISH_TIME_REQUEST_KEY));
            newTask.completedDate((GregorianCalendar) gc.clone());
        }

        if (request.containsKey(TASK_TYPE_REQUEST_KEY)) {
            newTask.type(TaskType.valueOf(request.get(TASK_TYPE_REQUEST_KEY).toString()));
        } else {
            newTask.type(TaskType.TASK);
        }

        if (request.containsKey(TASK_DETAILS_REQUEST_KEY)) {
            newTask.description(request.get(TASK_DETAILS_REQUEST_KEY).toString());
        } else {
            newTask.description("");
        }


        if (request.containsKey(TASK_CATEGORY_REQUEST_KEY)) {
            newTask.category(request.get(TASK_CATEGORY_REQUEST_KEY).toString());
        } else {
            newTask.category("");
        }

        planner.addTask(newTask.build());
        plannerRepo.save(planner);
        return planner;
    }

    public Planner taskDel(CustomAuth0User user, Map<String,Object> updatePlannerRequest)
            throws Exception
    {
        final Long PLANNER_ID = Integer.toUnsignedLong((int)
                retrieveKeyOrThrow(PLANNER_ID_REQUEST_KEY, updatePlannerRequest));
        final Long TASK_ID = Integer.toUnsignedLong((int)
                retrieveKeyOrThrow(TASK_ID_REQUEST_KEY, updatePlannerRequest));

        Planner planner = plannerRepo.findByUserAndId(user,PLANNER_ID)
                .orElseThrow(noPlannerFoundException(PLANNER_ID,user));
        Task taskToDelete = taskRepo.findByPlannerAndId(planner,TASK_ID)
                .orElseThrow(noTaskFoundException(TASK_ID,planner));

        planner.deleteTask(taskToDelete);
        plannerRepo.save(planner);
        return planner;
    }

    public Planner updateTask(CustomAuth0User user, Map<String,Object> request)
            throws Exception
    {
        final Long PLANNER_ID = Integer.toUnsignedLong((int) retrieveKeyOrThrow(PLANNER_ID_REQUEST_KEY, request));
        final Long TASK_ID = Integer.toUnsignedLong((int) retrieveKeyOrThrow(TASK_ID_REQUEST_KEY, request));

        Planner planner = plannerRepo.findByUserAndId(user,PLANNER_ID)
                .orElseThrow(noPlannerFoundException(PLANNER_ID,user));
        Task toUpdate = taskRepo.findByPlannerAndId(planner,TASK_ID)
                .orElseThrow(noTaskFoundException(TASK_ID,planner));
        planner.deleteTask(toUpdate);

        if (request.containsKey(START_TIME_REQUEST_KEY)) {
            gc.setTimeInMillis((Long) request.get(START_TIME_REQUEST_KEY));
            toUpdate.setStartDate((GregorianCalendar) gc.clone());
        }

        if (request.containsKey(DUE_TIME_REQUEST_KEY)) {
            gc.setTimeInMillis((long) request.get(DUE_TIME_REQUEST_KEY));
            toUpdate.setDueDate((GregorianCalendar) gc.clone());
        }

        if (request.containsKey(TASK_STATUS_REQUEST_KEY)) {
            toUpdate.setStatus(TaskStatus.valueOf(request.get(TASK_STATUS_REQUEST_KEY).toString()));

            if (toUpdate.getStatus().progress >= TaskStatus.COMPLETED.progress) {
                toUpdate.setCompletedDate(new GregorianCalendar());
            } else {
                toUpdate.setCompletedDate(null);
            }
        }

        if (request.containsKey(FINISH_TIME_REQUEST_KEY) ) {
            gc.setTimeInMillis((long) request.get(FINISH_TIME_REQUEST_KEY));
            toUpdate.setCompletedDate((GregorianCalendar) gc.clone());
        }

        if (request.containsKey(TASK_NAME_REQUEST_KEY)) {
            toUpdate.setTitle(request.get(TASK_NAME_REQUEST_KEY).toString());
        }

        if (request.containsKey(TASK_TYPE_REQUEST_KEY)) {
            toUpdate.setType(TaskType.valueOf(request.get(TASK_TYPE_REQUEST_KEY).toString()));
        }

        if (request.containsKey(TASK_DETAILS_REQUEST_KEY)) {
            toUpdate.setDescription(request.get(TASK_DETAILS_REQUEST_KEY).toString());
        }

        if (request.containsKey(TASK_CATEGORY_REQUEST_KEY)) {
            toUpdate.setCategory(request.get(TASK_CATEGORY_REQUEST_KEY).toString());
        }

        planner.addTask(toUpdate);
        plannerRepo.save(planner);
        return planner;
    }
}
