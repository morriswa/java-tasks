package org.morriswa.taskapp.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD,ElementType.PARAMETER,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = JWTScopeValidator.class)
public @interface VerifyJWTScope {
    String message() default "Invalid JWT...";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String[] scopes() default {};
}
