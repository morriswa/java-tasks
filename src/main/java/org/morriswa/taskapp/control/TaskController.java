package org.morriswa.taskapp.control;

import org.morriswa.taskapp.entity.CustomAuth0User;
import org.morriswa.taskapp.entity.Planner;
import org.morriswa.taskapp.entity.UserProfile;
import org.morriswa.taskapp.service.CustomAuthService;
import org.morriswa.taskapp.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings("unused")
@RestController @CrossOrigin
@RequestMapping(path = "${server.path}")
public class TaskController {
    private final CustomAuthService authService;
    private final TaskService taskService;
    private final Environment env;
    @Autowired
    public TaskController(CustomAuthService a,TaskService s,Environment e) {
        this.authService = a;
        this.taskService = s;
        this.env = e;
    }


    // LOGIN ENDPOINTS
    @PostMapping(path = "login")
    public ResponseEntity<?> registerUser(Principal principal, @RequestHeader String email) {
        try {
            CustomAuth0User newUser = this.authService.registerFlow(principal,email);
            CustomAuth0User confirmNewUser = this.authService.loginFlow(principal,email);
            return ResponseEntity.ok(
                    String.format("User with email %s, ID %S registered successfully.",
                            confirmNewUser.getEmail(),confirmNewUser.getOnlineId()));
        } catch (Exception e) {
            return Objects.equals(env.getProperty("server.debug"), "TRUE") ?
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e)
                    : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping(path = "login")
    @PreAuthorize("hasAuthority('SCOPE_read:profile')")
    public ResponseEntity<?> login(Principal principal, @RequestHeader String email) {
        try {
            CustomAuth0User newUser = this.authService.loginFlow(principal,email);
            return ResponseEntity.ok().body(
                    String.format("User with email %s, ID %S authenticated successfully.",
                            newUser.getEmail(),newUser.getOnlineId()));
        } catch (Exception e) {
            return Objects.equals(env.getProperty("server.debug"), "TRUE") ?
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e)
                    : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    // PROFILE ENDPOINTS
    @GetMapping(path = "profile") @PreAuthorize("hasAuthority('SCOPE_read:profile')")
    public ResponseEntity<?> getUserProfile(Principal principal, @RequestHeader String email) {
        try {
            CustomAuth0User authenticatedUser = this.authService.loginFlow(principal,email);
            UserProfile profile = this.taskService.profileGet(authenticatedUser);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return Objects.equals(env.getProperty("server.debug"), "TRUE") ?
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e)
                    : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PatchMapping(path = "profile")
    public ResponseEntity<?> updateUserProfile(Principal principal,
                                               @RequestHeader String email,
                                               @RequestBody Map<String,Object> request) {
        try {
            CustomAuth0User authenticatedUser = this.authService.loginFlow(principal,email);
            UserProfile profile = this.taskService.profileUpdate(authenticatedUser,request);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return Objects.equals(env.getProperty("server.debug"), "TRUE") ?
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e)
                    : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    // PLANNER ENDPOINTS
    @GetMapping(path = "planner")
    public ResponseEntity<?> getPlanner( Principal principal,
                                         @RequestHeader String email,
                                         @RequestParam Integer planner_id) {
        try {
            CustomAuth0User authenticatedUser = this.authService.loginFlow(principal,email);
            Planner planner = this.taskService.getPlanner(authenticatedUser,Map.of("planner-id",planner_id));
            return ResponseEntity.ok(planner);
        } catch (Exception e) {
            return Objects.equals(env.getProperty("server.debug"), "TRUE") ?
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e)
                    : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping(path = "planners")
    public ResponseEntity<?> getAllPlanners( Principal principal,
                                             @RequestHeader String email) {
        try {
            CustomAuth0User authenticatedUser = this.authService.loginFlow(principal,email);
            Set<Planner> planners = this.taskService.getAllPlanners(authenticatedUser);
            return ResponseEntity.ok(planners);
        } catch (Exception e) {
            return Objects.equals(env.getProperty("server.debug"), "TRUE") ?
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e)
                    : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping(path = "planner")
    public ResponseEntity<?> addPlannerToProfile(Principal principal,
                                                 @RequestHeader String email,
                                                 @RequestBody Map<String,Object> request) {
        try {
            CustomAuth0User authenticatedUser = this.authService.loginFlow(principal,email);
            Set<Planner> planners = this.taskService.plannerAdd(authenticatedUser,request);
            return ResponseEntity.ok(planners);
        } catch (Exception e) {
            return Objects.equals(env.getProperty("server.debug"), "TRUE") ?
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e)
                    : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping(path = "planner")
    public ResponseEntity<?> plannerDel(Principal principal,
                                                 @RequestHeader String email,
                                                 @RequestBody Map<String,Object> request) {
        try {
            CustomAuth0User authenticatedUser = this.authService.loginFlow(principal,email);
            Set<Planner> planners = this.taskService.plannerDel(authenticatedUser,request);
            return ResponseEntity.ok(planners);
        } catch (Exception e) {
            return Objects.equals(env.getProperty("server.debug"), "TRUE") ?
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e)
                    : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PatchMapping(path = "planner")
    public ResponseEntity<?> updatePlanner(Principal principal,
                                        @RequestHeader String email,
                                        @RequestBody Map<String,Object> request) {
        try {
            CustomAuth0User authenticatedUser = this.authService.loginFlow(principal,email);
            Planner planner = this.taskService.updatePlanner(authenticatedUser,request);
            return ResponseEntity.ok(planner);
        } catch (Exception e) {
            return Objects.equals(env.getProperty("server.debug"), "TRUE") ?
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e)
                    : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    // TASK ENDPOINTS
    @PostMapping(path = "task")
    public ResponseEntity<?> addTask(Principal principal,
                                               @RequestHeader String email,
                                               @RequestBody Map<String,Object> request) {
        try {
            CustomAuth0User authenticatedUser = this.authService.loginFlow(principal,email);
            Planner planner = this.taskService.taskAdd(authenticatedUser,request);
            return ResponseEntity.ok(planner);
        } catch (Exception e) {
            return Objects.equals(env.getProperty("server.debug"), "TRUE") ?
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e)
                    : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping(path = "task")
    public ResponseEntity<?> delTask(Principal principal,
                                     @RequestHeader String email,
                                     @RequestBody Map<String,Object> request) {
        try {
            CustomAuth0User authenticatedUser = this.authService.loginFlow(principal,email);
            Planner planner = this.taskService.taskDel(authenticatedUser,request);
            return ResponseEntity.ok(planner);
        } catch (Exception e) {
            return Objects.equals(env.getProperty("server.debug"), "TRUE") ?
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e)
                    : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PatchMapping(path = "task")
    public ResponseEntity<?> updateTask(Principal principal,
                                     @RequestHeader String email,
                                     @RequestBody Map<String,Object> request) {
        try {
            CustomAuth0User authenticatedUser = this.authService.loginFlow(principal,email);
            Planner planner = this.taskService.updateTask(authenticatedUser,request);
            return ResponseEntity.ok(planner);
        } catch (Exception e) {
            return Objects.equals(env.getProperty("server.debug"), "TRUE") ?
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e)
                    : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
