package org.morriswa.taskapp.control;

import org.morriswa.taskapp.entity.UserProfile;
import org.morriswa.taskapp.entity.Planner;
import org.morriswa.taskapp.exception.AuthenticationFailedException;
import org.morriswa.taskapp.exception.BadRequestException;
import org.morriswa.taskapp.exception.RegistrationFailedException;
import org.morriswa.taskapp.model.PlannerRequest;
import org.morriswa.taskapp.model.TaskRequest;
import org.morriswa.taskapp.service.CustomAuthService;
import org.morriswa.taskapp.service.TaskService;
import org.morriswa.taskapp.service.TaskServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
@RestController @CrossOrigin
@RequestMapping(path = "${server.path}")
public class TaskController {
    private final TaskService taskService;
    @Autowired
    public TaskController(TaskService s, Environment e) {
        this.taskService = s;
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

//    @ExceptionHandler({
//            AccessDeniedException.class, // default exception thrown when @PreAuthorize queries fail...
//    })
//    public ResponseEntity<?> forbidden(Exception e, WebRequest r) {
//        Map<String, Object> response = new HashMap<>(){{
//            put("error", "Insufficient Scope...");
//            put("message", e.getMessage());
//            put("timestamp", new GregorianCalendar().toZonedDateTime().toString());
//        }};
//        return ResponseEntity.status(403).body(response);
//    }

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
        UserProfile newUser = this.authService.registerFlow(principal,email);
        UserProfile confirmNewUser = this.authService.loginFlow(principal,email);

        Map<String, Object> response = new HashMap<>(){{
           put("message",
                   String.format("User with email %s, ID %S registered successfully.",
                   confirmNewUser.getEmail(),
                   confirmNewUser.getOnlineId()));
        }};
        return ResponseEntity.ok().body(response);
    }

    @GetMapping(path = "login")
    public ResponseEntity<?> login(Principal principal, @RequestHeader String email)
            throws AuthenticationFailedException
    {
        UserProfile newUser = this.authService.loginFlow(principal,email);

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

        UserProfile authenticatedUser = this.authService.loginFlow(principal,email);
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
        UserProfile authenticatedUser = this.authService.loginFlow(principal,email);
        UserProfile profile = this.taskService.profileUpdate(authenticatedUser,request);

        Map<String, Object> response = new HashMap<>(){{
            put("message","Successfully updated UserProfile...");
            put("profile",profile);
        }};
        return ResponseEntity.ok().body(response);
    }


    // PLANNER ENDPOINTS
    @GetMapping(path = "planner")
    public ResponseEntity<?> getPlanner(JwtAuthenticationToken token,
                                        @RequestParam Long id) throws Exception {
        Planner planner = this.taskService.getPlanner(token.getName(),id);

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
        Planner planner = this.taskService.taskAdd(request);
        Map<String, Object> response = new HashMap<>(){{
            put("message","Successfully added Task...");
            put("planner",planner);
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
