package org.morriswa.taskapp.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class JWTScopeValidator implements ConstraintValidator<VerifyJWTScope,Object> {
    private Set<String> scopes;

    @Override
    public void initialize(VerifyJWTScope constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        scopes = Arrays.stream(constraintAnnotation.scopes()).collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(Object authorization_header, ConstraintValidatorContext constraintValidatorContext) {
        List<String> required_scopes = new ArrayList<>(scopes);

        var jwt_authorities =
                SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getAuthorities();

        for (var granted : jwt_authorities) {
            required_scopes.remove(granted.getAuthority());
        }

        if (required_scopes.isEmpty()) {
            return true;
        } else {
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(
                            String.format("Could not locate authority %s in jwt...",required_scopes))
                    .addPropertyNode("error").addBeanNode().inIterable().atKey("403")
                    .addConstraintViolation();
            return false;
        }

    }
}
