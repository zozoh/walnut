package org.nutz.walnut.impl.box;

import java.util.LinkedList;
import java.util.List;
import org.nutz.walnut.api.box.WnBox;
import org.nutz.walnut.api.box.WnBoxService;
import org.nutz.walnut.api.box.WnBoxStatus;

public class JvmBoxService implements WnBoxService {

    private static final int ASSIGN_SIZE = 20;

    private List<JvmBox> boxes;

    private JvmExecutorFactory jef;

    public JvmBoxService(JvmExecutorFactory jef) {
        this.jef = jef;
        this.boxes = new LinkedList<JvmBox>();

        // 创建一组初始的沙箱
        for (int i = 0; i < ASSIGN_SIZE; i++) {
            JvmBox jb = new JvmBox();
            jb.jef = this.jef;
            jb.status = WnBoxStatus.FREE;
            boxes.add(jb);
        }
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
            jb = new JvmBox();
            jb.jef = this.jef;
            jb.status = WnBoxStatus.FREE;
            boxes.add(jb);
        }
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
        JvmBox jb = (JvmBox) box;
        jb.free();
        jb.status = WnBoxStatus.FREE;
    }

    @Override
    public synchronized void shutdown() {
        for (JvmBox jb : boxes)
            if (jb.status() != WnBoxStatus.FREE)
                free(jb);
    }

}
