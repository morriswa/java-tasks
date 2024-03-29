package org.morriswa.taskapp.exception;

import org.morriswa.common.model.BadRequestException;
import org.morriswa.taskapp.entity.UserProfile;
import org.morriswa.taskapp.entity.Planner;

import java.util.function.Supplier;

public class TaskApiExceptionSupply {
    public static Supplier<BadRequestException> noPlannerFoundException(Long plannerId, UserProfile user) {
        return () -> new BadRequestException(
                String.format("No planner found with ID %s for user %s",
                        plannerId,
                        user.getOnlineId()));
    }

    public static Supplier<BadRequestException> noPlannerFoundException(Long plannerId, String onlineId) {
        return () -> new BadRequestException(
                String.format("No planner found with ID %s for user %s",
                        plannerId,
                        onlineId));
    }

    public static Supplier<BadRequestException> noTaskFoundException(Long taskId, Planner planner) {
        return () -> new BadRequestException(
                String.format("No task found with ID %s within planner %s for user %s",
                        taskId,planner.getName(),
                        planner.getOnlineId()));
    }

    public static Supplier<BadRequestException> noTaskFoundException(Long taskId, String onlineId) {
        return () -> new BadRequestException(
                String.format(
                        "No task found with ID %s for user %s",
                        taskId,
                        onlineId
                ));
    }
}
