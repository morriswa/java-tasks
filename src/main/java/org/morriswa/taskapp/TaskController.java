package org.morriswa.taskapp;

import org.morriswa.taskapp.entity.CustomAuth0User;
import org.morriswa.taskapp.entity.Planner;
import org.morriswa.taskapp.entity.UserProfile;
import org.morriswa.taskapp.service.CustomAuthService;
import org.morriswa.taskapp.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
@RestController @CrossOrigin
@RequestMapping(path = "tasks/dev/")
public class TaskController {
    private final CustomAuthService authService;
    private final TaskService taskService;
    @Autowired
    public TaskController(CustomAuthService a,TaskService s) {
        this.authService = a;
        this.taskService = s;
    }


    // LOGIN ENDPOINTS
    @PostMapping(path = "login/register")
//    @PreAuthorize("hasAuthority('SCOPE_openid')")
    public ResponseEntity<?> registerUser(Principal principal, @RequestHeader String email) {
        try {
            CustomAuth0User newUser = this.authService.registerFlow(principal,email);
            CustomAuth0User confirmNewUser = this.authService.loginFlow(principal,email);
            return ResponseEntity.ok(
                    String.format("User with email %s, ID %S registered successfully.",
                            confirmNewUser.getEmail(),confirmNewUser.getOnlineId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
        }
    }

    @PatchMapping(path = "profile/update")
    public ResponseEntity<?> updateUserProfile(Principal principal,
                                               @RequestHeader String email,
                                               @RequestBody Map<String,Object> request) {
        try {
            CustomAuth0User authenticatedUser = this.authService.loginFlow(principal,email);
            UserProfile profile = this.taskService.profileUpdate(authenticatedUser,request);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
        }
    }

    @GetMapping(path = "planner/all")
    public ResponseEntity<?> getAllPlanners( Principal principal,
                                             @RequestHeader String email) {
        try {
            CustomAuth0User authenticatedUser = this.authService.loginFlow(principal,email);
            Set<Planner> planners = this.taskService.getAllPlanners(authenticatedUser);
            return ResponseEntity.ok(planners);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
        }
    }

    @PostMapping(path = "planner/add")
    public ResponseEntity<?> addPlannerToProfile(Principal principal,
                                                 @RequestHeader String email,
                                                 @RequestBody Map<String,Object> request) {
        try {
            CustomAuth0User authenticatedUser = this.authService.loginFlow(principal,email);
            Set<Planner> planners = this.taskService.plannerAdd(authenticatedUser,request);
            return ResponseEntity.ok(planners);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
        }
    }

    @DeleteMapping(path = "planner/del")
    public ResponseEntity<?> plannerDel(Principal principal,
                                                 @RequestHeader String email,
                                                 @RequestBody Map<String,Object> request) {
        try {
            CustomAuth0User authenticatedUser = this.authService.loginFlow(principal,email);
            Set<Planner> planners = this.taskService.plannerDel(authenticatedUser,request);
            return ResponseEntity.ok(planners);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
        }
    }

    @PatchMapping(path = "planner/update")
    public ResponseEntity<?> updatePlanner(Principal principal,
                                        @RequestHeader String email,
                                        @RequestBody Map<String,Object> request) {
        try {
            CustomAuth0User authenticatedUser = this.authService.loginFlow(principal,email);
            Planner planner = this.taskService.updatePlanner(authenticatedUser,request);
            return ResponseEntity.ok(planner);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
        }
    }


    // TASK ENDPOINTS
    @PostMapping(path = "task/add")
    public ResponseEntity<?> addTask(Principal principal,
                                               @RequestHeader String email,
                                               @RequestBody Map<String,Object> request) {
        try {
            CustomAuth0User authenticatedUser = this.authService.loginFlow(principal,email);
            Planner planner = this.taskService.taskAdd(authenticatedUser,request);
            return ResponseEntity.ok(planner);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
        }
    }

    @DeleteMapping(path = "task/del")
    public ResponseEntity<?> delTask(Principal principal,
                                     @RequestHeader String email,
                                     @RequestBody Map<String,Object> request) {
        try {
            CustomAuth0User authenticatedUser = this.authService.loginFlow(principal,email);
            Planner planner = this.taskService.taskDel(authenticatedUser,request);
            return ResponseEntity.ok(planner);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
        }
    }

    @PatchMapping(path = "task/update")
    public ResponseEntity<?> updateTask(Principal principal,
                                     @RequestHeader String email,
                                     @RequestBody Map<String,Object> request) {
        try {
            CustomAuth0User authenticatedUser = this.authService.loginFlow(principal,email);
            Planner planner = this.taskService.updateTask(authenticatedUser,request);
            return ResponseEntity.ok(planner);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
        }
    }

}
