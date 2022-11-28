package org.morriswa.taskapp.exception;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.GregorianCalendar;

@NoArgsConstructor @Data
public class CustomExceptionResponse {
    private String timestamp = GregorianCalendar.getInstance().getTime().toString();
    private String message;
    private Object trace;

    public CustomExceptionResponse(String message) {
        this.message = message;
    }

    public CustomExceptionResponse(String message, Object trace) {
        this.message = message;
        this.trace = trace;
    }
}
