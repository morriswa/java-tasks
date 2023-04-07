package org.morriswa.taskapp.control;

import org.morriswa.common.model.BadRequestException;
import org.morriswa.common.model.DefaultResponse;
import org.morriswa.taskapp.entity.Planner;
import org.morriswa.taskapp.entity.Task;
import org.morriswa.taskapp.entity.UserProfile;
import org.morriswa.taskapp.model.PlannerRequest;
import org.morriswa.taskapp.model.PlannerResponse;
import org.morriswa.taskapp.model.TaskRequest;
import org.morriswa.taskapp.model.UserProfileRequest;
import org.morriswa.taskapp.service.TaskService;
import org.morriswa.taskapp.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@SuppressWarnings("unused")
@RestController @CrossOrigin
@RequestMapping(path = "${server.path}")
public class TaskController {
    private final TaskService taskService;
    private final UserProfileService userProfileService;

    @Autowired
    public TaskController(TaskService s, UserProfileService u, Environment e) {
        this.taskService = s;
        this.userProfileService = u;
    }

    // User Profile ENDPOINTS
    @PostMapping(path = "login")
    public ResponseEntity<?> registerUser(JwtAuthenticationToken token,
                                          @RequestBody UserProfileRequest request) {
        request.setOnlineId(token.getName());
        this.userProfileService.updateUserProfile(request);

        return ResponseEntity.ok().body(DefaultResponse.builder()
                .timestamp(new GregorianCalendar())
                .message(
                    String.format("User with email %s, ID %S registered successfully.",
                        request.getEmail(),
                        request.getOnlineId()))
                .build());
    }

    @GetMapping(path = "login")
    public ResponseEntity<?> login(JwtAuthenticationToken token) throws BadRequestException {
        UserProfile newUser = userProfileService.getUserProfile(token.getName());

        return ResponseEntity.ok().body(DefaultResponse.builder()
                .timestamp(new GregorianCalendar())
                .message(
                    String.format("User with email %s, ID %S authenticated successfully.",
                        newUser.getEmail(),
                        newUser.getOnlineId()))
                .build());
    }


    // PLANNER ENDPOINTS
    @GetMapping(path = "planner")
    public ResponseEntity<?> getPlanner(JwtAuthenticationToken token,
                                        @RequestParam Optional<Long> id) throws Exception {
        var plannerId = id.orElse(null);
        PlannerResponse planner = this.taskService.getPlannerWithTasks(token.getName(),plannerId);

        return ResponseEntity.ok().body(DefaultResponse.builder()
                .timestamp(new GregorianCalendar())
                .message("Successfully retrieved Planner...")
                .payload(planner)
                .build());
    }

    @GetMapping(path = "planners")
    public ResponseEntity<?> getAllPlanners(JwtAuthenticationToken token) {
        Set<Planner> planners = this.taskService.getAllPlanners(token.getName());

        return ResponseEntity.ok().body(DefaultResponse.builder()
                .timestamp(new GregorianCalendar())
                .message("Successfully retrieved all Planners...")
                .payload(planners)
                .build());
    }

    @PostMapping(path = "planner")
    public ResponseEntity<?> addPlannerToProfile(JwtAuthenticationToken token,
                                                 @RequestBody PlannerRequest request) throws Exception
    {
        request.setOnlineId(token.getName());
        Set<Planner> planners = this.taskService.plannerAdd(request);

        return ResponseEntity.ok().body(DefaultResponse.builder()
                .timestamp(new GregorianCalendar())
                .message("Successfully added Planner...")
                .payload(planners)
                .build());
    }

    @DeleteMapping(path = "planner")
    public ResponseEntity<?> plannerDel(JwtAuthenticationToken token,
                                        @RequestBody PlannerRequest request) throws Exception
    {
        request.setOnlineId(token.getName());
        Set<Planner> planners = this.taskService.plannerDel(request);

        return ResponseEntity.ok().body(DefaultResponse.builder()
                .timestamp(new GregorianCalendar())
                .message("Successfully deleted Planner...")
                .payload(planners)
                .build());
    }

    @PatchMapping(path = "planner")
    public ResponseEntity<?> updatePlanner( JwtAuthenticationToken token,
                                            @RequestBody PlannerRequest request)
            throws MissingRequestValueException, BadRequestException {
        request.setOnlineId(token.getName());
        Planner planner = this.taskService.updatePlanner(request);

        return ResponseEntity.ok().body(DefaultResponse.builder()
                .timestamp(new GregorianCalendar())
                .message("Successfully updated Planner...")
                .payload(planner)
                .build());
    }


    // TASK ENDPOINTS
    @PostMapping(path = "task")
    public ResponseEntity<?> addTask(JwtAuthenticationToken token,
                                               @RequestBody TaskRequest request) throws Exception
    {
        request.setOnlineId(token.getName());
        Set<Task> tasks = this.taskService.taskAdd(request);

        return ResponseEntity.ok().body(DefaultResponse.builder()
                .timestamp(new GregorianCalendar())
                .message("Successfully added Task...")
                .payload(tasks)
                .build());
    }

    @DeleteMapping(path = "task")
    public ResponseEntity<?> delTask(JwtAuthenticationToken token,
                                     @RequestBody TaskRequest request) throws Exception
    {
        request.setOnlineId(token.getName());
        this.taskService.taskDel(request);

        return ResponseEntity.ok().body(DefaultResponse.builder()
                .timestamp(new GregorianCalendar())
                .message("Successfully deleted Task...")
                .build());
    }

    @PatchMapping(path = "task")
    public ResponseEntity<?> updateTask(JwtAuthenticationToken token,
                                     @RequestBody TaskRequest request) throws Exception {
        request.setOnlineId(token.getName());
        this.taskService.updateTask(request);

        return ResponseEntity.ok().body(DefaultResponse.builder()
                .timestamp(new GregorianCalendar())
                .message("Successfully updated Task...")
                .build());
    }
}
