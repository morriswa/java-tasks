package org.morriswa.taskapp.exception;

public class BadRequestException extends Exception {
    public BadRequestException(String format) {
        super(format);
    }
}

