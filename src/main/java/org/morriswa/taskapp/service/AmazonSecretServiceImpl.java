package org.morriswa.taskapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.morriswa.taskapp.security.AmazonSecretService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Service
public class AmazonSecretServiceImpl extends AmazonSecretService
{
    private final Map<String,Object> secrets;

    @Autowired
    public AmazonSecretServiceImpl(Environment env) throws JsonProcessingException {
        this.secrets = getSecret(
                env.getProperty("aws.secret-name"),
                env.getProperty("aws.region"));
    }

    public String retrieveKey(String key) {
        Object secret = this.secrets.get(key);
        if (Objects.isNull(secret)) {
            throw new NullPointerException(String.format("No secret found with key:%s",key));
        }
        return (String) secret;
    }
}
