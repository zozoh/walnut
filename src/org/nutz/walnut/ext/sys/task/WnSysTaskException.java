package org.nutz.walnut.ext.sys.task;

public class WnSysTaskException extends Exception {

    public WnSysTaskException(Throwable e) {
        super(e);
    }

    public WnSysTaskException(String message) {
        super(message);
    }
}
