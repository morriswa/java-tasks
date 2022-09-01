package org.morriswa.taskapp.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity @Table(name = "Users")
@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class CustomAuth0User implements Serializable
{
    @Id @Column(name = "user_id",unique = true,nullable = false)
    @SequenceGenerator(name = "user_seq")
    @GeneratedValue(strategy = GenerationType.IDENTITY,generator = "user_seq")
    private Long id;

    @Column(name ="online_id",unique = true,nullable = false)
    private String onlineId;

    @Column(name ="email",unique = true,nullable = false)
    private String email;
}
