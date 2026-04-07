package org.nutz.lang.socket;


public class CloseSocketException extends RuntimeException {

    public CloseSocketException() {
        super();
    }

    public CloseSocketException(String msg) {
        super(msg);
    }
}
