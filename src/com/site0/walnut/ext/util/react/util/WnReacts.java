package com.site0.walnut.ext.util.react.util;

import java.util.HashMap;
import java.util.Map;

import com.site0.walnut.ext.util.react.action.ReactActionHandler;
import com.site0.walnut.ext.util.react.action.ReactCommandAction;
import com.site0.walnut.ext.util.react.action.ReactJsAction;
import com.site0.walnut.ext.util.react.action.ReactObjClearAction;
import com.site0.walnut.ext.util.react.action.ReactObjCreateAction;
import com.site0.walnut.ext.util.react.action.ReactObjDeleteAction;
import com.site0.walnut.ext.util.react.action.ReactObjUpdateAction;
import com.site0.walnut.ext.util.react.action.ReactThingClearAction;
import com.site0.walnut.ext.util.react.action.ReactThingCreateAction;
import com.site0.walnut.ext.util.react.action.ReactThingDeleteAction;
import com.site0.walnut.ext.util.react.action.ReactThingUpdateAction;
import com.site0.walnut.ext.util.react.bean.ReactAction;
import com.site0.walnut.ext.util.react.bean.ReactType;

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
