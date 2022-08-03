package org.morriswa.taskapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.Principal;

@SpringBootTest
public class TestSuite {
    public static Principal getPrincipal() {
        return new Principal() {
            @Override
            public String getName() {
                return "LTX8oCMsWR0IamtmnGFOzJKlMogMpg0r@clients";
            }
        };
    }

    @Test void runItBack() {
    }
}
