package org.morriswa.taskapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.morriswa.common.security.AmazonSecretService;
import org.springframework.core.env.Environment;

import java.util.Map;
import java.util.Objects;

public class AmazonSecretServiceImpl extends AmazonSecretService
{
    private final Map<String,Object> secrets;

    public AmazonSecretServiceImpl(Environment env) throws JsonProcessingException {
        this.secrets = getSecret(
                env.getProperty("aws.secret-name"),
                env.getProperty("aws.region"));
    }

    public String retriveKey(String key) {
        Object secret = this.secrets.get(key);
        if (Objects.isNull(secret)) {
            throw new NullPointerException(String.format("No secret found with key:%s",key));
        }
        return (String) secret;
    }
}
