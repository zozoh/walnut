package com.site0.walnut.impl.box;

import java.util.LinkedList;
import java.util.List;

import com.site0.walnut.api.box.WnBox;
import com.site0.walnut.api.box.WnBoxService;
import com.site0.walnut.api.box.WnBoxStatus;

public class JvmBoxService implements WnBoxService {

    private List<JvmBox> boxes;

    private JvmExecutorFactory jef;

    public JvmBoxService(JvmExecutorFactory jef) {
        this.jef = jef;
        this.boxes = new LinkedList<JvmBox>();
    }

    @Override
    public synchronized WnBox get(String boxId) {
        for (JvmBox jb : boxes)
            if (jb.id().equals(boxId))
                return jb;
        return null;
    }

    @Override
    public synchronized WnBox alloc(long timeout) {
        JvmBox jb = null;
        jb = __find_free();
        if (null == jb) {
            jb = new JvmBox(this);
            jb.setJvmExecutorFactory(this.jef);
            boxes.add(jb);
        }
        jb.idle();
        return jb;
    }

    private JvmBox __find_free() {
        for (JvmBox jb : boxes)
            if (jb.status() == WnBoxStatus.FREE)
                return jb;
        return null;
    }

    @Override
    public synchronized void free(WnBox box) {
        if (null != box) {
            JvmBox jb = (JvmBox) box;
            jb.free();
        }
    }

    @Override
    public synchronized void shutdown() {
        for (JvmBox jb : boxes)
            if (jb.status() != WnBoxStatus.FREE)
                free(jb);
    }

}
