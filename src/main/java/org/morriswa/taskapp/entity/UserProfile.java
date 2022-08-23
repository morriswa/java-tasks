package org.morriswa.taskapp.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity @Table(name = "Profiles")
@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class UserProfile {
    @OneToOne(optional = false)
    @JoinColumn(name = "user_id",referencedColumnName = "user_id")
    private CustomAuth0User user;

    @Id @Column(name = "user_profile_id")
    @SequenceGenerator(name = "user_profile_seq")
    @GeneratedValue(strategy = GenerationType.AUTO,generator = "user_profile_seq")
    private Long id;
    private String nameFirst;
    private String nameMiddle;
    private String nameLast;
    private String displayName;
    private String pronouns;

    public UserProfile(CustomAuth0User user) {
        this.user = user;
        this.nameFirst = "";
        this.nameMiddle = "";
        this.nameLast = "";
        this.displayName = "";
        this.pronouns = "";
    }
}
