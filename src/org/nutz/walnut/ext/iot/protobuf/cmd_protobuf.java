package org.nutz.walnut.ext.iot.protobuf;

import org.nutz.walnut.ext.geo.gpstracker.Tracker;
import org.nutz.walnut.impl.box.JvmHdlExecutor;

public class cmd_protobuf extends JvmHdlExecutor {

    static {
        ProtobufPool.add(Tracker.class, "a9352.");
    }
}
