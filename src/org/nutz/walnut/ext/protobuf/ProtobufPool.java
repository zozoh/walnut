package org.nutz.walnut.ext.protobuf;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"rawtypes"})
public class ProtobufPool {

    protected static Map<String, Class> classes = new HashMap<>();

    public static void add(Class<?> klass, String prefix) {
        for (Class klass2 : klass.getClasses()) {
            if (klass2.isInterface())
                continue;
            classes.put(prefix + klass2.getSimpleName(), klass2);
            classes.put(klass2.getName(), klass2);
        }
    }

    public static Class<?> getClass(String name) {
        return classes.get(name);
    }
}
