package com.site0.walnut.core.hdl.memory;

import java.util.HashMap;
import java.util.Map;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.core.HandleInfo;
import com.site0.walnut.core.WnIoHandle;
import com.site0.walnut.core.WnIoMappingFactory;
import com.site0.walnut.core.hdl.AbstractIoHandleManager;

public class MemoryIoHandleManager extends AbstractIoHandleManager {

    private Map<String, HandleInfo> handlers;

    public MemoryIoHandleManager(WnIoMappingFactory mappings, int timeout) {
        super(mappings, timeout);
        handlers = new HashMap<>();
    }

    @Override
    public synchronized HandleInfo load(String hid) {
        String key = _KEY(hid);
        return handlers.get(key);
    }

    @Override
    public synchronized void remove(String hid) {
        String key = _KEY(hid);
        handlers.remove(key);
    }

    @Override
    protected synchronized void doSave(WnIoHandle h) {
        if (!h.hasId()) {
            throw Er.create("e.io.hdl.doSave.withoutID");
        }
        String key = _KEY(h.getId());
        handlers.put(key, h);
    }

    @Override
    protected synchronized void doTouch(WnIoHandle h) {
        if (h.hasTimeout()) {
            String key = _KEY(h.getId());
            handlers.put(key, h);
        }
    }

    private String _KEY(String hid) {
        return hid;
    }

}
