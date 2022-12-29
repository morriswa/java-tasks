package org.morriswa.taskapp.control;

import org.morriswa.taskapp.entity.CustomAuth0User;
import org.morriswa.taskapp.entity.Planner;
import org.morriswa.taskapp.entity.UserProfile;
import org.morriswa.taskapp.exception.AuthenticationFailedException;
import org.morriswa.taskapp.exception.BadRequestException;
import org.morriswa.taskapp.exception.RegistrationFailedException;
import org.morriswa.taskapp.service.CustomAuthService;
import org.morriswa.taskapp.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.security.Principal;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> internalServerError(Exception e, WebRequest r) {
        boolean include_stack = "TRUE".equals(env.getProperty("server.debug"));
        Map<String, Object> response = new HashMap<>(){{
            put("error", e.getMessage());
            if (include_stack) {
                put("stack",e);
            }
        }};

        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler({
            AuthenticationFailedException.class // thrown when user ID and email do not match
    })
    public ResponseEntity<?> unauthorized(Exception e, WebRequest r) {
        Map<String, Object> response = new HashMap<>(){{
            put("error", e.getMessage());
        }};
        return ResponseEntity.status(401).body(response);
    }

    @ExceptionHandler({
            AccessDeniedException.class, // default exception thrown when @PreAuthorize queries fail...
    })
    public ResponseEntity<?> forbidden(Exception e, WebRequest r) {
        Map<String, Object> response = new HashMap<>(){{
            put("error", "Insufficient Scope...");
            put("message", e.getMessage());
            put("timestamp", new GregorianCalendar().toZonedDateTime().toString());
        }};
        return ResponseEntity.status(403).body(response);
    }

    @ExceptionHandler({
            BadRequestException.class,
            MissingRequestValueException.class
    })
    public ResponseEntity<?> badRequest(Exception e, WebRequest r) {
        boolean include_stack = "TRUE".equals(env.getProperty("server.debug"));
        Map<String, Object> response = new HashMap<>(){{
            put("error", e.getMessage());
            if (include_stack) {
                put("stack",e);
            }
        }};

        return ResponseEntity.badRequest().body(response);
    }


    // LOGIN ENDPOINTS
    @PostMapping(path = "login")
    public ResponseEntity<?> registerUser(Principal principal, @RequestHeader String email)
            throws RegistrationFailedException, AuthenticationFailedException
    {
        CustomAuth0User newUser = this.authService.registerFlow(principal,email);
        CustomAuth0User confirmNewUser = this.authService.loginFlow(principal,email);

        Map<String, Object> response = new HashMap<>(){{
           put("message",
                   String.format("User with email %s, ID %S registered successfully.",
                   confirmNewUser.getEmail(),
                   confirmNewUser.getOnlineId()));
        }};
        return ResponseEntity.ok().body(response);
    }

    @GetMapping(path = "login")
    @PreAuthorize("hasAuthority('SCOPE_read:profile')")
    public ResponseEntity<?> login(Principal principal, @RequestHeader String email)
            throws AuthenticationFailedException
    {
        CustomAuth0User newUser = this.authService.loginFlow(principal,email);

        Map<String, Object> response = new HashMap<>(){{
            put("message",
                    String.format("User with email %s, ID %S authenticated successfully.",
                    newUser.getEmail(),
                    newUser.getOnlineId()));
        }};
        return ResponseEntity.ok().body(response);
    }


    // PROFILE ENDPOINTS
    @GetMapping(path = "profile")
    public ResponseEntity<?> getUserProfile(Principal principal, @RequestHeader String email)
            throws AuthenticationFailedException {

        CustomAuth0User authenticatedUser = this.authService.loginFlow(principal,email);
        UserProfile profile = this.taskService.profileGet(authenticatedUser);

        Map<String, Object> response = new HashMap<>(){{
            put("message","Successfully retrieved UserProfile...");
            put("profile",profile);
        }};
        return ResponseEntity.ok().body(response);
    }

    @PatchMapping(path = "profile")
    public ResponseEntity<?> updateUserProfile(Principal principal,
                                               @RequestHeader String email,
                                               @RequestBody Map<String,Object> request)
            throws AuthenticationFailedException
    {
        CustomAuth0User authenticatedUser = this.authService.loginFlow(principal,email);
        UserProfile profile = this.taskService.profileUpdate(authenticatedUser,request);

        Map<String, Object> response = new HashMap<>(){{
            put("message","Successfully updated UserProfile...");
            put("profile",profile);
        }};
        return ResponseEntity.ok().body(response);
    }


    // PLANNER ENDPOINTS
    @GetMapping(path = "planner")
    public ResponseEntity<?> getPlanner(Principal principal,
                                        @RequestHeader String email,
                                        @RequestParam Integer id) throws Exception {
        CustomAuth0User authenticatedUser = this.authService.loginFlow(principal,email);
        Planner planner = this.taskService.getPlanner(authenticatedUser,Map.of("planner-id",id));

        Map<String, Object> response = new HashMap<>(){{
            put("message","Successfully retrieved Planner...");
            put("planner",planner);
        }};
        return ResponseEntity.ok().body(response);
    }

    @GetMapping(path = "planners")
    public ResponseEntity<?> getAllPlanners( Principal principal,
                                             @RequestHeader String email) throws AuthenticationFailedException
    {
        CustomAuth0User authenticatedUser = this.authService.loginFlow(principal,email);
        Set<Planner> planners = this.taskService.getAllPlanners(authenticatedUser);
        Map<String, Object> response = new HashMap<>(){{
            put("message","Successfully retrieved all Planners...");
            put("planners",planners);
        }};
        return ResponseEntity.ok().body(response);
    }

    @PostMapping(path = "planner")
    public ResponseEntity<?> addPlannerToProfile(Principal principal,
                                                 @RequestHeader String email,
                                                 @RequestBody Map<String,Object> request) throws Exception
    {
        CustomAuth0User authenticatedUser = this.authService.loginFlow(principal,email);
        Set<Planner> planners = this.taskService.plannerAdd(authenticatedUser,request);
        Map<String, Object> response = new HashMap<>(){{
            put("message","Successfully added Planner...");
            put("planners",planners);
        }};
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping(path = "planner")
    public ResponseEntity<?> plannerDel(Principal principal,
                                                 @RequestHeader String email,
                                                 @RequestBody Map<String,Object> request) throws Exception
    {
        CustomAuth0User authenticatedUser = this.authService.loginFlow(principal,email);
        Set<Planner> planners = this.taskService.plannerDel(authenticatedUser,request);
        Map<String, Object> response = new HashMap<>(){{
            put("message","Successfully deleted Planner...");
            put("planners",planners);
        }};
        return ResponseEntity.ok().body(response);
    }

    @PatchMapping(path = "planner")
    public ResponseEntity<?> updatePlanner(Principal principal,
                                        @RequestHeader String email,
                                        @RequestBody Map<String,Object> request)
            throws AuthenticationFailedException, MissingRequestValueException, BadRequestException {
        CustomAuth0User authenticatedUser = this.authService.loginFlow(principal,email);
        Planner planner = this.taskService.updatePlanner(authenticatedUser,request);
        Map<String, Object> response = new HashMap<>(){{
            put("message","Successfully updated Planner...");
            put("planner",planner);
        }};
        return ResponseEntity.ok().body(response);
    }


    // TASK ENDPOINTS
    @PostMapping(path = "task")
    public ResponseEntity<?> addTask(Principal principal,
                                               @RequestHeader String email,
                                               @RequestBody Map<String,Object> request) throws Exception
    {
        CustomAuth0User authenticatedUser = this.authService.loginFlow(principal,email);
        Planner planner = this.taskService.taskAdd(authenticatedUser,request);
        Map<String, Object> response = new HashMap<>(){{
            put("message","Successfully added Task...");
            put("planner",planner);
        }};
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping(path = "task")
    public ResponseEntity<?> delTask(Principal principal,
                                     @RequestHeader String email,
                                     @RequestBody Map<String,Object> request) throws Exception
    {
        CustomAuth0User authenticatedUser = this.authService.loginFlow(principal,email);
        Planner planner = this.taskService.taskDel(authenticatedUser,request);
        Map<String, Object> response = new HashMap<>(){{
            put("message","Successfully deleted Task...");
            put("planner",planner);
        }};
        return ResponseEntity.ok().body(response);
    }

    @PatchMapping(path = "task")
    public ResponseEntity<?> updateTask(Principal principal,
                                     @RequestHeader String email,
                                     @RequestBody Map<String,Object> request) throws Exception {
        CustomAuth0User authenticatedUser = this.authService.loginFlow(principal, email);
        Planner planner = this.taskService.updateTask(authenticatedUser, request);
        Map<String, Object> response = new HashMap<>() {{
            put("message", "Successfully updated Task...");
            put("planner", planner);
        }};
        return ResponseEntity.ok().body(response);
    }
}
