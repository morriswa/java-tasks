package org.morriswa.taskapp.config;

import org.morriswa.starter.service.util.AmazonSecretService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class CustomDatasourceConfig {
    @Autowired
    private AmazonSecretService ss;

    @Value("${spring.profiles.active}")
    private String SPRING_PROFILES_ACTIVE;

    @Bean
    public DataSource getDataSource() {
        return switch (SPRING_PROFILES_ACTIVE) {
            case "local" -> DataSourceBuilder.create()
                    .url(String.format("%s://%s:%s/%s",
                            ss.retrieveKey("datasource.scheme"),
                            ss.retrieveKey("datasource.path"),
                            ss.retrieveKey("datasource.port"),
                            ss.retrieveKey("datasource.database.name"))).build();
            case "develop","devapi","prod" -> DataSourceBuilder.create()
                    .username(ss.retrieveKey("datasource.database.user.name"))
                    .password(ss.retrieveKey("datasource.database.password"))
                    .url(String.format("%s://%s:%s/%s",
                            ss.retrieveKey("datasource.scheme"),
                            ss.retrieveKey("datasource.path"),
                            ss.retrieveKey("datasource.port"),
                            ss.retrieveKey("datasource.database.name"))
                    ).build();
            default -> throw new RuntimeException("DATASOURCE COULD NOT BE CONFIGURED, PLEASE CHECK CONFIG <3");
        };
    }
}
