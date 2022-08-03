package org.morriswa.taskapp;

import org.morriswa.common.CommonConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import({
        CommonConfig.class
}) @SpringBootApplication
public class TaskApp {
    public static void main(String[] args) {
        SpringApplication.run(TaskApp.class,args);
    }
}
