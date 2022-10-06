package org.morriswa.taskapp.service;

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

import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.morriswa.taskapp.exception.CustomExceptionSupply.noPlannerFoundException;
import static org.morriswa.taskapp.exception.CustomExceptionSupply.noTaskFoundException;

@Service
public class TaskService {
    private final UserProfileRepo profileRepo;
    private final PlannerRepo plannerRepo;
    private final TaskRepo taskRepo;

    @Autowired
    public TaskService(UserProfileRepo p,PlannerRepo pr,TaskRepo tr) {
        this.profileRepo = p;
        this.plannerRepo = pr;
        this.taskRepo = tr;
    }


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
                .nameFirst((String) newProfileRequest.getOrDefault("name-first",profile.getNameFirst()))
                .nameMiddle((String) newProfileRequest.getOrDefault("name-middle",profile.getNameMiddle()))
                .nameLast((String) newProfileRequest.getOrDefault("name-last",profile.getNameLast()))
                .displayName((String) newProfileRequest.getOrDefault("name-display",profile.getDisplayName()))
                .pronouns((String) newProfileRequest.getOrDefault("pronouns",profile.getPronouns()))
                        .build();

        profileRepo.save(updatedProfile);
        return updatedProfile;
    }


    // Planner Actions
    public Planner getPlanner(CustomAuth0User authenticatedUser, Map<String, Object> request)
            throws Exception
    {
        final Long PLANNER_ID = Integer.toUnsignedLong((int) request.get("planner-id"));

        return plannerRepo.findByUserAndId(authenticatedUser,PLANNER_ID)
                .orElseThrow(noPlannerFoundException(PLANNER_ID,authenticatedUser));
    }

    public Set<Planner> getAllPlanners(CustomAuth0User authenticatedUser) {
        return plannerRepo.findAllByUser(authenticatedUser);
    }

    public Set<Planner> plannerAdd(CustomAuth0User user, Map<String,Object> newPlannerRequest)
            throws Exception
    {
        GregorianCalendar gc = new GregorianCalendar();

        final String PLANNER_NAME = newPlannerRequest.get("planner-name").toString();

        if (plannerRepo.existsByUserAndName(user,PLANNER_NAME)) {
            throw new RequestFailedException(
                    String.format("Planner with name %s already exists for user %s",PLANNER_NAME,user.getOnlineId()));
        }

        if (PLANNER_NAME.equals("")) {
            throw new RequestFailedException("Planner cannot have no name");
        }

        Planner.PlannerBuilder newPlanner = Planner.builder()
                .user(user).name(PLANNER_NAME);

        if (newPlannerRequest.containsKey("planner-goal")) {
            newPlanner.goal((String) newPlannerRequest.get("planner-goal"));
        }

        if (newPlannerRequest.containsKey("start-greg")) {
            gc.setTimeInMillis((Long) newPlannerRequest.get("start-greg"));
            newPlanner.startDate((GregorianCalendar) gc.clone());
        }

        if (newPlannerRequest.containsKey("finish-greg")) {
            gc.setTimeInMillis((Long) newPlannerRequest.get("finish-greg"));
            newPlanner.finishDate((GregorianCalendar) gc.clone());
        }

        plannerRepo.save(newPlanner.build());
        return plannerRepo.findAllByUser(user);
    }

    public Set<Planner> plannerDel(CustomAuth0User user, Map<String,Object> request)
            throws Exception
    {
        final Long PLANNER_ID = Integer.toUnsignedLong((int) request.get("planner-id"));

        Optional<Planner> planner = plannerRepo.findByUserAndId(user,PLANNER_ID);

        plannerRepo.delete(planner
                .orElseThrow(noPlannerFoundException(PLANNER_ID,user)));

        return plannerRepo.findAllByUser(user);
    }

    public Planner updatePlanner(CustomAuth0User user,Map<String,Object> request)
            throws RequestFailedException
    {
        GregorianCalendar gc = new GregorianCalendar();
        final Long PLANNER_ID = Integer.toUnsignedLong((int) request.get("planner-id"));

        Planner planner = plannerRepo.findByUserAndId(user,PLANNER_ID)
                .orElseThrow(noPlannerFoundException(PLANNER_ID,user));

        if (request.containsKey("planner-name")) {
            planner.setName(request.get("planner-name").toString());
        }

        if (request.containsKey("planner-goal")) {
            planner.setGoal((String) request.get("planner-goal"));
        }

        if (request.containsKey("start-greg")) {
            gc.setTimeInMillis((Long) request.get("start-greg"));
            planner.setStartDate((GregorianCalendar) gc.clone());
        }

        if (request.containsKey("finish-greg")) {
            gc.setTimeInMillis((Long) request.get("finish-greg"));
            planner.setFinishDate((GregorianCalendar) gc.clone());
        }

        plannerRepo.save(planner);
        return plannerRepo.findByUserAndId(user,PLANNER_ID)
                .orElseThrow(noPlannerFoundException(PLANNER_ID,user));
    }


    // Task Actions
    public Planner taskAdd(CustomAuth0User user, Map<String,Object> request)
            throws Exception
    {
        GregorianCalendar gc = new GregorianCalendar();

        final Long PLANNER_ID = Integer.toUnsignedLong((int) request.get("planner-id"));
        final String TASK_NAME = request.get("task-name").toString();

        Planner planner = plannerRepo.findByUserAndId(user,PLANNER_ID)
                .orElseThrow(noPlannerFoundException(PLANNER_ID,user));
        Task.TaskBuilder newTask = Task.builder()
                .planner(planner).title(TASK_NAME).creationDate(new GregorianCalendar());


        if (request.containsKey("start-greg")) {
            gc.setTimeInMillis((Long) request.get("start-greg"));
            newTask.startDate((GregorianCalendar) gc.clone());
        }

        if (    request.containsKey("due-greg") ) {
            gc.setTimeInMillis((long) request.get("due-greg"));
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

        if (request.containsKey("finish-greg") ) {
            gc.setTimeInMillis((long) request.get("finish-greg"));
            newTask.completedDate((GregorianCalendar) gc.clone());
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
            throws Exception
    {
        final Long PLANNER_ID = Integer.toUnsignedLong((int) updatePlannerRequest.get("planner-id"));
        final Long TASK_ID = Integer.toUnsignedLong((int) updatePlannerRequest.get("task-id"));

        Planner planner = plannerRepo.findByUserAndId(user,PLANNER_ID)
                .orElseThrow(noPlannerFoundException(PLANNER_ID,user));

        planner.deleteTask(taskRepo.findByPlannerAndId(planner,TASK_ID)
                .orElseThrow(noTaskFoundException(TASK_ID,planner)));

        plannerRepo.save(planner);
        return plannerRepo.findByUserAndId(user,PLANNER_ID)
                .orElseThrow(noPlannerFoundException(PLANNER_ID,user));
    }

    public Planner updateTask(CustomAuth0User user, Map<String,Object> request)
            throws Exception
    {
        GregorianCalendar gc = new GregorianCalendar();
        final Long PLANNER_ID = Integer.toUnsignedLong((int) request.get("planner-id"));
        final Long TASK_ID = Integer.toUnsignedLong((int)  request.get("task-id"));

        Planner planner = plannerRepo.findByUserAndId(user,PLANNER_ID)
                .orElseThrow(noPlannerFoundException(PLANNER_ID,user));
        Task toUpdate = taskRepo.findByPlannerAndId(planner,TASK_ID)
                .orElseThrow(noTaskFoundException(TASK_ID,planner));
        planner.deleteTask(toUpdate);

        if (request.containsKey("start-greg")) {
            gc.setTimeInMillis((Long) request.get("start-greg"));
            toUpdate.setStartDate((GregorianCalendar) gc.clone());
        }

        if (request.containsKey("due-greg")) {
            gc.setTimeInMillis((long) request.get("due-greg"));
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

        if (request.containsKey("finish-greg") ) {
            gc.setTimeInMillis((long) request.get("finish-greg"));
            toUpdate.setCompletedDate((GregorianCalendar) gc.clone());
        }

        if (request.containsKey("task-name")) {
            toUpdate.setTitle(request.get("task-name").toString());
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

}
