package org.morriswa.taskapp.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Map;

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
        if (this.secrets.containsKey(key)) {
            return (String) this.secrets.get(key);
        }
        throw new NullPointerException(String.format("No secret found with key:%s",key));
    }
}
