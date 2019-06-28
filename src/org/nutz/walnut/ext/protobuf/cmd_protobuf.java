package org.nutz.walnut.ext.protobuf;

import org.nutz.walnut.ext.gpstracker.Tracker;
import org.nutz.walnut.impl.box.JvmHdlExecutor;

public class cmd_protobuf extends JvmHdlExecutor {

    static {
        ProtobufPool.add(Tracker.class, "a9352.");
    }
}
