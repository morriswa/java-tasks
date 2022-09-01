package org.morriswa.taskapp.exception;

import org.morriswa.taskapp.entity.CustomAuth0User;
import org.morriswa.taskapp.entity.Planner;

import java.util.function.Supplier;

public class CustomExceptionSupply {
    public static Supplier<RequestFailedException> noPlannerFoundException(Long plannerId, CustomAuth0User user) {
        return () -> new RequestFailedException(
                String.format("No planner found with ID %s for user %s",
                        plannerId,
                        user.getOnlineId()));
    }

    public static Supplier<RequestFailedException> noTaskFoundException(Long taskId, Planner planner) {
        return () -> new RequestFailedException(
                String.format("No task found with ID %s within planner %s for user %s",
                        taskId,planner.getName(),
                        planner.getUser().getOnlineId()));
    }

    public static Supplier<AuthenticationFailedException>
        couldNotAuthenticateUserException(String onlineId,String email)
    {
        return () -> new AuthenticationFailedException(
                String.format("User with ID %s and email %s could not be authenticated.",
                        onlineId,
                        email));
    }
}
