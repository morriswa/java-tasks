package org.morriswa.taskapp.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

//    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9_.-]*$")
    @Length(max=16)
    @Column(name = "display_name",unique = true)
    private String displayName;
    private String pronouns;
}
