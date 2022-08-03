package org.morriswa.taskapp;

import org.morriswa.taskapp.dao.CustomAuth0User;
import org.morriswa.taskapp.dao.UserProfile;
import org.morriswa.taskapp.service.CustomAuthService;
import org.morriswa.taskapp.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController @CrossOrigin @RequestMapping(path = "tasks/dev/")
public class TaskController {
    private final CustomAuthService authService;
    private final TaskService taskService;
    @Autowired
    public TaskController(CustomAuthService a,TaskService s) {
        this.authService = a;
        this.taskService = s;
    }

    @PostMapping(path = "register")
//    @PreAuthorize("hasAuthority('SCOPE_openid')")
    public ResponseEntity<?> registerUser(Principal principal, @RequestHeader String email) {
        try {
            CustomAuth0User newUser = this.authService.registerFlow(principal,email);
            return ResponseEntity.ok(
                    String.format("User with email %s, ID %S registered successfully",
                            newUser.getEmail(),newUser.getOnlineId()));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
        }
    }

    @GetMapping(path = "login")
    @PreAuthorize("hasAuthority('SCOPE_read:profile')")
    public ResponseEntity<?> login(Principal principal, @RequestHeader String email) {
        try {
            CustomAuth0User newUser = this.authService.loginFlow(principal,email);
            return ResponseEntity.ok(
                    String.format("User with email %s, ID %S logged in successfully",
                            newUser.getEmail(),newUser.getOnlineId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
        }
    }

    @GetMapping(path = "profile") @PreAuthorize("hasAuthority('SCOPE_read:profile')")
    public ResponseEntity<?> getUserProfile(Principal principal, @RequestHeader String email) {
        try {
            CustomAuth0User authenticatedUser = this.authService.loginFlow(principal,email);
            UserProfile profile = this.taskService.getUserProfile(authenticatedUser);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
        }
    }

    @PostMapping(path = "profile/planner/add")
    public ResponseEntity<?> addPlannerToProfile(Principal principal,
                                                 @RequestHeader String email,
                                                 @RequestBody Map<String,Object> request) {
        try {
            CustomAuth0User authenticatedUser = this.authService.loginFlow(principal,email);
            UserProfile profile = this.taskService.newPlanner(authenticatedUser,request);
            return ResponseEntity.ok(profile);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
        }
    }

    @PatchMapping(path = "profile/update")
    public ResponseEntity<?> updateUserProfile(Principal principal,
                                               @RequestHeader String email,
                                               @RequestBody Map<String,Object> request) {
        try {
            CustomAuth0User authenticatedUser = this.authService.loginFlow(principal,email);
            UserProfile profile = this.taskService.updateUserProfile(authenticatedUser,request);
            return ResponseEntity.ok(profile);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
        }
    }

    @PatchMapping(path = "profile/task/add")
    public ResponseEntity<?> addTask(Principal principal,
                                               @RequestHeader String email,
                                               @RequestBody Map<String,Object> request) {
        try {
            CustomAuth0User authenticatedUser = this.authService.loginFlow(principal,email);
            UserProfile profile = this.taskService.updatePlannerWithNewTask(authenticatedUser,request);
            return ResponseEntity.ok(profile);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
        }
    }

    @PatchMapping(path = "profile/task/del")
    public ResponseEntity<?> delTask(Principal principal,
                                     @RequestHeader String email,
                                     @RequestBody Map<String,Object> request) {
        try {
            CustomAuth0User authenticatedUser = this.authService.loginFlow(principal,email);
            UserProfile profile = this.taskService.deleteTaskInPlanner(authenticatedUser,request);
            return ResponseEntity.ok(profile);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
        }
    }

    @PatchMapping(path = "profile/task/update")
    public ResponseEntity<?> updateTask(Principal principal,
                                     @RequestHeader String email,
                                     @RequestBody Map<String,Object> request) {
        try {
            CustomAuth0User authenticatedUser = this.authService.loginFlow(principal,email);
            UserProfile profile = this.taskService.updateTaskStatus(authenticatedUser,request);
            return ResponseEntity.ok(profile);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
        }
    }
}
