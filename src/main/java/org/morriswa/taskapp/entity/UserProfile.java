package org.morriswa.taskapp.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Entity @Table(name = "profile")
@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class UserProfile implements Serializable
{
    @Id
    @Column(name ="online_id",unique = true,nullable = false,updatable = false)
    private String onlineId;

    @NotBlank
    @Email
    @Column(name ="email",unique = true,nullable = false)
    private String email;

    @Length(max = 20)
    private String nameFirst;

    @Length(max = 20)
    private String nameMiddle;

    @Length(max = 40)
    private String nameLast;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9_.-]*$")
    @Length(max=16)
    @Column(name = "display_name",unique = true, nullable = false)
    private String displayName;
    private String pronouns;
}
