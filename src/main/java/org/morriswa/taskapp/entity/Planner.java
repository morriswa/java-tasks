package org.morriswa.taskapp.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
