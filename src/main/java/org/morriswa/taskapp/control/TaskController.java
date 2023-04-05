package org.morriswa.taskapp.control;

import org.morriswa.common.model.BadRequestException;
import org.morriswa.taskapp.entity.Planner;
import org.morriswa.taskapp.entity.Task;
import org.morriswa.taskapp.entity.UserProfile;
import org.morriswa.taskapp.exception.AuthenticationFailedException;
import org.morriswa.taskapp.model.PlannerRequest;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

        Map<String, Object> response = new HashMap<>(){{
           put("message",
                   String.format("User with email %s, ID %S registered successfully.",
                   request.getEmail(),
                   request.getOnlineId()));
        }};
        return ResponseEntity.ok().body(response);
    }

    @GetMapping(path = "login")
    public ResponseEntity<?> login(JwtAuthenticationToken token)
            throws AuthenticationFailedException, BadRequestException {
        UserProfile newUser = userProfileService.getUserProfile(token.getName());

        Map<String, Object> response = new HashMap<>(){{
            put("message",
                    String.format("User with email %s, ID %S authenticated successfully.",
                    newUser.getEmail(),
                    newUser.getOnlineId()));
        }};
        return ResponseEntity.ok().body(response);
    }


    // PLANNER ENDPOINTS
    @GetMapping(path = "planner")
    public ResponseEntity<?> getPlanner(JwtAuthenticationToken token,
                                        @RequestParam Optional<Long> id) throws Exception {
        var plannerId = id.orElse(null);
        Planner planner = this.taskService.getPlanner(token.getName(),plannerId);

        Map<String, Object> response = new HashMap<>(){{
            put("message","Successfully retrieved Planner...");
            put("planner",planner);
        }};
        return ResponseEntity.ok().body(response);
    }

    @GetMapping(path = "planners")
    public ResponseEntity<?> getAllPlanners( JwtAuthenticationToken token,
                                             @RequestHeader String email) throws AuthenticationFailedException
    {
        Set<Planner> planners = this.taskService.getAllPlanners(token.getName());
        Map<String, Object> response = new HashMap<>(){{
            put("message","Successfully retrieved all Planners...");
            put("planners",planners);
        }};
        return ResponseEntity.ok().body(response);
    }

    @PostMapping(path = "planner")
    public ResponseEntity<?> addPlannerToProfile(JwtAuthenticationToken token,
                                                 @RequestBody PlannerRequest request) throws Exception
    {
        request.setOnlineId(token.getName());
        Set<Planner> planners = this.taskService.plannerAdd(request);
        Map<String, Object> response = new HashMap<>(){{
            put("message","Successfully added Planner...");
            put("planners",planners);
        }};
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping(path = "planner")
    public ResponseEntity<?> plannerDel(JwtAuthenticationToken token,
                                        @RequestBody PlannerRequest request) throws Exception
    {
        request.setOnlineId(token.getName());
        Set<Planner> planners = this.taskService.plannerDel(request);
        Map<String, Object> response = new HashMap<>(){{
            put("message","Successfully deleted Planner...");
            put("planners",planners);
        }};
        return ResponseEntity.ok().body(response);
    }

    @PatchMapping(path = "planner")
    public ResponseEntity<?> updatePlanner( JwtAuthenticationToken token,
                                            @RequestBody PlannerRequest request)
            throws MissingRequestValueException, BadRequestException {
        request.setOnlineId(token.getName());
        Planner planner = this.taskService.updatePlanner(request);
        Map<String, Object> response = new HashMap<>(){{
            put("message","Successfully updated Planner...");
            put("planner",planner);
        }};
        return ResponseEntity.ok().body(response);
    }


    // TASK ENDPOINTS
    @PostMapping(path = "task")
    public ResponseEntity<?> addTask(JwtAuthenticationToken token,
                                               @RequestBody TaskRequest request) throws Exception
    {
        request.setOnlineId(token.getName());
        Set<Task> tasks = this.taskService.taskAdd(request);
        Map<String, Object> response = new HashMap<>(){{
            put("message","Successfully added Task...");
            put("tasks",tasks);
        }};
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping(path = "task")
    public ResponseEntity<?> delTask(JwtAuthenticationToken token,
                                     @RequestBody TaskRequest request) throws Exception
    {
        request.setOnlineId(token.getName());
        this.taskService.taskDel(request);
        Map<String, Object> response = new HashMap<>(){{
            put("message","Successfully deleted Task...");
        }};
        return ResponseEntity.ok().body(response);
    }

    @PatchMapping(path = "task")
    public ResponseEntity<?> updateTask(JwtAuthenticationToken token,
                                     @RequestBody TaskRequest request) throws Exception {
        request.setOnlineId(token.getName());
        this.taskService.updateTask(request);
        Map<String, Object> response = new HashMap<>() {{
            put("message", "Successfully updated Task...");
        }};
        return ResponseEntity.ok().body(response);
    }
}
