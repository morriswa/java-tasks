package org.morriswa.taskapp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class UserProfileRequest {
    @NotBlank
    private String onlineId;
    private String email;

    @Length(max = 20)
    private String nameFirst;

    @Length(max = 20)
    private String nameMiddle;

    @Length(max = 40)
    private String nameLast;

    @Pattern(regexp = "^[a-zA-Z0-9_.-]*$")
    @Length(max=16)
    private String displayName;
    private String pronouns;
}
