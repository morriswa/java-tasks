package org.morriswa.taskapp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.morriswa.taskapp.enums.TaskStatus;
import org.morriswa.taskapp.enums.TaskType;

import jakarta.validation.constraints.NotBlank;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class TaskRequest {
    @NotBlank
    private String onlineId;
    private Long taskId;
    private Long plannerId;
    private String title;
    private TaskStatus status;
    private TaskType type;
    private String details;
    private String category;
    private Long startDate;
    private Long dueDate;
    private Long finishDate;
}
