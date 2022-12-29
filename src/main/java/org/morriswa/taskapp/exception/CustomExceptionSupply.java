package org.morriswa.taskapp.exception;

import org.morriswa.taskapp.entity.CustomAuth0User;
import org.morriswa.taskapp.entity.Planner;

import java.util.function.Supplier;

public class CustomExceptionSupply {
    public static Supplier<AuthenticationFailedException> unableToAccessPlannerError(String user_id) {
        return () -> new AuthenticationFailedException(
                String.format("Could not access profile for user: %s",user_id));
    }

    public static Supplier<AuthenticationFailedException> couldNotAuthenticateUserException(String onlineId,
                                                                                            String email)
    {
        return () -> new AuthenticationFailedException(
                String.format("User with ID %s and email %s could not be authenticated.",
                        onlineId,
                        email));
    }

    public static Supplier<BadRequestException> noPlannerFoundException(Long plannerId, CustomAuth0User user) {
        return () -> new BadRequestException(
                String.format("No planner found with ID %s for user %s",
                        plannerId,
                        user.getOnlineId()));
    }
    public static Supplier<BadRequestException> noTaskFoundException(Long taskId, Planner planner) {
        return () -> new BadRequestException(
                String.format("No task found with ID %s within planner %s for user %s",
                        taskId,planner.getName(),
                        planner.getUser().getOnlineId()));
    }
}
