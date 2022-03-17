package org.nutz.walnut.ext.util.react.util;

import java.util.HashMap;
import java.util.Map;

import org.nutz.walnut.ext.util.react.action.ReactActionHandler;
import org.nutz.walnut.ext.util.react.action.ReactCommandAction;
import org.nutz.walnut.ext.util.react.action.ReactJsAction;
import org.nutz.walnut.ext.util.react.action.ReactObjClearAction;
import org.nutz.walnut.ext.util.react.action.ReactObjCreateAction;
import org.nutz.walnut.ext.util.react.action.ReactObjDeleteAction;
import org.nutz.walnut.ext.util.react.action.ReactObjUpdateAction;
import org.nutz.walnut.ext.util.react.action.ReactThingClearAction;
import org.nutz.walnut.ext.util.react.action.ReactThingCreateAction;
import org.nutz.walnut.ext.util.react.action.ReactThingDeleteAction;
import org.nutz.walnut.ext.util.react.action.ReactThingUpdateAction;
import org.nutz.walnut.ext.util.react.bean.ReactAction;
import org.nutz.walnut.ext.util.react.bean.ReactType;

public abstract class WnReacts {

    private static Map<ReactType, ReactActionHandler> actions = new HashMap<>();

    static {
        actions.put(ReactType.thing_create, new ReactThingCreateAction());
        actions.put(ReactType.thing_update, new ReactThingUpdateAction());
        actions.put(ReactType.thing_delete, new ReactThingDeleteAction());
        actions.put(ReactType.thing_clear, new ReactThingClearAction());
        actions.put(ReactType.obj_create, new ReactObjCreateAction());
        actions.put(ReactType.obj_update, new ReactObjUpdateAction());
        actions.put(ReactType.obj_delete, new ReactObjDeleteAction());
        actions.put(ReactType.obj_clear, new ReactObjClearAction());
        actions.put(ReactType.exec, new ReactCommandAction());
        actions.put(ReactType.jsc, new ReactJsAction());
    }

    public static ReactActionHandler getActionHandler(ReactType type) {
        return actions.get(type);
    }

    public static ReactActionHandler getActionHandler(ReactAction a) {
        return actions.get(a.type);
    }
}
