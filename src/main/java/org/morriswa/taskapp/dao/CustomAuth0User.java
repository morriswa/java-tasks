package org.morriswa.taskapp.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity @Table(name = "users")
@Data @AllArgsConstructor @NoArgsConstructor
public class CustomAuth0User implements Serializable
{
    @Id @Column(name = "db_id",unique = true,nullable = false)
    @SequenceGenerator(name = "db_id_seq")
    @GeneratedValue(strategy = GenerationType.AUTO,generator = "db_id_seq")
    private Long id;

    @Column(name ="online_id",unique = true,nullable = false)
    private String onlineId;

    @Column(name ="email",unique = true,nullable = false)
    private String email;

    public CustomAuth0User(String onlineId, String email) {
        this.onlineId = onlineId;
        this.email = email;
    }
}
