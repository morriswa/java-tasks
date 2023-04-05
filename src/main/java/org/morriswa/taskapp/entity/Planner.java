package org.morriswa.taskapp.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.GregorianCalendar;


@Entity @Table(name = "planner")
@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class Planner {
    @Id @Column(name = "planner_id")
    @SequenceGenerator(name = "planner_seq")
    @GeneratedValue(generator = "planner_seq", strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank
    @JoinColumn(name = "online_id",nullable = false,updatable = false)
    private String onlineId;

    @NotBlank
    private String name;

    @NotNull
    private GregorianCalendar creationDate = new GregorianCalendar();
    private GregorianCalendar startDate;
    private GregorianCalendar finishDate;
    private String goal = "";
}
