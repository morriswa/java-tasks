package org.morriswa.taskapp.control;

import lombok.extern.slf4j.Slf4j;
import org.morriswa.taskapp.entity.CustomAuth0User;
import org.morriswa.taskapp.entity.Planner;
import org.morriswa.taskapp.entity.UserProfile;
import org.morriswa.taskapp.exception.AuthenticationFailedException;
import org.morriswa.taskapp.exception.CustomExceptionResponse;
import org.morriswa.taskapp.exception.RegistrationFailedException;
import org.morriswa.taskapp.exception.RequestFailedException;
import org.morriswa.taskapp.service.CustomAuthService;
import org.morriswa.taskapp.service.TaskService;
import org.morriswa.taskapp.validation.VerifyJWTScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings("unused")
@RestController
@CrossOrigin
@RequestMapping(path = "${server.path}/") @Slf4j @Validated
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


    // Exception handling
    @ExceptionHandler({
            AccessDeniedException.class, // default exception thrown when @PreAuthorize queries fail...
            AuthenticationFailedException.class // thrown when user ID and email do not match
    })
    public ResponseEntity<?> auth_err(Exception e, WebRequest r) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new CustomExceptionResponse(e.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        for (var violation : ex.getConstraintViolations()) {
            if (violation.getPropertyPath().toString().contains("403")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new CustomExceptionResponse(violation.getMessage()));
            }

            if (violation.getPropertyPath().toString().contains("401")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new CustomExceptionResponse(violation.getMessage()));
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CustomExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> err(Exception e, WebRequest r) {
        return Objects.equals(env.getProperty("server.debug"), "TRUE") ?
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new CustomExceptionResponse(e.getMessage(), e))
                : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new CustomExceptionResponse(e.getMessage()));
    }


    // LOGIN ENDPOINTS
    @PostMapping(path = "login")
//    @VerifyJWT
    public ResponseEntity<?> registerUser(@RequestHeader String email)
            throws RegistrationFailedException, AuthenticationFailedException
    {
        CustomAuth0User newUser = this.authService.registerFlow(
                SecurityContextHolder.getContext().getAuthentication(),email);
        CustomAuth0User confirmNewUser = this.authService.loginFlow(
                SecurityContextHolder.getContext().getAuthentication(),email);

        Map<String, Object> map = new HashMap<>();
        map.put("derived_auth", SecurityContextHolder.getContext().getAuthentication().getName());
        map.put("message", String.format("User with email %s, ID %S registered successfully.",
                confirmNewUser.getEmail(),confirmNewUser.getOnlineId()));

        return ResponseEntity.ok(map);
    }

    @GetMapping(path = "login")
    @VerifyJWTScope(scopes = "read:profile")
    public ResponseEntity<?> login(@RequestHeader String email) throws AuthenticationFailedException
    {
        CustomAuth0User newUser = this.authService.loginFlow(
                SecurityContextHolder.getContext().getAuthentication(), email);
        Map<String, Object> response = new HashMap<>();
        response.put("message",
                String.format(  "User with email %s, ID %S authenticated successfully.",
                                newUser.getEmail(),
                                newUser.getOnlineId()));
        response.put("derived_auth", SecurityContextHolder.getContext().getAuthentication().getName());

        return ResponseEntity.ok().body(response);
    }


    // PROFILE ENDPOINTS
    @GetMapping(path = "profile")
    @VerifyJWTScope(scopes = "read:profile")
    public ResponseEntity<?> getUserProfile(@RequestHeader String email)
            throws AuthenticationFailedException
    {
        CustomAuth0User authenticatedUser = this.authService.loginFlow(
                SecurityContextHolder.getContext().getAuthentication(), email);
        UserProfile profile = this.taskService.profileGet(authenticatedUser);
        return ResponseEntity.ok(profile);
    }

    @PatchMapping(path = "profile")
    @VerifyJWTScope(scopes = {"read:profile","write:profile"})
    public ResponseEntity<?> updateUserProfile(@RequestHeader String email,
                                               @RequestBody Map<String,Object> request) throws Exception
    {
        CustomAuth0User authenticatedUser = this.authService.loginFlow(
                SecurityContextHolder.getContext().getAuthentication(), email);
        UserProfile profile = this.taskService.profileUpdate(authenticatedUser,request);
        return ResponseEntity.ok(profile);
    }


    // PLANNER ENDPOINTS
    @GetMapping(path = "planner")
    @VerifyJWTScope(scopes = "read:profile")
    public ResponseEntity<?> getPlanner(@RequestHeader String email,
                                        @RequestParam Integer planner_id) throws Exception
    {
        CustomAuth0User authenticatedUser = this.authService.loginFlow(
                SecurityContextHolder.getContext().getAuthentication(),email);
        Planner planner = this.taskService.getPlanner(authenticatedUser,Map.of("planner-id",planner_id));
        return ResponseEntity.ok(planner);
    }

    @GetMapping(path = "planners")
    @VerifyJWTScope(scopes = "read:profile")
    public ResponseEntity<?> getAllPlanners(@RequestHeader String email) throws AuthenticationFailedException
    {
        CustomAuth0User authenticatedUser = this.authService.loginFlow(
                SecurityContextHolder.getContext().getAuthentication(),email);
        Set<Planner> planners = this.taskService.getAllPlanners(authenticatedUser);
        return ResponseEntity.ok(planners);
    }

    @PostMapping(path = "planner")
    @VerifyJWTScope(scopes = {"read:profile","write:profile"})
    public ResponseEntity<?> addPlannerToProfile(@RequestHeader String email,
                                                 @RequestBody Map<String,Object> request) throws Exception
    {
        CustomAuth0User authenticatedUser = this.authService.loginFlow(
                SecurityContextHolder.getContext().getAuthentication(),email);
        Set<Planner> planners = this.taskService.plannerAdd(authenticatedUser,request);
        return ResponseEntity.ok(planners);
    }

    @DeleteMapping(path = "planner")
    @VerifyJWTScope(scopes = {"read:profile","write:profile"})
    public ResponseEntity<?> plannerDel(@RequestHeader String email,
                                        @RequestBody Map<String,Object> request) throws Exception
    {
        CustomAuth0User authenticatedUser = this.authService.loginFlow(
                SecurityContextHolder.getContext().getAuthentication(),email);
        Set<Planner> planners = this.taskService.plannerDel(authenticatedUser,request);
        return ResponseEntity.ok(planners);
    }

    @PatchMapping(path = "planner")
    @VerifyJWTScope(scopes = {"read:profile","write:profile"})
    public ResponseEntity<?> updatePlanner(@RequestHeader String email,
                                           @RequestBody Map<String,Object> request)
            throws RequestFailedException, AuthenticationFailedException
    {
        CustomAuth0User authenticatedUser = this.authService.loginFlow(
                SecurityContextHolder.getContext().getAuthentication(),email);
        Planner planner = this.taskService.updatePlanner(authenticatedUser,request);
        return ResponseEntity.ok(planner);
    }


    // TASK ENDPOINTS
    @PostMapping(path = "task")
    @VerifyJWTScope(scopes = {"read:profile","write:profile"})
    public ResponseEntity<?> addTask(@RequestHeader String email,
                                     @RequestBody Map<String,Object> request) throws Exception
    {
        CustomAuth0User authenticatedUser = this.authService.loginFlow(
                SecurityContextHolder.getContext().getAuthentication(),email);
        Planner planner = this.taskService.taskAdd(authenticatedUser,request);
        return ResponseEntity.ok(planner);
    }

    @DeleteMapping(path = "task")
    @VerifyJWTScope(scopes = {"read:profile","write:profile"})
    public ResponseEntity<?> delTask(@RequestHeader String email,
                                     @RequestBody Map<String,Object> request) throws Exception
    {
        CustomAuth0User authenticatedUser = this.authService.loginFlow(
                SecurityContextHolder.getContext().getAuthentication(),email);
        Planner planner = this.taskService.taskDel(authenticatedUser,request);
        return ResponseEntity.ok(planner);
    }

    @PatchMapping(path = "task")
    @VerifyJWTScope(scopes = {"read:profile","write:profile"})
    public ResponseEntity<?> updateTask(@RequestHeader String email,
                                        @RequestBody Map<String,Object> request) throws Exception
    {
        CustomAuth0User authenticatedUser = this.authService.loginFlow(
                SecurityContextHolder.getContext().getAuthentication(),email);
        Planner planner = this.taskService.updateTask(authenticatedUser,request);
        return ResponseEntity.ok(planner);
    }
}
