package org.morriswa.taskapp.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.morriswa.taskapp.dao.CustomAuth0User;
import org.morriswa.taskapp.dao.UserProfile;
import org.morriswa.taskapp.dao.UserProfileRepo;
import org.morriswa.taskapp.entity.Planner;
import org.morriswa.taskapp.entity.Task;
import org.morriswa.taskapp.entity.TaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.Principal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.morriswa.taskapp.TestSuite.getPrincipal;

@SpringBootTest
class TaskServiceTest {
    private final TaskService test;
    private final UserProfileRepo profileRepo = Mockito.mock(UserProfileRepo.class);
    @Autowired
    public TaskServiceTest() {
        this.test = new TaskService(profileRepo);
    }

    @Test
    void updateUserProfileTest() {
        Principal p = getPrincipal();
        String email = "junit@morriswa.org";
        CustomAuth0User testUser = new CustomAuth0User(p.getName(),email);
        Map<String,Object> testRequest = new HashMap<>();
        testRequest.put("name-display","Test User 007");
        testRequest.put("pronouns","She/Her/Hers");
        testRequest.put("name-middle","Testington");

        when(profileRepo.findByUser(testUser)).thenReturn(Optional.of(new UserProfile()));

        UserProfile updatedUserProfile = test.updateUserProfile(testUser,testRequest);
        assertEquals("Test User 007",updatedUserProfile.getDisplayName());
        assertEquals("She/Her/Hers",updatedUserProfile.getPronouns());
        assertEquals("Testington",updatedUserProfile.getNameMiddle());
        verify(profileRepo).save(any());
    }

    @Test
    void createPlannerTest() {
        Principal p = getPrincipal();
        String email = "junit@morriswa.org";
        CustomAuth0User testUser = new CustomAuth0User(p.getName(),email);
        UserProfile testUserProfile = new UserProfile();
        testUserProfile.setPlanners(new HashMap<>());
        Map<String,Object> testRequest = new HashMap<>();
        testRequest.put("planner-name","My Junit Planner");

        when(profileRepo.findByUser(testUser)).thenReturn(Optional.of(new UserProfile()));

        UserProfile updatedUserProfile = test.newPlanner(testUser,testRequest);
        assertEquals(Boolean.TRUE, updatedUserProfile.getPlanners().containsKey(
                "My Junit Planner"));
        assertEquals(new Planner("My Junit Planner",new ArrayList<>()),
                updatedUserProfile.getPlanners().get("My Junit Planner"));
        verify(profileRepo).save(any());
    }

    @Test
    void createTaskTest() {

        Principal p = getPrincipal();
        String email = "junit@morriswa.org";
        CustomAuth0User testUser = new CustomAuth0User(p.getName(),email);
        UserProfile testUserProfile = new UserProfile();
        Map<String,Object> testRequest = new HashMap<>();
        testRequest.put("planner-name","My Junit Planner");
        testRequest.put("task-name","My Testing Task");
        testRequest.put("start-year","2002");
        testRequest.put("start-month","1");
        testRequest.put("start-day","1");
        testRequest.put("finish-year","2002");
        testRequest.put("finish-month","1");
        testRequest.put("finish-day","31");

        GregorianCalendar gc = new GregorianCalendar();
        gc.set( Integer.parseInt(testRequest.get("start-year").toString()),
                Integer.parseInt(testRequest.get("start-month").toString()),
                Integer.parseInt(testRequest.get("start-day").toString()));
        final GregorianCalendar START_DATE = (GregorianCalendar) gc.clone();
        gc.set( Integer.parseInt(testRequest.get("finish-year").toString()),
                Integer.parseInt(testRequest.get("finish-month").toString()),
                Integer.parseInt(testRequest.get("finish-day").toString()));
        final GregorianCalendar FINISH_DATE = (GregorianCalendar) gc.clone();

        Planner testPlanner = new Planner("My Junit Planner",new ArrayList<>());
        testUserProfile.addPlanner(testPlanner);

        when(profileRepo.findByUser(testUser)).thenReturn(Optional.of(testUserProfile));

        UserProfile updatedUserProfile = test.updatePlannerWithNewTask(testUser,testRequest);
        assertEquals("My Testing Task",
                updatedUserProfile.getPlanners().get("My Junit Planner").getTasks().get(0).getTitle());
        assertEquals(START_DATE,
                updatedUserProfile.getPlanners().get("My Junit Planner").getTasks().get(0).getStartDate());
    }

    @Test
    void taskSortTest() {

        Principal p = getPrincipal();
        String email = "junit@morriswa.org";
        CustomAuth0User testUser = new CustomAuth0User(p.getName(),email);
        UserProfile testUserProfile = new UserProfile();
        Map<String,Object> testRequest = new HashMap<>();
        testRequest.put("planner-name","My Junit Planner");
        testRequest.put("task-name","Should be SECOND");
        testRequest.put("start-year","2002");
        testRequest.put("start-month","1");
        testRequest.put("start-day","15");
        testRequest.put("finish-year","2002");
        testRequest.put("finish-month","1");
        testRequest.put("finish-day","31");

        GregorianCalendar gc = new GregorianCalendar();
        gc.set( Integer.parseInt(testRequest.get("start-year").toString()),
                Integer.parseInt(testRequest.get("start-month").toString()),
                Integer.parseInt(testRequest.get("start-day").toString()));
        final GregorianCalendar START_DATE = (GregorianCalendar) gc.clone();
        gc.set( Integer.parseInt(testRequest.get("finish-year").toString()),
                Integer.parseInt(testRequest.get("finish-month").toString()),
                Integer.parseInt(testRequest.get("finish-day").toString()));
        final GregorianCalendar FINISH_DATE = (GregorianCalendar) gc.clone();

        Planner testPlanner = new Planner("My Junit Planner",new ArrayList<>());
        testPlanner.addTask(new Task("Should be FIRST",START_DATE,START_DATE));
        testPlanner.addTask(new Task("Should be THIRD",FINISH_DATE,FINISH_DATE));
        testUserProfile.addPlanner(testPlanner);
        when(profileRepo.findByUser(testUser)).thenReturn(Optional.of(testUserProfile));

        UserProfile updatedUserProfile = test.updatePlannerWithNewTask(testUser,testRequest);
        assertEquals("Should be FIRST",
                updatedUserProfile.getPlanners().get("My Junit Planner").getTasks().get(0).getTitle());
        assertEquals("Should be SECOND",
                updatedUserProfile.getPlanners().get("My Junit Planner").getTasks().get(1).getTitle());
        assertEquals("Should be THIRD",
                updatedUserProfile.getPlanners().get("My Junit Planner").getTasks().get(2).getTitle());
        verify(profileRepo).save(any());
    }

    @Test
    void testMarkTaskAsComplete() {
        Principal p = getPrincipal();

        String email = "junit@morriswa.org";
        CustomAuth0User testUser = new CustomAuth0User(p.getName(),email);
        UserProfile testUserProfile = new UserProfile();
        Map<String,Object> testRequest = new HashMap<>();
        testRequest.put("planner-name","My Junit Planner");
        testRequest.put("task-index",0);
        testRequest.put("task-status","COMPLETED");

        GregorianCalendar gc = new GregorianCalendar();
        gc.set( 2002, Calendar.JANUARY, 1);
        final GregorianCalendar START_DATE = (GregorianCalendar) gc.clone();
        gc.set( 2002, Calendar.FEBRUARY, 1);
        final GregorianCalendar FINISH_DATE = (GregorianCalendar) gc.clone();

        Planner testPlanner = new Planner("My Junit Planner",new ArrayList<>());
        testPlanner.addTask(new Task("Should be COMPLETE",START_DATE,FINISH_DATE));
        testUserProfile.addPlanner(testPlanner);
        when(profileRepo.findByUser(testUser)).thenReturn(Optional.of(testUserProfile));

        UserProfile updatedUserProfile = test.updateTaskStatus(testUser,testRequest);
        assertEquals("Should be COMPLETE",
                updatedUserProfile.getPlanners().get("My Junit Planner").getTasks().get(0).getTitle());
        assertEquals(TaskStatus.COMPLETED,
                updatedUserProfile.getPlanners().get("My Junit Planner").getTasks().get(0).getStatus());
        verify(profileRepo).save(any());

    }
}