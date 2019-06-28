package org.nutz.walnut.ext.protobuf;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.castor.Castors;
import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;

@SuppressWarnings({"rawtypes", "unchecked"})
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

    public static Object fromMap(Class klass, NutMap map) throws Exception {
        Object builder = klass.getMethod("newBuilder").invoke(null);
        Mirror<Object> mirror = Mirror.me(builder);
        Method[] methods = builder.getClass().getMethods();
        for (Map.Entry<String, Object> en : map.entrySet()) {
            String key = en.getKey();
            Object value = en.getValue();
            if (value != null) {
                if (value instanceof Map) {
                    String methodName = "set" + Strings.upperFirst(key);
                    for (Method method : methods) {
                        if (method.getName().equals(methodName) && !method.getParameterTypes()[0].getName().endsWith("Builder")) {
                            value = fromMap(method.getParameterTypes()[0], (NutMap) value);
                            break;
                        }
                    }
                }
                else if (value instanceof List) {
                    String methodName = "add" + Strings.upperFirst(key);
                    Method method2 = null;
                    for (Method method : methods) {
                        if (method.getName().equals(methodName) && method.getParameterCount() == 1 && !method.getParameterTypes()[0].getName().endsWith("Builder")) {
                            method2 = method;
                            break;
                        }
                    }
                    Class typeClass = method2.getParameterTypes()[0];
                    for (Object item : (List)value) {
                        if (item instanceof NutMap)
                            item = fromMap(typeClass, (NutMap)item);
                        method2.invoke(builder, item);
                    }
                    continue; // 不需要继续处理
                }
                String methodName = "set" + Strings.upperFirst(key);
                Method m = null;
                for (Method method : methods) {
                    if (method.getName().equals(methodName) && !method.getParameterTypes()[0].getName().endsWith("Builder")) {
                        m = method;
                        break;
                    }
                }
                Class klass2 = m.getParameterTypes()[0];
                m.invoke(builder, Castors.me().castTo(value, klass2));
            }
        }
        return mirror.invoke(builder, "build");
    }

    public static NutMap toMap(Object obj) throws Exception {
        Mirror mirror = Mirror.me(obj);
        NutMap map = new NutMap();
        com.google.protobuf.Descriptors.Descriptor desc = (Descriptor) mirror.getType().getMethod("getDescriptor").invoke(null);
        for (FieldDescriptor field : desc.getFields()) {
            String name = field.getName();
            if (name.contains("_")) {
                String[] tmp = name.split("_");
                StringBuilder sb = new StringBuilder(tmp[0]);
                for (int i = 1; i < tmp.length; i++) {
                    sb.append(Strings.upperFirst(tmp[i]));
                }
                name = sb.toString();
            }
            Object value;
            if (field.isRepeated()) {
                name += "List";
            }
            value = mirror.getValue(obj, name);
            if (value != null) {
                if (value instanceof AbstractMessage) {
                    value = toMap(value);
                }
                else if (value instanceof List) {
                    List list = new ArrayList();
                    for (Object object : (List)value) {
                        if (object instanceof AbstractMessage)
                            list.add(toMap(object));
                        else
                            list.add(object);
                    }
                    value = list;
                }
            }
            map.put(name, value);
        }
        return map;
    }
}
