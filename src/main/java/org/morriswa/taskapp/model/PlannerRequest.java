package org.morriswa.taskapp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class PlannerRequest {
    @NotBlank
    private String onlineId;
    private Long plannerId;
    private String name;
    private String goal;
    private Long startDate;
    private Long finishDate;
}
