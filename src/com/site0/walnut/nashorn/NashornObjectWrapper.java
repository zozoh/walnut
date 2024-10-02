package com.site0.walnut.nashorn;

import java.util.Collection;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

public class NashornObjectWrapper {

    private ScriptEngine engine;

    public NashornObjectWrapper(ScriptEngine engine) {
        this.engine = engine;
    }

    @SuppressWarnings("unchecked")
    public Object deepConvert(Object obj) throws ScriptException {
        if (obj instanceof Map) {
            return deepConvertMap((Map<String, Object>) obj);
        } else if (obj instanceof Collection) {
            return deepConvertCollection((Collection<Object>) obj);
        } else if (obj instanceof Object[]) {
            return deepConvertArray((Object[]) obj);
        } else {
            return obj;
        }
    }

    private ScriptObjectMirror deepConvertMap(Map<String, Object> map) throws ScriptException {
        ScriptObjectMirror jsObject = (ScriptObjectMirror) engine.eval("new Object()");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            jsObject.put(entry.getKey(), deepConvert(entry.getValue()));
        }
        return jsObject;
    }

    private ScriptObjectMirror deepConvertCollection(Collection<Object> collection)
            throws ScriptException {
        ScriptObjectMirror jsArray = (ScriptObjectMirror) engine.eval("new Array()");
        int index = 0;
        for (Object item : collection) {
            jsArray.setSlot(index++, deepConvert(item));
        }
        return jsArray;
    }

    private ScriptObjectMirror deepConvertArray(Object[] array) throws ScriptException {
        ScriptObjectMirror jsArray = (ScriptObjectMirror) engine.eval("new Array()");
        for (int i = 0; i < array.length; i++) {
            jsArray.setSlot(i, deepConvert(array[i]));
        }
        return jsArray;
    }
}