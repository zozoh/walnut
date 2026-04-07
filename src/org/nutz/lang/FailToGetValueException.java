package org.nutz.lang;


public class FailToGetValueException extends RuntimeException {

    public FailToGetValueException(String message, Throwable e) {
        super(message, e);
    }

}
