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

        return DataSourceBuilder.create()
                .username(ss.retriveKey("dbuser"))
                .password(ss.retriveKey("dbpass"))
                .url(
                        String.format("jdbc:postgresql://%s:%s/%s",
                                ss.retriveKey("dburi"),
                                5432,
                                ss.retriveKey("dbname"))
                ).build();

    }
}
