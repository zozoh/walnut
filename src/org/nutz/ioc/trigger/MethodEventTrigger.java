package org.nutz.ioc.trigger;

import java.lang.reflect.Method;

import org.nutz.ioc.IocEventTrigger;
import com.site0.walnut.util.Wlang;

public class MethodEventTrigger implements IocEventTrigger<Object> {

    private Method method;

    public MethodEventTrigger(Method method) {
        this.method = method;
    }

    public void trigger(Object obj) {
        try {
            method.invoke(obj);
        }
        catch (Exception e) {
            throw Wlang.wrapThrow(e);
        }
    }

}
