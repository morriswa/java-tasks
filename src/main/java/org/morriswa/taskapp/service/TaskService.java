package org.morriswa.taskapp.service;

import com.amazonaws.services.managedblockchain.model.IllegalActionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.catalina.User;
import org.morriswa.taskapp.dao.CustomAuth0User;
import org.morriswa.taskapp.dao.CustomAuth0UserRepo;
import org.morriswa.taskapp.dao.UserProfile;
import org.morriswa.taskapp.dao.UserProfileRepo;
import org.morriswa.taskapp.entity.Planner;
import org.morriswa.taskapp.entity.Task;
import org.morriswa.taskapp.entity.TaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.*;

@Service
public class TaskService {
    private final UserProfileRepo profileRepo;
    @Autowired
    public TaskService(UserProfileRepo p) {
        this.profileRepo = p;
    }

    public UserProfile updateUserProfile(CustomAuth0User user, Map<String,Object> newProfileRequest) {
        UserProfile profile = profileRepo.findByUser(user)
                .orElseThrow(() -> new IllegalActionException("could not access profile"));
        UserProfile updatedProfile = UserProfile.builder()
                .id(profile.getId()).user(profile.getUser()).planners(profile.getPlanners())
                .nameFirst(String.valueOf(newProfileRequest.getOrDefault("name-first",profile.getNameFirst())))
                .nameMiddle(String.valueOf(newProfileRequest.getOrDefault("name-middle",profile.getNameMiddle())))
                .nameLast(String.valueOf(newProfileRequest.getOrDefault("name-last",profile.getNameLast())))
                .displayName(String.valueOf(newProfileRequest.getOrDefault("name-display",profile.getDisplayName())))
                .pronouns(String.valueOf(newProfileRequest.getOrDefault("pronouns",profile.getPronouns())))
                        .build();

//        if (newProfileRequest.containsKey("name-first") &&
//                !newProfileRequest.get("name-first").equals("")) {
//            profile.setNameFirst(newProfileRequest.get("name-first").toString());
//        }
//
//        if (newProfileRequest.containsKey("name-middle") &&
//                !newProfileRequest.get("name-middle").equals("")) {
//            profile.setNameMiddle(newProfileRequest.get("name-middle").toString());
//        }
//
//        if (newProfileRequest.containsKey("name-last") &&
//                !newProfileRequest.get("name-last").equals("")) {
//            profile.setNameLast(newProfileRequest.get("name-last").toString());
//        }
//
//        if (newProfileRequest.containsKey("name-display") &&
//                !newProfileRequest.get("name-display").equals("")) {
//            profile.setDisplayName(newProfileRequest.get("name-display").toString());
//        }
//
//        if (newProfileRequest.containsKey("pronouns") &&
//                !newProfileRequest.get("pronouns").equals("")) {
//            profile.setPronouns(newProfileRequest.get("pronouns").toString());
//        }

        profileRepo.save(updatedProfile);
        return updatedProfile;
    }

    public UserProfile newPlanner(CustomAuth0User user, Map<String,Object> newPlannerRequest) {
        UserProfile profile = profileRepo.findByUser(user)
                .orElseThrow(() -> new IllegalActionException("could not access profile"));

        if (!newPlannerRequest.containsKey("planner-name")) {
            throw new IllegalActionException("Bad request");
        }

        Planner newPlanner = new Planner(newPlannerRequest.get("planner-name").toString(),new ArrayList<>());
        profile.addPlanner(newPlanner);
        profileRepo.save(profile);
        return profile;
    }
    public UserProfile getUserProfile(CustomAuth0User authenticatedUser) {
        return profileRepo.findByUser(authenticatedUser)
                .orElseThrow(() -> new IllegalActionException("could not access profile"));
    }

    public UserProfile updatePlannerWithNewTask(CustomAuth0User user, Map<String,Object> updatePlannerRequest) {
        final String PLANNER_NAME = updatePlannerRequest.get("planner-name").toString();
        final String TASK_NAME = updatePlannerRequest.get("task-name").toString();

        GregorianCalendar gc = new GregorianCalendar();
        gc.set( Integer.parseInt(updatePlannerRequest.get("start-year").toString()),
                Integer.parseInt(updatePlannerRequest.get("start-month").toString()),
                Integer.parseInt(updatePlannerRequest.get("start-day").toString()));
        final GregorianCalendar START_DATE = (GregorianCalendar) gc.clone();
        gc.set( Integer.parseInt(updatePlannerRequest.get("finish-year").toString()),
                Integer.parseInt(updatePlannerRequest.get("finish-month").toString()),
                Integer.parseInt(updatePlannerRequest.get("finish-day").toString()));
        final GregorianCalendar FINISH_DATE = (GregorianCalendar) gc.clone();

        UserProfile profile = profileRepo.findByUser(user)
                .orElseThrow(() -> new IllegalActionException("could not access profile"));

        if (!profile.getPlanners().containsKey(PLANNER_NAME)) {
            throw new IllegalActionException("No planner found to update");
        }

        Task newTask = new Task(TASK_NAME,START_DATE,FINISH_DATE);
        profile.updatePlanner(PLANNER_NAME,newTask);
        profileRepo.save(profile);
        return profile;
    }

    public UserProfile deleteTaskInPlanner(CustomAuth0User user, Map<String,Object> updatePlannerRequest) {
        final String PLANNER_NAME = updatePlannerRequest.get("planner-name").toString();
        final int TASK_INDEX = (int) updatePlannerRequest.get("task-index");

        UserProfile profile = profileRepo.findByUser(user)
                .orElseThrow(() -> new IllegalActionException("could not access profile"));

        profile.deleteTaskInPlanner(TASK_INDEX,PLANNER_NAME);
        profileRepo.save(profile);
        return profile;
    }

    public UserProfile updateTaskStatus(CustomAuth0User user, Map<String,Object> taskCompleteRequest) {
        final String PLANNER_NAME = taskCompleteRequest.get("planner-name").toString();
        final TaskStatus TASK_STATUS = TaskStatus.valueOf(taskCompleteRequest.get("task-status").toString());
        final int TASK_INDEX = Integer.parseInt(taskCompleteRequest.get("task-index").toString());

        UserProfile profile = profileRepo.findByUser(user)
                .orElseThrow(() -> new IllegalActionException("could not access profile"));

        profile.updateTaskInPlanner(TASK_INDEX,TASK_STATUS,PLANNER_NAME);
        profileRepo.save(profile);
        return profile;
    }


}
