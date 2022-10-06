package org.morriswa.taskapp.entity;

import org.morriswa.taskapp.service.AmazonSecretServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

@Configuration
public class CustomDatasourceConfig {
    @Autowired
    private Environment env;
    @Autowired
    private AmazonSecretServiceImpl ss;

    @Bean
    public DataSource getDataSource() {

        return "prod".equals(env.getProperty("spring.profiles.active"))?
                DataSourceBuilder.create()
                .username(ss.retrieveKey("dbuser"))
                .password(ss.retrieveKey("dbpass"))
                .url(
                        String.format("jdbc:postgresql://%s:%s/%s",
                                ss.retrieveKey("dburi"),
                                5432,
                                ss.retrieveKey("dbname"))
                ).build()
                : DataSourceBuilder.create()
                .url(env.getProperty("spring.datasource.url")).build();

    }
}
