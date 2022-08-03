package org.morriswa.taskapp.exception;

public class AuthenticationFailedException extends Exception {
    public AuthenticationFailedException(String format) {
        super(format);
    }
}
