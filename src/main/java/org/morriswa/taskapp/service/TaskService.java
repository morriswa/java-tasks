package org.morriswa.taskapp.service;

import com.amazonaws.services.managedblockchain.model.IllegalActionException;
import org.morriswa.taskapp.entity.CustomAuth0User;
import org.morriswa.taskapp.entity.Planner;
import org.morriswa.taskapp.entity.Task;
import org.morriswa.taskapp.entity.UserProfile;
import org.morriswa.taskapp.enums.TaskStatus;
import org.morriswa.taskapp.enums.TaskType;
import org.morriswa.taskapp.exception.AuthenticationFailedException;
import org.morriswa.taskapp.exception.RequestFailedException;
import org.morriswa.taskapp.repo.PlannerRepo;
import org.morriswa.taskapp.repo.TaskRepo;
import org.morriswa.taskapp.repo.UserProfileRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.morriswa.taskapp.exception.CustomExceptionSupply.noPlannerFoundException;
import static org.morriswa.taskapp.exception.CustomExceptionSupply.noTaskFoundException;

@Service
public class TaskService {
    private final UserProfileRepo profileRepo;
    private final PlannerRepo plannerRepo;
    private final TaskRepo taskRepo;

//    private final String PLANNER_NAME;
//    private final String PLANNER_ID;
//    private final String TASK_NAME;
//    private final String TASK_ID;
//    private final String

    @Autowired
    public TaskService(UserProfileRepo p,PlannerRepo pr,TaskRepo tr) {
        this.profileRepo = p;
        this.plannerRepo = pr;
        this.taskRepo = tr;
    }

//    // HELPERS
//    private static void validateRequestBody(Map<String,Object> request,List<String> requiredRequestKeys)
//            throws RequestFailedException
//    {
//        for (String key : requiredRequestKeys) {
//            if (!request.containsKey(key)) {
//                throw new RequestFailedException(
//                        String.format("Bad request... Http request body is missing required param: %s",key));
//            }
//        }
//    }

    // Profile Actions
    public UserProfile profileGet(CustomAuth0User authenticatedUser)
            throws AuthenticationFailedException
    {
        return profileRepo.findByUser(authenticatedUser)
                .orElseThrow(() -> new AuthenticationFailedException(
                        String.format("Could not access profile for user: %s",authenticatedUser.getOnlineId())));
    }

    public UserProfile profileUpdate(CustomAuth0User user, Map<String,Object> newProfileRequest)
            throws AuthenticationFailedException
    {
        UserProfile profile = profileRepo.findByUser(user)
                .orElseThrow(() -> new AuthenticationFailedException(
                        String.format("Could not access profile for user: %s",user.getOnlineId())));

        UserProfile updatedProfile = UserProfile.builder()
                .id(profile.getId())
                .user(profile.getUser())
                .nameFirst(String.valueOf(newProfileRequest.getOrDefault("name-first",profile.getNameFirst())))
                .nameMiddle(String.valueOf(newProfileRequest.getOrDefault("name-middle",profile.getNameMiddle())))
                .nameLast(String.valueOf(newProfileRequest.getOrDefault("name-last",profile.getNameLast())))
                .displayName(String.valueOf(newProfileRequest.getOrDefault("name-display",profile.getDisplayName())))
                .pronouns(String.valueOf(newProfileRequest.getOrDefault("pronouns",profile.getPronouns())))
                        .build();

        profileRepo.save(updatedProfile);
        return updatedProfile;
    }


    // Planner Actions
    public Planner getPlanner(CustomAuth0User authenticatedUser, Map<String, Object> request)
            throws RequestFailedException
    {
        final Long PLANNER_ID;
        try {
            PLANNER_ID = Integer.toUnsignedLong((int) request.get("planner-id"));
        } catch (Exception e) {
            throw new RequestFailedException(e.getMessage());
        }

        return plannerRepo.findByUserAndId(authenticatedUser,PLANNER_ID)
                .orElseThrow(noPlannerFoundException(PLANNER_ID,authenticatedUser));
    }

    public Set<Planner> getAllPlanners(CustomAuth0User authenticatedUser) {
        return plannerRepo.findAllByUser(authenticatedUser);
    }

    public Set<Planner> plannerAdd(CustomAuth0User user, Map<String,Object> newPlannerRequest)
            throws RequestFailedException
    {
        GregorianCalendar gc = new GregorianCalendar();

        final String PLANNER_NAME;
        try {
            PLANNER_NAME = newPlannerRequest.get("planner-name").toString();
        } catch (Exception e) {
            throw new RequestFailedException(e.getMessage());
        }

        if (plannerRepo.existsByUserAndName(user,PLANNER_NAME)) {
            throw new RequestFailedException(
                    String.format("Planner with name %s already exists for user %s",PLANNER_NAME,user.getOnlineId()));
        }

        if (PLANNER_NAME.equals("")) {
            throw new RequestFailedException("Planner cannot have no name: ");
        }

        Planner.PlannerBuilder newPlanner = Planner.builder()
                .user(user).name(PLANNER_NAME);

        if (newPlannerRequest.containsKey("planner-goal")) {
            newPlanner.goal((String) newPlannerRequest.get("planner-goal"));
        }

        if (newPlannerRequest.containsKey("start-year") &&
            newPlannerRequest.containsKey("start-month") &&
            newPlannerRequest.containsKey("start-day")) {
            gc.set( (int) newPlannerRequest.get("start-year"),
                    (int) newPlannerRequest.get("start-month"),
                    (int) newPlannerRequest.get("start-day"));
            newPlanner.startDate((GregorianCalendar) gc.clone());
        }

        if (    newPlannerRequest.containsKey("finish-year") &&
                newPlannerRequest.containsKey("finish-month") &&
                newPlannerRequest.containsKey("finish-day")) {
            gc.set( (int) newPlannerRequest.get("finish-year"),
                    (int) newPlannerRequest.get("finish-month"),
                    (int) newPlannerRequest.get("finish-day"));
            newPlanner.finishDate((GregorianCalendar) gc.clone());
        }


//        Planner newPlanner = new Planner(user,PLANNER_NAME);
        plannerRepo.save(newPlanner.build());
        return plannerRepo.findAllByUser(user);
    }

    public Set<Planner> plannerDel(CustomAuth0User user, Map<String,Object> request)
            throws RequestFailedException
    {
        final Long PLANNER_ID;
        try {
            PLANNER_ID = Integer.toUnsignedLong((int) request.get("planner-id"));
        } catch (Exception e) {
            throw new RequestFailedException(e.getMessage());
        }

        Optional<Planner> planner = plannerRepo.findByUserAndId(user,PLANNER_ID);

        plannerRepo.delete(planner
                .orElseThrow(noPlannerFoundException(PLANNER_ID,user)));

        return plannerRepo.findAllByUser(user);
    }

    public Planner updatePlanner(CustomAuth0User user,Map<String,Object> request)
            throws RequestFailedException
    {
        GregorianCalendar gc = new GregorianCalendar();
        final Long PLANNER_ID;
        try {
            PLANNER_ID = Integer.toUnsignedLong((int) request.get("planner-id"));
        } catch (Exception e) {
            throw new RequestFailedException(e.getMessage());
        }

        Planner planner = plannerRepo.findByUserAndId(user,PLANNER_ID)
                .orElseThrow(noPlannerFoundException(PLANNER_ID,user));

        if (request.containsKey("planner-name")) {
            planner.setName(request.get("planner-name").toString());
        }


        if (request.containsKey("planner-goal")) {
            planner.setGoal((String) request.get("planner-goal"));
        }

        if (request.containsKey("start-year") &&
            request.containsKey("start-month") &&
            request.containsKey("start-day")) {
            gc.set( (int) request.get("start-year"),
                    (int) request.get("start-month"),
                    (int) request.get("start-day"));
            planner.setStartDate((GregorianCalendar) gc.clone());
        }

        if (    request.containsKey("finish-year") &&
                request.containsKey("finish-month") &&
                request.containsKey("finish-day")) {
            gc.set( (int) request.get("finish-year"),
                    (int) request.get("finish-month"),
                    (int) request.get("finish-day"));
            planner.setFinishDate((GregorianCalendar) gc.clone());
        }

        plannerRepo.save(planner);
        return plannerRepo.findByUserAndId(user,PLANNER_ID)
                .orElseThrow(noPlannerFoundException(PLANNER_ID,user));
    }


    // Task Actions
    public Planner taskAdd(CustomAuth0User user, Map<String,Object> request)
            throws RequestFailedException
    {
        GregorianCalendar gc = new GregorianCalendar();

        final Long PLANNER_ID;
        final String TASK_NAME;
        try {
            PLANNER_ID = Integer.toUnsignedLong((int) request.get("planner-id"));
            TASK_NAME = request.get("task-name").toString();
        } catch (Exception e) {
            throw new RequestFailedException(e.getMessage());
        }

        Planner planner = plannerRepo.findByUserAndId(user,PLANNER_ID)
                .orElseThrow(noPlannerFoundException(PLANNER_ID,user));
        Task.TaskBuilder newTask = Task.builder()
                .planner(planner).title(TASK_NAME).creationDate(new GregorianCalendar());

        if (    request.containsKey("start-year") &&
                request.containsKey("start-month") &&
                request.containsKey("start-day")) {
            gc.set( (int) request.get("start-year"),
                    (int) request.get("start-month"),
                    (int) request.get("start-day"));
            newTask.startDate((GregorianCalendar) gc.clone());
        }

        if (    request.containsKey("due-year") &&
                request.containsKey("due-month") &&
                request.containsKey("due-day")) {
            gc.set( (int) request.get("due-year"),
                    (int) request.get("due-month"),
                    (int) request.get("due-day"));
            newTask.dueDate((GregorianCalendar) gc.clone());
        }

        if (request.containsKey("task-status")) {
            newTask.status(TaskStatus.valueOf(request.get("task-status").toString()));

            if ((TaskStatus.valueOf((String) request.get
                    ("task-status"))).progress >= TaskStatus.COMPLETED.progress) {
                newTask.completedDate(new GregorianCalendar());
            } else {
                newTask.completedDate(null);
            }
        } else {
            newTask.status(TaskStatus.NEW);
        }

        if (request.containsKey("task-type")) {
            newTask.type(TaskType.valueOf(request.get("task-type").toString()));
        } else {
            newTask.type(TaskType.TASK);
        }

        if (request.containsKey("task-details")) {
            newTask.description(request.get("task-details").toString());
        } else {
            newTask.description("");
        }


        if (request.containsKey("task-category")) {
            newTask.category(request.get("task-category").toString());
        } else {
            newTask.category("");
        }

        planner.addTask(newTask.build());
        plannerRepo.save(planner);
        return plannerRepo.findByUserAndId(user,PLANNER_ID)
                .orElseThrow(noPlannerFoundException(PLANNER_ID,user));
    }

    public Planner taskDel(CustomAuth0User user, Map<String,Object> updatePlannerRequest)
            throws RequestFailedException
    {
        final Long PLANNER_ID;
        final Long TASK_ID;
        try {
             PLANNER_ID = Integer.toUnsignedLong((int) updatePlannerRequest.get("planner-id"));
             TASK_ID = Integer.toUnsignedLong((int) updatePlannerRequest.get("task-id"));
        } catch (Exception e) {
            throw new RequestFailedException(e.getMessage());
        }

        Planner planner = plannerRepo.findByUserAndId(user,PLANNER_ID)
                .orElseThrow(noPlannerFoundException(PLANNER_ID,user));

        planner.deleteTask(taskRepo.findByPlannerAndId(planner,TASK_ID)
                .orElseThrow(noTaskFoundException(TASK_ID,planner)));

        plannerRepo.save(planner);
        return plannerRepo.findByUserAndId(user,PLANNER_ID)
                .orElseThrow(noPlannerFoundException(PLANNER_ID,user));
    }

    public Planner updateTask(CustomAuth0User user, Map<String,Object> request)
            throws RequestFailedException
    {
        GregorianCalendar gc = new GregorianCalendar();
        final Long PLANNER_ID;
        final Long TASK_ID;
        try {
            PLANNER_ID = Integer.toUnsignedLong((int) request.get("planner-id"));
            TASK_ID = Integer.toUnsignedLong((int)  request.get("task-id"));
        } catch (Exception e) {
            throw new RequestFailedException(e.getMessage());
        }

        Planner planner = plannerRepo.findByUserAndId(user,PLANNER_ID)
                .orElseThrow(noPlannerFoundException(PLANNER_ID,user));
        Task toUpdate = taskRepo.findByPlannerAndId(planner,TASK_ID)
                .orElseThrow(noTaskFoundException(TASK_ID,planner));
        planner.deleteTask(toUpdate);

        if (
                request.containsKey("start-year") &&
                request.containsKey("start-month") &&
                request.containsKey("start-day")
        ) {
            gc.set( (int) request.get("start-year"),
                    (int) request.get("start-month"),
                    (int) request.get("start-day"));
            toUpdate.setStartDate((GregorianCalendar) gc.clone());
        }

        if (
                request.containsKey("due-year") &&
                request.containsKey("due-month") &&
                request.containsKey("due-day")
        ) {
            gc.set( (int) request.get("due-year"),
                    (int) request.get("due-month"),
                    (int) request.get("due-day"));
            toUpdate.setDueDate((GregorianCalendar) gc.clone());
        }

        if (request.containsKey("task-status")) {
            toUpdate.setStatus(TaskStatus.valueOf(request.get("task-status").toString()));

            if (toUpdate.getStatus().progress >= TaskStatus.COMPLETED.progress) {
                toUpdate.setCompletedDate(new GregorianCalendar());
            } else {
                toUpdate.setCompletedDate(null);
            }
        }

        if (
                request.containsKey("finish-year") &&
                        request.containsKey("finish-month") &&
                        request.containsKey("finish-day")
        ) {
            gc.set( (int) request.get("finish-year"),
                    (int) request.get("finish-month"),
                    (int) request.get("finish-day"));
            toUpdate.setCompletedDate((GregorianCalendar) gc.clone());
        }

        if (request.containsKey("task-type")) {
            toUpdate.setType(TaskType.valueOf(request.get("task-type").toString()));
        }

        if (request.containsKey("task-details")) {
            toUpdate.setDescription(request.get("task-details").toString());
        }

        if (request.containsKey("task-category")) {
            toUpdate.setCategory(request.get("task-category").toString());
        }

        planner.addTask(toUpdate);
        plannerRepo.save(planner);
        return plannerRepo.findByUserAndId(user,PLANNER_ID)
                .orElseThrow(noPlannerFoundException(PLANNER_ID,user));
    }

    @Deprecated
    public Planner oldTaskUpdate(CustomAuth0User user, Map<String,Object> request) {
        if (
                !request.containsKey("planner-name") ||
                !request.containsKey("task-id") ||
                !request.containsKey("task-status") ||
                !request.containsKey("task-type") ||
                !request.containsKey("task-details")
        ) {
            throw new IllegalStateException("Bad request");
        }
        final String PLANNER_NAME = request.get("planner-name").toString();
        final Long TASK_ID = Integer.toUnsignedLong((int) request.get("task-id"));
        final TaskStatus TASK_STATUS = TaskStatus.valueOf(request.get("task-status").toString());
        final TaskType TASK_TYPE = TaskType.valueOf(request.get("task-type").toString());
        final String TASK_DETAILS = request.get("task-details").toString();

        Planner planner = plannerRepo.findByUserAndName(user,PLANNER_NAME)
                .orElseThrow(() -> new IllegalActionException("No planner found to update"));
        Task toUpdate = taskRepo.getByPlannerAndId(planner,TASK_ID);
        planner.deleteTask(toUpdate);

        toUpdate.setStatus(TASK_STATUS);
        toUpdate.setType(TASK_TYPE);
        toUpdate.setDescription(TASK_DETAILS);

        planner.addTask(toUpdate);
        plannerRepo.save(planner);
        return plannerRepo.findByUserAndName(user,PLANNER_NAME)
                .orElseThrow(() -> new IllegalActionException("could not access planner"));
    }

    @Deprecated
    public Planner oldUpdateTaskStatus(CustomAuth0User user, Map<String,Object> request) {
        if (
                !request.containsKey("planner-name") ||
                        !request.containsKey("task-id") ||
                        !request.containsKey("task-status")
        ) {
            throw new IllegalStateException("Bad request");
        }
        final String PLANNER_NAME = request.get("planner-name").toString();
        final Long TASK_ID = Integer.toUnsignedLong((int) request.get("task-id"));
        final TaskStatus TASK_STATUS = TaskStatus.valueOf(request.get("task-status").toString());

        Planner planner = plannerRepo.findByUserAndName(user,PLANNER_NAME)
                .orElseThrow(() -> new IllegalActionException("No planner found to update"));
        Task toUpdate = taskRepo.getByPlannerAndId(planner,TASK_ID);
        planner.deleteTask(toUpdate);

        toUpdate.setStatus(TASK_STATUS);

        planner.addTask(toUpdate);
        plannerRepo.save(planner);
        return plannerRepo.findByUserAndName(user,PLANNER_NAME)
                .orElseThrow(() -> new IllegalActionException("could not access planner"));
    }

}
